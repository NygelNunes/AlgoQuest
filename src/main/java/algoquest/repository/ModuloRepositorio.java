package algoquest.repository;

import org.springframework.stereotype.Repository;

import algoquest.model.Modulo;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ModuloRepositorio implements Repositorio<Modulo> {

    @Override
    public void salvar(Modulo entidade) {
        // TODO: implementar persistencia
    }

    @Override
    public Modulo buscarPorId(int id) {
        // TODO: implementar busca
        return null;
    }

    @Override
    public List<Modulo> buscarTodos() {
        // TODO: implementar busca
        return new ArrayList<>();
    }

    @Override
    public void deletar(int id) {
        // TODO: implementar remocao
    }

    public List<Modulo> buscarModulosAtivos() {
        // TODO: implementar busca de modulos ativos
        return new ArrayList<>();
    }
}
