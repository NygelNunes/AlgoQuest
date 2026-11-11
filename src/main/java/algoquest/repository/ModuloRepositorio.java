package algoquest.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import algoquest.model.Dificuldade;
import algoquest.model.Exercicio;
import algoquest.model.ExercicioMultiplaEscolha;
import algoquest.model.Modulo;

@Repository
public class ModuloRepositorio {

    private static final String SQL_EXERCICIOS =
            "SELECT e.id, e.modulo_id, e.descricao, e.dificuldade, e.pontos, e.texto_feedback, e.visual, m.indice_correto "
            + "FROM exercicio e JOIN exercicio_multipla_escolha m ON m.exercicio_id = e.id ";

    private final JdbcTemplate jdbc;

    public ModuloRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Modulo buscarAtivoPorId(int id) {
        return buscarUm("SELECT id, titulo, descricao, tema FROM modulo WHERE id = ? AND ativo = TRUE", id);
    }

    public List<Modulo> buscarModulosAtivos() {
        return listar("SELECT id, titulo, descricao, tema FROM modulo WHERE ativo = TRUE ORDER BY ordem, id");
    }

    public Exercicio buscarExercicioPorId(int idExercicio) {
        List<ExercicioMultiplaEscolha> r = jdbc.query(
                SQL_EXERCICIOS + "JOIN modulo mo ON mo.id = e.modulo_id AND mo.ativo = TRUE WHERE e.id = ?",
                (rs, n) -> montarExercicio(rs), idExercicio);
        if (r.isEmpty()) {
            return null;
        }
        ExercicioMultiplaEscolha ex = r.get(0);
        ex.setOpcoes(jdbc.queryForList("SELECT texto FROM exercicio_opcao WHERE exercicio_id = ? ORDER BY indice",
                String.class, idExercicio));
        return ex;
    }

    private Modulo buscarUm(String sql, int id) {
        List<Modulo> r = new ArrayList<>(jdbc.query(sql, (rs, n) -> montarModulo(rs), id));
        if (r.isEmpty()) {
            return null;
        }
        Modulo m = r.get(0);
        carregarExercicios(Map.of(m.getId(), m));
        return m;
    }

    private List<Modulo> listar(String sql) {
        Map<Integer, Modulo> porId = new LinkedHashMap<>();
        jdbc.query(sql, rs -> {
            porId.put(rs.getInt("id"), montarModulo(rs));
        });
        carregarExercicios(porId);
        return new ArrayList<>(porId.values());
    }

    private void carregarExercicios(Map<Integer, Modulo> modulos) {
        if (modulos.isEmpty()) {
            return;
        }
        Object[] ids = modulos.keySet().toArray();
        String marcadores = String.join(",", Collections.nCopies(ids.length, "?"));
        Map<Integer, ExercicioMultiplaEscolha> porId = new LinkedHashMap<>();
        jdbc.query(SQL_EXERCICIOS + "WHERE e.modulo_id IN (" + marcadores + ") ORDER BY e.modulo_id, e.ordem, e.id", rs -> {
            ExercicioMultiplaEscolha ex = montarExercicio(rs);
            porId.put(ex.getId(), ex);
            modulos.get(rs.getInt("modulo_id")).adicionarExercicio(ex);
        }, ids);
        jdbc.query("SELECT exercicio_id, texto FROM exercicio_opcao WHERE exercicio_id IN "
                + "(SELECT id FROM exercicio WHERE modulo_id IN (" + marcadores + ")) ORDER BY exercicio_id, indice", rs -> {
            porId.get(rs.getInt("exercicio_id")).getOpcoes().add(rs.getString("texto"));
        }, ids);
    }

    private static Modulo montarModulo(ResultSet rs) throws SQLException {
        Modulo m = new Modulo(rs.getInt("id"), rs.getString("titulo"), rs.getString("descricao"));
        m.setTema(rs.getString("tema"));
        return m;
    }

    private static ExercicioMultiplaEscolha montarExercicio(ResultSet rs) throws SQLException {
        ExercicioMultiplaEscolha ex = new ExercicioMultiplaEscolha(rs.getInt("id"), rs.getString("descricao"),
                Dificuldade.valueOf(rs.getString("dificuldade")), rs.getInt("pontos"),
                rs.getString("texto_feedback"), new ArrayList<>(), rs.getInt("indice_correto"));
        ex.setVisual(rs.getString("visual"));
        return ex;
    }
}
