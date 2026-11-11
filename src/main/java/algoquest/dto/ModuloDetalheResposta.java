package algoquest.dto;

import java.util.List;

import algoquest.model.Exercicio;
import algoquest.model.ExercicioMultiplaEscolha;
import algoquest.model.Modulo;

public record ModuloDetalheResposta(int id, String titulo, String descricao, String tema, List<ExercicioResposta> exercicios) {

    public record ExercicioResposta(int id, String descricao, List<String> opcoes, String dificuldade, int pontos, String visual) {

        static ExercicioResposta de(Exercicio e) {
            List<String> opcoes = e instanceof ExercicioMultiplaEscolha m ? List.copyOf(m.getOpcoes()) : List.of();
            return new ExercicioResposta(e.getId(), e.getDescricao(), opcoes, e.getDificuldade().name(), e.getPontos(), e.getVisual());
        }
    }

    public static ModuloDetalheResposta de(Modulo m) {
        return new ModuloDetalheResposta(m.getId(), m.getTitulo(), m.getDescricao(), m.getTema(),
                m.getExercicios().stream().map(ExercicioResposta::de).toList());
    }
}
