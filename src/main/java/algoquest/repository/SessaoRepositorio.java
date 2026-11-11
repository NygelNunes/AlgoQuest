package algoquest.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SessaoRepositorio {

    private final JdbcTemplate jdbc;

    public SessaoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void criar(String tokenHash, int idUsuario, Instant expiraEm) {
        jdbc.update("INSERT INTO sessao (token_hash, usuario_id, expira_em) VALUES (?, ?, ?)",
                tokenHash, idUsuario, Timestamp.from(expiraEm));
    }

    public Integer buscarIdUsuarioValido(String tokenHash, Instant agora) {
        List<Integer> r = jdbc.queryForList(
                "SELECT usuario_id FROM sessao WHERE token_hash = ? AND expira_em > ?",
                Integer.class, tokenHash, Timestamp.from(agora));
        return r.isEmpty() ? null : r.get(0);
    }

    public void remover(String tokenHash) {
        jdbc.update("DELETE FROM sessao WHERE token_hash = ?", tokenHash);
    }

    public void removerExpiradas(Instant agora) {
        jdbc.update("DELETE FROM sessao WHERE expira_em <= ?", Timestamp.from(agora));
    }
}
