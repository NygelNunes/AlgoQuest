package algoquest.model;

import java.util.List;

public class ExercicioMultiplaEscolha extends Exercicio {

    private List<String> opcoes;
    private int indiceCorreto;

    public ExercicioMultiplaEscolha(int id, String descricao, Dificuldade dificuldade, int pontos,
                                    String textoFeedback, List<String> opcoes, int indiceCorreto) {
        super(id, descricao, dificuldade, pontos, textoFeedback);
        this.opcoes = opcoes;
        this.indiceCorreto = indiceCorreto;
    }

    @Override
    public boolean verificarResposta(Object resposta) {
        return converterIndice(resposta) == indiceCorreto;
    }

    private int converterIndice(Object resposta) {
        int indice;
        if (resposta instanceof Integer || resposta instanceof Long || resposta instanceof Short || resposta instanceof Byte) {
            long valor = ((Number) resposta).longValue();
            if (valor < Integer.MIN_VALUE || valor > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Resposta fora do intervalo valido");
            }
            indice = (int) valor;
        } else if (resposta instanceof String texto && texto.trim().matches("-?\\d{1,9}")) {
            indice = Integer.parseInt(texto.trim());
        } else {
            throw new IllegalArgumentException("A resposta deve ser o indice numerico da opcao escolhida");
        }
        if (indice < 0 || indice >= opcoes.size()) {
            throw new IllegalArgumentException("Opcao inexistente: " + indice);
        }
        return indice;
    }

    public List<String> getOpcoes() { return opcoes; }
    public void setOpcoes(List<String> opcoes) { this.opcoes = opcoes; }

    public int getIndiceCorreto() { return indiceCorreto; }
    public void setIndiceCorreto(int indiceCorreto) { this.indiceCorreto = indiceCorreto; }
}
