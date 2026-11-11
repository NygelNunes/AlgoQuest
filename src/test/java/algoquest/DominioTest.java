package algoquest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import algoquest.model.*;

class DominioTest {

    @Test
    void usuarioAcumulaPontosEXpESobeDeNivelACada100Xp() {
        Usuario u = new Usuario(1, "ana", "hash");
        u.adicionarPontos(10);
        u.adicionarXp(99);
        assertEquals(10, u.getPontuacao());
        assertEquals(1, u.getNivel());
        assertEquals(100, u.getXpProximoNivel());
        u.adicionarXp(1);
        assertEquals(2, u.getNivel());
        assertEquals(200, u.getXpProximoNivel());
        u.adicionarXp(250);
        assertEquals(4, u.getNivel());
    }

    @Test
    void usuarioRejeitaValoresNegativos() {
        Usuario u = new Usuario(1, "ana", "hash");
        assertThrows(IllegalArgumentException.class, () -> u.adicionarPontos(-1));
        assertThrows(IllegalArgumentException.class, () -> u.adicionarXp(-1));
    }

    @Test
    void progressoNaoDuplicaExercicios() {
        ProgressoUsuario p = new ProgressoUsuario(1);
        p.marcarExercicioConcluido(7);
        p.marcarExercicioConcluido(7);
        assertEquals(1, p.getExerciciosConcluidos().size());
        assertTrue(p.isConcluido(7));
        assertFalse(p.isConcluido(8));
    }

    @Test
    void moduloCalculaPorcentagemEQuantidade() {
        Modulo m = new Modulo(1, "M", "d");
        assertEquals(0.0, m.calcularPorcentagem(new ProgressoUsuario(1)));
        for (int i = 1; i <= 4; i++) {
            m.adicionarExercicio(new ExercicioMultiplaEscolha(i, "q", Dificuldade.FACIL, 10, "f", List.of("a", "b"), 0));
        }
        ProgressoUsuario p = new ProgressoUsuario(1);
        p.marcarExercicioConcluido(1);
        assertEquals(4, m.getQuantidadeExercicios());
        assertEquals(25.0, m.calcularPorcentagem(p));
        p.marcarExercicioConcluido(2);
        p.marcarExercicioConcluido(99);
        assertEquals(50.0, m.calcularPorcentagem(p));
    }

    @Test
    void multiplaEscolhaVerificaIndiceEValidaEntrada() {
        ExercicioMultiplaEscolha e = new ExercicioMultiplaEscolha(1, "q", Dificuldade.MEDIO, 20, "f",
                List.of("a", "b", "c"), 2);
        assertTrue(e.verificarResposta(2));
        assertTrue(e.verificarResposta(2L));
        assertTrue(e.verificarResposta("2"));
        assertFalse(e.verificarResposta(0));
        for (Object invalida : new Object[] { -1, 3, 99, "abc", "", 1.5, true, null, new Object() }) {
            assertThrows(IllegalArgumentException.class, () -> e.verificarResposta(invalida), String.valueOf(invalida));
        }
    }
}
