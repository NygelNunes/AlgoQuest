package algoquest.repository;

import org.springframework.stereotype.Repository;

import algoquest.model.Usuario;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioRepositorio implements Repositorio<Usuario> {

    @Override
    public void salvar(Usuario entidade) {
        // TODO: implementar persistencia
    }

    @Override
    public Usuario buscarPorId(int id) {
        // TODO: implementar busca
        return null;
    }

    @Override
    public List<Usuario> buscarTodos() {
        // TODO: implementar busca
        return new ArrayList<>();
    }

    @Override
    public void deletar(int id) {
        // TODO: implementar remocao
    }

    public Usuario buscarPorNome(String nomeUsuario) {
        // TODO: implementar busca por nome de usuario
        return null;
    }
}
