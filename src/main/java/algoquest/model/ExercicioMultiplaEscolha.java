package algoquest.model;

import java.util.ArrayList;
import java.util.List;

public class ExercicioMultiplaEscolha extends Exercicio {

    private List<String> opcoes;
    private int indiceCorreto;

    public ExercicioMultiplaEscolha() {
        super();
        this.opcoes = new ArrayList<>();
    }

    public ExercicioMultiplaEscolha(int id, String descricao, Dificuldade dificuldade, int pontos,
                                    String textoFeedback, List<String> opcoes, int indiceCorreto) {
        super(id, descricao, dificuldade, pontos, textoFeedback);
        this.opcoes = opcoes;
        this.indiceCorreto = indiceCorreto;
    }

    @Override
    public boolean verificarResposta(Object resposta) {
        // TODO: converter a resposta para Integer e comparar com indiceCorreto
        return false;
    }

    public List<String> getOpcoes() { return opcoes; }
    public void setOpcoes(List<String> opcoes) { this.opcoes = opcoes; }

    public int getIndiceCorreto() { return indiceCorreto; }
    public void setIndiceCorreto(int indiceCorreto) { this.indiceCorreto = indiceCorreto; }
}
