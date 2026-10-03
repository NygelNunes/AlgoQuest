package algoquest.util;

import algoquest.model.Usuario;

public class SessaoUsuario {

    private static SessaoUsuario instanciaUnica;
    private Usuario usuarioAtual;

    private SessaoUsuario() {
    }

    public static SessaoUsuario getInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new SessaoUsuario();
        }
        return instanciaUnica;
    }

    public Usuario getUsuarioAtual() {
        return usuarioAtual;
    }

    public void setUsuarioAtual(Usuario usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
    }

    public void limparSessao() {
        this.usuarioAtual = null;
    }
}
