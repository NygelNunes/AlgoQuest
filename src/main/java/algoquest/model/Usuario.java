package algoquest.model;

public class Usuario {

    private int id;
    private String nomeUsuario;
    private String senha;
    private int pontuacao;
    private int xp;
    private int nivel;
    private Papel papel = Papel.USUARIO;
    private String avatar;
    private String avatarImagem;

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

    public Usuario(int id, String nomeUsuario, String senha, int pontuacao, int xp, int nivel, String avatar) {
        this(id, nomeUsuario, senha, pontuacao, xp, nivel);
        this.avatar = avatar;
    }

    public static final int XP_POR_NIVEL = 100;

    public void adicionarPontos(int pontos) {
        if (pontos < 0) {
            throw new IllegalArgumentException("Pontos nao podem ser negativos");
        }
        this.pontuacao += pontos;
    }

    public void adicionarXp(int quantidadeXp) {
        if (quantidadeXp < 0) {
            throw new IllegalArgumentException("XP nao pode ser negativo");
        }
        this.xp += quantidadeXp;
        while (this.xp >= this.nivel * XP_POR_NIVEL) {
            subirNivel();
        }
    }

    public void subirNivel() {
        this.nivel++;
    }

    public int getXpProximoNivel() {
        return nivel * XP_POR_NIVEL;
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

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getAvatarImagem() { return avatarImagem; }
    public void setAvatarImagem(String avatarImagem) { this.avatarImagem = avatarImagem; }

    public Papel getPapel() { return papel; }
    public void setPapel(Papel papel) { this.papel = papel == null ? Papel.USUARIO : papel; }
}
