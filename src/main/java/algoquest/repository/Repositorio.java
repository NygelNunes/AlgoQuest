package algoquest.repository;

import java.util.List;

public interface Repositorio<T> {

    void salvar(T entidade);

    T buscarPorId(int id);

    List<T> buscarTodos();

    void deletar(int id);
}
