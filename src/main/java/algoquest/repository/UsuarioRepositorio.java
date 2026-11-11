package algoquest.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import algoquest.model.Papel;
import algoquest.model.Usuario;

@Repository
public class UsuarioRepositorio {

    private static final String COLUNAS = "id, nome_usuario, senha_hash, pontuacao, xp, nivel, avatar, papel";
    private static final RowMapper<Usuario> MAPEADOR = (rs, n) -> {
        Usuario u = new Usuario(
                rs.getInt("id"), rs.getString("nome_usuario"), rs.getString("senha_hash"),
                rs.getInt("pontuacao"), rs.getInt("xp"), rs.getInt("nivel"), rs.getString("avatar"));
        u.setPapel(Papel.de(rs.getString("papel")));
        return u;
    };

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insercao;

    public UsuarioRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insercao = new SimpleJdbcInsert(jdbc).withTableName("usuario").usingGeneratedKeyColumns("id")
                .usingColumns("nome_usuario", "senha_hash", "pontuacao", "xp", "nivel", "avatar", "papel");
    }

    public void salvar(Usuario u) {
        if (u.getId() == 0) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("nome_usuario", u.getNomeUsuario());
            valores.put("senha_hash", u.getSenha());
            valores.put("pontuacao", u.getPontuacao());
            valores.put("xp", u.getXp());
            valores.put("nivel", u.getNivel());
            valores.put("avatar", u.getAvatar());
            valores.put("papel", u.getPapel().name());
            u.setId(insercao.executeAndReturnKey(valores).intValue());
        } else {
            jdbc.update("UPDATE usuario SET nome_usuario = ?, senha_hash = ?, pontuacao = ?, xp = ?, nivel = ? WHERE id = ?",
                    u.getNomeUsuario(), u.getSenha(), u.getPontuacao(), u.getXp(), u.getNivel(), u.getId());
        }
    }

    public void atualizarPapel(int id, Papel papel) {
        jdbc.update("UPDATE usuario SET papel = ? WHERE id = ?", papel.name(), id);
    }

    public void atualizarAvatar(int id, String avatar, String imagem) {
        jdbc.update("UPDATE usuario SET avatar = ?, avatar_imagem = ? WHERE id = ?", avatar, imagem, id);
    }

    public boolean definirAvatarSeVazio(int id, String avatar) {
        return jdbc.update("UPDATE usuario SET avatar = ? WHERE id = ? AND avatar IS NULL", avatar, id) > 0;
    }

    public String buscarAvatarImagem(int id) {
        List<String> r = jdbc.query("SELECT avatar_imagem FROM usuario WHERE id = ?", (rs, n) -> rs.getString(1), id);
        return r.isEmpty() ? null : r.get(0);
    }

    public Usuario buscarPorId(int id) {
        List<Usuario> r = jdbc.query("SELECT " + COLUNAS + " FROM usuario WHERE id = ?", MAPEADOR, id);
        return r.isEmpty() ? null : r.get(0);
    }

    public Usuario buscarPorNome(String nomeUsuario) {
        List<Usuario> r = jdbc.query("SELECT " + COLUNAS + " FROM usuario WHERE nome_usuario = ?", MAPEADOR, nomeUsuario);
        return r.isEmpty() ? null : r.get(0);
    }

    public Usuario buscarPorIdParaAtualizar(int id) {
        List<Usuario> r = jdbc.query("SELECT " + COLUNAS + " FROM usuario WHERE id = ? FOR UPDATE", MAPEADOR, id);
        return r.isEmpty() ? null : r.get(0);
    }
}
