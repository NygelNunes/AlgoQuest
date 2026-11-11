package algoquest.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import algoquest.dto.AdminDtos.AdminModuloResposta;
import algoquest.dto.AdminDtos.AdminQuestaoResposta;
import algoquest.dto.AdminDtos.AdminUsuarioResposta;
import algoquest.dto.QuestaoRequisicao;

@Repository
public class ConteudoRepositorio {

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insercaoModulo;
    private final SimpleJdbcInsert insercaoQuestao;

    public ConteudoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insercaoModulo = new SimpleJdbcInsert(jdbc).withTableName("modulo").usingGeneratedKeyColumns("id")
                .usingColumns("titulo", "descricao", "ordem", "ativo", "tema");
        this.insercaoQuestao = new SimpleJdbcInsert(jdbc).withTableName("exercicio").usingGeneratedKeyColumns("id")
                .usingColumns("modulo_id", "ordem", "tipo", "descricao", "dificuldade", "pontos", "texto_feedback", "visual");
    }

    public List<AdminModuloResposta> listarModulos() {
        return jdbc.query("SELECT m.id, m.titulo, m.descricao, m.tema, m.ativo, m.ordem, "
                + "(SELECT COUNT(*) FROM exercicio e WHERE e.modulo_id = m.id) AS qtd FROM modulo m ORDER BY m.ordem, m.id",
                (rs, n) -> new AdminModuloResposta(rs.getInt("id"), rs.getString("titulo"), rs.getString("descricao"),
                        rs.getString("tema"), rs.getBoolean("ativo"), rs.getInt("ordem"), rs.getInt("qtd")));
    }

    public AdminModuloResposta buscarModulo(int id) {
        return listarModulos().stream().filter(m -> m.id() == id).findFirst().orElse(null);
    }

    public int inserirModulo(String titulo, String descricao, String tema, boolean ativo) {
        int ordem = jdbc.queryForObject("SELECT COALESCE(MAX(ordem), 0) + 1 FROM modulo", Integer.class);
        Map<String, Object> v = new HashMap<>();
        v.put("titulo", titulo);
        v.put("descricao", descricao);
        v.put("ordem", ordem);
        v.put("ativo", ativo);
        v.put("tema", tema);
        return insercaoModulo.executeAndReturnKey(v).intValue();
    }

    public boolean atualizarModulo(int id, String titulo, String descricao, String tema, boolean ativo) {
        return jdbc.update("UPDATE modulo SET titulo = ?, descricao = ?, tema = ?, ativo = ? WHERE id = ?",
                titulo, descricao, tema, ativo, id) > 0;
    }

    public boolean excluirModulo(int id) {
        return jdbc.update("DELETE FROM modulo WHERE id = ?", id) > 0;
    }

    public List<Integer> idsModulosOrdenados() {
        return jdbc.queryForList("SELECT id FROM modulo ORDER BY ordem, id", Integer.class);
    }

    public void gravarOrdemModulos(List<Integer> ids) {
        for (int i = 0; i < ids.size(); i++) {
            jdbc.update("UPDATE modulo SET ordem = ? WHERE id = ?", i + 1, ids.get(i));
        }
    }

    public List<AdminQuestaoResposta> listarQuestoes(int moduloId) {
        Map<Integer, List<String>> opcoes = new HashMap<>();
        jdbc.query("SELECT o.exercicio_id, o.texto FROM exercicio_opcao o JOIN exercicio e ON e.id = o.exercicio_id "
                + "WHERE e.modulo_id = ? ORDER BY o.exercicio_id, o.indice", rs -> {
            opcoes.computeIfAbsent(rs.getInt("exercicio_id"), k -> new ArrayList<>()).add(rs.getString("texto"));
        }, moduloId);
        return jdbc.query("SELECT e.id, e.modulo_id, e.ordem, e.descricao, e.dificuldade, e.pontos, e.texto_feedback, e.visual, m.indice_correto "
                + "FROM exercicio e JOIN exercicio_multipla_escolha m ON m.exercicio_id = e.id "
                + "WHERE e.modulo_id = ? ORDER BY e.ordem, e.id", (rs, n) -> new AdminQuestaoResposta(rs.getInt("id"),
                rs.getInt("modulo_id"), rs.getInt("ordem"), rs.getString("descricao"), rs.getString("dificuldade"),
                rs.getInt("pontos"), opcoes.getOrDefault(rs.getInt("id"), List.of()), rs.getInt("indice_correto"),
                rs.getString("texto_feedback"), rs.getString("visual")), moduloId);
    }

    public AdminQuestaoResposta buscarQuestao(int id) {
        List<Integer> m = jdbc.queryForList("SELECT modulo_id FROM exercicio WHERE id = ?", Integer.class, id);
        return m.isEmpty() ? null : listarQuestoes(m.get(0)).stream().filter(q -> q.id() == id).findFirst().orElse(null);
    }

    public int inserirQuestao(int moduloId, QuestaoRequisicao q) {
        int ordem = jdbc.queryForObject("SELECT COALESCE(MAX(ordem), 0) + 1 FROM exercicio WHERE modulo_id = ?", Integer.class, moduloId);
        Map<String, Object> v = new HashMap<>();
        v.put("modulo_id", moduloId);
        v.put("ordem", ordem);
        v.put("tipo", "MULTIPLA_ESCOLHA");
        v.put("descricao", q.descricao());
        v.put("dificuldade", q.dificuldade());
        v.put("pontos", q.pontos());
        v.put("texto_feedback", q.explicacao());
        v.put("visual", q.visual());
        int id = insercaoQuestao.executeAndReturnKey(v).intValue();
        jdbc.update("INSERT INTO exercicio_multipla_escolha (exercicio_id, indice_correto) VALUES (?, ?)", id, q.indiceCorreto());
        gravarOpcoes(id, q.opcoes());
        return id;
    }

    public boolean atualizarQuestao(int id, QuestaoRequisicao q) {
        int n = jdbc.update("UPDATE exercicio SET descricao = ?, dificuldade = ?, pontos = ?, texto_feedback = ?, visual = ? WHERE id = ?",
                q.descricao(), q.dificuldade(), q.pontos(), q.explicacao(), q.visual(), id);
        if (n == 0) return false;
        jdbc.update("UPDATE exercicio_multipla_escolha SET indice_correto = ? WHERE exercicio_id = ?", q.indiceCorreto(), id);
        jdbc.update("DELETE FROM exercicio_opcao WHERE exercicio_id = ?", id);
        gravarOpcoes(id, q.opcoes());
        return true;
    }

    private void gravarOpcoes(int id, List<String> opcoes) {
        for (int i = 0; i < opcoes.size(); i++) {
            jdbc.update("INSERT INTO exercicio_opcao (exercicio_id, indice, texto) VALUES (?, ?, ?)", id, i, opcoes.get(i));
        }
    }

    public boolean excluirQuestao(int id) {
        return jdbc.update("DELETE FROM exercicio WHERE id = ?", id) > 0;
    }

    public List<Integer> idsQuestoesOrdenadas(int moduloId) {
        return jdbc.queryForList("SELECT id FROM exercicio WHERE modulo_id = ? ORDER BY ordem, id", Integer.class, moduloId);
    }

    public void gravarOrdemQuestoes(List<Integer> ids) {
        for (int i = 0; i < ids.size(); i++) {
            jdbc.update("UPDATE exercicio SET ordem = ? WHERE id = ?", i + 1, ids.get(i));
        }
    }

    public List<String> enunciados(int moduloId) {
        return jdbc.queryForList("SELECT descricao FROM exercicio WHERE modulo_id = ?", String.class, moduloId);
    }

    public List<AdminUsuarioResposta> listarUsuarios() {
        return jdbc.query("SELECT id, nome_usuario, papel, nivel, pontuacao, criado_em FROM usuario ORDER BY id",
                (rs, n) -> new AdminUsuarioResposta(rs.getInt("id"), rs.getString("nome_usuario"), null, rs.getString("papel"),
                        rs.getInt("nivel"), rs.getInt("pontuacao"),
                        rs.getTimestamp("criado_em") == null ? null : rs.getTimestamp("criado_em").toInstant().toString()));
    }
}
