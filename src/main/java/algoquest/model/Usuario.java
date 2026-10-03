package algoquest.model;

public class Usuario {

    private int id;
    private String nomeUsuario;
    private String senha;
    private int pontuacao;
    private int xp;
    private int nivel;

    public Usuario() {
        this.nivel = 1;
    }

    public Usuario(int id, String nomeUsuario, String senha) {
        this.id = id;
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.pontuacao = 0;
        this.xp = 0;
        this.nivel = 1;
    }

    public Usuario(int id, String nomeUsuario, String senha, int pontuacao, int xp, int nivel) {
        this.id = id;
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.pontuacao = pontuacao;
        this.xp = xp;
        this.nivel = nivel;
    }

    public void adicionarPontos(int pontos) {
        // TODO: implementar regra de acumulo de pontos
    }

    public void adicionarXp(int xp) {
        // TODO: implementar regra de acumulo de XP (e verificar subida de nivel)
    }

    public void subirNivel() {
        // TODO: implementar regra de subida de nivel
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public int getPontuacao() { return pontuacao; }
    public void setPontuacao(int pontuacao) { this.pontuacao = pontuacao; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
}
