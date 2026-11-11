package algoquest.model;

public enum Papel {
    USUARIO, ADMIN, MASTER;

    public boolean podeAdministrarConteudo() {
        return this == ADMIN || this == MASTER;
    }

    public boolean podeGerenciarUsuarios() {
        return this == MASTER;
    }

    public static Papel de(String texto) {
        if (texto == null) return USUARIO;
        try {
            return valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return USUARIO;
        }
    }

    public static String validarAlteracao(Papel ator, int idAtor, Papel atualAlvo, int idAlvo, Papel novo) {
        if (!ator.podeGerenciarUsuarios()) return "Somente o Master pode alterar papéis.";
        if (idAtor == idAlvo) return "Você não pode alterar o seu próprio papel.";
        if (atualAlvo == MASTER) return "A conta Master é protegida e não pode ser alterada.";
        if (novo == MASTER) return "Não é possível promover alguém a Master.";
        return null;
    }
}
