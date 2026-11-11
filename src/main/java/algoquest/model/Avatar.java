package algoquest.model;

import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class Avatar {

    public static final String PERSONALIZADO = "personalizado";
    public static final int MAX_BYTES_IMAGEM = 150 * 1024;
    private static final Set<String> VALIDOS = Set.copyOf(Arrays.asList(AvatarIds.TODOS));
    private static final String[] TIPOS = {"image/jpeg", "image/png", "image/webp"};

    private Avatar() {
    }

    public static boolean idDoCatalogo(String id) {
        return id != null && VALIDOS.contains(id);
    }

    public static String padraoPara(String nomeUsuario, RandomGenerator aleatorio) {
        String n = nomeUsuario == null ? "" : nomeUsuario.trim().toLowerCase();
        String[] grupo = n.endsWith("a") ? AvatarIds.FEMININOS : AvatarIds.MASCULINOS;
        return grupo[aleatorio.nextInt(grupo.length)];
    }

    public static String validarImagem(String dataUrl) {
        return validarImagem(dataUrl, MAX_BYTES_IMAGEM);
    }

    public static String validarImagem(String dataUrl, int maxBytes) {
        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            throw new IllegalArgumentException("Imagem inválida.");
        }
        int virgula = dataUrl.indexOf(',');
        if (virgula < 0 || virgula > 60 || dataUrl.length() > maxBytes * 4 / 3 + 100) {
            throw new IllegalArgumentException("Imagem inválida ou grande demais.");
        }
        String cabecalho = dataUrl.substring(5, virgula);
        String tipo = null;
        for (String t : TIPOS) {
            if (cabecalho.equals(t + ";base64")) tipo = t;
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Formato não suportado. Use JPEG, PNG ou WebP.");
        }
        byte[] b;
        try {
            b = Base64.getDecoder().decode(dataUrl.substring(virgula + 1));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Imagem inválida.");
        }
        if (b.length == 0 || b.length > maxBytes) {
            throw new IllegalArgumentException("Imagem grande demais (máximo " + maxBytes / 1024 + " KB).");
        }
        boolean ok = switch (tipo) {
            case "image/jpeg" -> b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "image/png" -> b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            default -> b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        };
        if (!ok) {
            throw new IllegalArgumentException("O arquivo não corresponde a uma imagem válida.");
        }
        return dataUrl;
    }
}
