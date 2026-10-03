package algoquest.model;

public abstract class Exercicio {

    private int id;
    private String descricao;
    private Dificuldade dificuldade;
    private int pontos;
    private String textoFeedback;

    public Exercicio() {
    }

    public Exercicio(int id, String descricao, Dificuldade dificuldade, int pontos, String textoFeedback) {
        this.id = id;
        this.descricao = descricao;
        this.dificuldade = dificuldade;
        this.pontos = pontos;
        this.textoFeedback = textoFeedback;
    }

    /**
     * Verifica se a resposta informada esta correta.
     * Cada tipo de exercicio define sua propria estrategia de verificacao.
     */
    public abstract boolean verificarResposta(Object resposta);

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Dificuldade getDificuldade() { return dificuldade; }
    public void setDificuldade(Dificuldade dificuldade) { this.dificuldade = dificuldade; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public String getTextoFeedback() { return textoFeedback; }
    public void setTextoFeedback(String textoFeedback) { this.textoFeedback = textoFeedback; }
}
