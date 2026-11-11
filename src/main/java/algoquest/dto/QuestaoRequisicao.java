package algoquest.dto;

import java.util.List;

public record QuestaoRequisicao(String descricao, String dificuldade, Integer pontos, List<String> opcoes,
                                Integer indiceCorreto, String explicacao, String visual) {
}
