package algoquest.dto;

import algoquest.model.Modulo;

public record ModuloResumoResposta(int id, String titulo, String descricao, String tema, double porcentagem,
                                   int quantidadeExercicios) {

    public static ModuloResumoResposta de(Modulo m, double porcentagem) {
        double arredondada = Math.round(porcentagem * 100.0) / 100.0;
        return new ModuloResumoResposta(m.getId(), m.getTitulo(), m.getDescricao(), m.getTema(), arredondada,
                m.getQuantidadeExercicios());
    }
}
