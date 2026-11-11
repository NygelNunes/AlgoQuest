package algoquest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import algoquest.util.HashSenha;

class HashSenhaTest {

    private final HashSenha hash = new HashSenha(1_000);

    @Test
    void hashNaoContemASenhaEUsaSaltAleatorio() {
        String a = hash.gerar("minha-senha");
        String b = hash.gerar("minha-senha");
        assertTrue(a.startsWith("pbkdf2_sha256$1000$"));
        assertFalse(a.contains("minha-senha"));
        assertNotEquals(a, b);
    }

    @Test
    void verificaSenhaCorretaERejeitaIncorretaEHashMalformado() {
        String h = hash.gerar("minha-senha");
        assertTrue(hash.verificar("minha-senha", h));
        assertFalse(hash.verificar("outra", h));
        assertFalse(hash.verificar("minha-senha", "lixo"));
        assertFalse(hash.verificar("minha-senha", "pbkdf2_sha256$x$y$z"));
        assertFalse(hash.verificar("minha-senha", ""));
    }

    @Test
    void hashGuardaSuasIteracoesEntaoMudarAConfiguracaoNaoInvalidaSenhasAntigas() {
        String antigo = new HashSenha(500).gerar("segredo");
        assertTrue(new HashSenha(2_000).verificar("segredo", antigo));
    }

    @Test
    void tokensSaoUnicosELongosEHashDeTokenEstavel() {
        String t1 = HashSenha.gerarToken(), t2 = HashSenha.gerarToken();
        assertNotEquals(t1, t2);
        assertEquals(43, t1.length());
        assertEquals(64, HashSenha.hashDeToken(t1).length());
        assertEquals(HashSenha.hashDeToken(t1), HashSenha.hashDeToken(t1));
        assertNotEquals(t1, HashSenha.hashDeToken(t1));
    }
}
