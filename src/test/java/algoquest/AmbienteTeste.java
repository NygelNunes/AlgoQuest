package algoquest;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import algoquest.config.ResolvedorUsuarioAutenticado;
import algoquest.controller.LoginController;
import algoquest.controller.ModuloController;
import algoquest.exception.TratadorErros;
import algoquest.model.Usuario;
import algoquest.repository.ModuloRepositorio;
import algoquest.repository.ProgressoRepositorio;
import algoquest.repository.SessaoRepositorio;
import algoquest.repository.UsuarioRepositorio;
import algoquest.service.ServicoAutenticacao;
import algoquest.service.ServicoAdmin;
import algoquest.service.ServicoAvatar;
import algoquest.controller.AdminController;
import algoquest.repository.ConteudoRepositorio;
import algoquest.service.ServicoGamificacao;
import algoquest.service.ServicoModulo;
import algoquest.service.ServicoResposta;
import algoquest.util.HashSenha;

final class AmbienteTeste {

    final DataSource dataSource;
    final JdbcTemplate jdbc;
    final HashSenha hashSenha = new HashSenha(1_000);
    final UsuarioRepositorio usuarios;
    final SessaoRepositorio sessoes;
    final MockMvc mvc;

    AmbienteTeste() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        this.dataSource = ds;
        new ResourceDatabasePopulator(false, false, StandardCharsets.UTF_8.name(),
                new ClassPathResource("db/migration/V1__esquema_inicial.sql"),
                new ClassPathResource("db/migration/V2__conteudo_inicial.sql"),
                new ClassPathResource("db/migration/V3__exercicios_adicionais.sql"),
                new ClassPathResource("db/migration/V4__avatar.sql"),
                new ClassPathResource("db/migration/V5__administracao.sql")).execute(ds);

        this.jdbc = new JdbcTemplate(ds);
        this.usuarios = new UsuarioRepositorio(jdbc);
        this.sessoes = new SessaoRepositorio(jdbc);
        ModuloRepositorio modulos = new ModuloRepositorio(jdbc);
        ProgressoRepositorio progressos = new ProgressoRepositorio(jdbc);
        ServicoAvatar avatares = new ServicoAvatar(usuarios);
        ServicoAutenticacao autenticacao = new ServicoAutenticacao(usuarios, sessoes, hashSenha, avatares, 12);
        ServicoResposta resposta = new ServicoResposta(modulos, progressos, usuarios, new ServicoGamificacao(),
                new DataSourceTransactionManager(ds));
        this.mvc = MockMvcBuilders
                .standaloneSetup(new LoginController(autenticacao, avatares),
                        new ModuloController(new ServicoModulo(modulos, progressos), resposta),
                        new AdminController(new ServicoAdmin(new ConteudoRepositorio(jdbc), usuarios, new DataSourceTransactionManager(ds))))
                .setControllerAdvice(new TratadorErros())
                .setCustomArgumentResolvers(new ResolvedorUsuarioAutenticado(autenticacao))
                .build();
    }

    Usuario criarUsuario(String nome, String senha, algoquest.model.Papel papel) {
        Usuario u = criarUsuario(nome, senha);
        usuarios.atualizarPapel(u.getId(), papel);
        u.setPapel(papel);
        return u;
    }

    Usuario criarUsuario(String nome, String senha) {
        Usuario u = new Usuario(0, nome, hashSenha.gerar(senha));
        usuarios.salvar(u);
        return u;
    }

    int indiceCorreto(int idExercicio) {
        return jdbc.queryForObject("SELECT indice_correto FROM exercicio_multipla_escolha WHERE exercicio_id = ?",
                Integer.class, idExercicio);
    }
}
