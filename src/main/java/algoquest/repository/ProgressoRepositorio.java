package algoquest.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import algoquest.model.ProgressoUsuario;

@Repository
public class ProgressoRepositorio {

    private final JdbcTemplate jdbc;

    public ProgressoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ProgressoUsuario buscarPorUsuario(int idUsuario) {
        List<Integer> ids = jdbc.queryForList(
                "SELECT exercicio_id FROM progresso_usuario WHERE usuario_id = ? ORDER BY exercicio_id",
                Integer.class, idUsuario);
        return new ProgressoUsuario(idUsuario, new ArrayList<>(ids));
    }

    public void registrarConclusao(int idUsuario, int idExercicio) {
        jdbc.update("INSERT INTO progresso_usuario (usuario_id, exercicio_id) VALUES (?, ?)", idUsuario, idExercicio);
    }
}
