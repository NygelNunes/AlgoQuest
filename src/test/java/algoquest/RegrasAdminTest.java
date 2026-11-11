package algoquest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import algoquest.dto.QuestaoRequisicao;
import algoquest.model.Papel;
import algoquest.model.ValidadorConteudo;
import algoquest.model.Visual;

class RegrasAdminTest {

    @Test
    void hierarquiaDePapeis() {
        assertFalse(Papel.USUARIO.podeAdministrarConteudo());
        assertTrue(Papel.ADMIN.podeAdministrarConteudo());
        assertFalse(Papel.ADMIN.podeGerenciarUsuarios());
        assertTrue(Papel.MASTER.podeGerenciarUsuarios());
        assertEquals(Papel.USUARIO, Papel.de("qualquer coisa"));
    }

    @Test
    void alteracaoDePapelSoPeloMasterEComProtecoes() {
        assertNull(Papel.validarAlteracao(Papel.MASTER, 1, Papel.USUARIO, 2, Papel.ADMIN));
        assertNull(Papel.validarAlteracao(Papel.MASTER, 1, Papel.ADMIN, 2, Papel.USUARIO));
        assertNotNull(Papel.validarAlteracao(Papel.ADMIN, 5, Papel.USUARIO, 2, Papel.ADMIN));
        assertNotNull(Papel.validarAlteracao(Papel.MASTER, 1, Papel.MASTER, 1, Papel.USUARIO));
        assertNotNull(Papel.validarAlteracao(Papel.MASTER, 1, Papel.USUARIO, 2, Papel.MASTER));
    }

    @Test
    void svgSeguroEAceitoEMaliciosoRecusado() {
        assertTrue(Visual.validar("<svg viewBox=\"0 0 10 10\"><circle cx=\"5\" cy=\"5\" r=\"4\"/></svg>").contains("xmlns"));
        for (String ruim : List.of("<svg><script>alert(1)</script></svg>", "<svg onload=\"x()\"></svg>", "<svg><foreignObject/></svg>",
                "<svg><image href=\"http://x/y.png\"/></svg>", "<!DOCTYPE svg [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><svg>&x;</svg>", "<div>oi</div>")) {
            assertThrows(IllegalArgumentException.class, () -> Visual.validar(ruim), ruim);
        }
    }

    @Test
    void questaoValidaNormalizaEInvalidaERecusada() {
        QuestaoRequisicao ok = ValidadorConteudo.validarQuestao(new QuestaoRequisicao("  Qual e O(1)?  ", "facil", null, List.of("A", "B"), 1, "", null));
        assertEquals(10, ok.pontos());
        assertEquals("FACIL", ok.dificuldade());
        assertNull(ok.explicacao());
        assertThrows(IllegalArgumentException.class, () -> ValidadorConteudo.validarQuestao(new QuestaoRequisicao("Enunciado ok", "FACIL", 1, List.of("A"), 0, null, null)));
        assertThrows(IllegalArgumentException.class, () -> ValidadorConteudo.validarQuestao(new QuestaoRequisicao("Enunciado ok", "FACIL", 1, List.of("A", "B"), 7, null, null)));
        assertThrows(IllegalArgumentException.class, () -> ValidadorConteudo.validarQuestao(new QuestaoRequisicao("Enunciado ok", "FACIL", 1, List.of("A", "a"), 0, null, null)));
    }
}
