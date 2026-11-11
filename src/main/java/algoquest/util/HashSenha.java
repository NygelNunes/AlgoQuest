package algoquest.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HashSenha {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIXO = "pbkdf2_sha256";
    private static final int TAMANHO_SALT = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final int iteracoes;
    private volatile String hashFalso;

    public HashSenha(@Value("${algoquest.senha.iteracoes:600000}") int iteracoes) {
        if (iteracoes < 1) {
            throw new IllegalArgumentException("algoquest.senha.iteracoes deve ser positivo");
        }
        this.iteracoes = iteracoes;
    }

    public String gerar(String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = derivar(senha.toCharArray(), salt, iteracoes);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIXO + "$" + iteracoes + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
    }

    public boolean verificar(String senha, String armazenado) {
        try {
            String[] partes = armazenado.split("\\$");
            if (partes.length != 4 || !PREFIXO.equals(partes[0])) {
                return false;
            }
            int it = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] obtido = derivar(senha.toCharArray(), salt, it);
            return MessageDigest.isEqual(esperado, obtido);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void verificarFalso(String senha) {
        if (hashFalso == null) {
            hashFalso = gerar("senha-descartavel-" + System.nanoTime());
        }
        verificar(senha, hashFalso);
    }

    public static String gerarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hashDeToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] derivar(char[] senha, byte[] salt, int iteracoes) {
        PBEKeySpec spec = new PBEKeySpec(senha, salt, iteracoes, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 indisponivel", e);
        } finally {
            spec.clearPassword();
        }
    }
}
