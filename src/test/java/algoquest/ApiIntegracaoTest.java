package algoquest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ApiIntegracaoTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private AmbienteTeste env;

    @BeforeEach
    void preparar() {
        env = new AmbienteTeste();
        env.criarUsuario("nygel", "senha-forte-123");
        env.criarUsuario("ana", "outra-senha-456");
    }

    private record Resp(int status, JsonNode corpo, String texto) {}

    private Resp executar(MockHttpServletRequestBuilder req) throws Exception {
        MockHttpServletResponse r = env.mvc.perform(req).andReturn().getResponse();
        String texto = new String(r.getContentAsByteArray(), StandardCharsets.UTF_8);
        return new Resp(r.getStatus(), texto.isBlank() ? null : JSON.readTree(texto), texto);
    }
    private Resp login(String nome, String senha) throws Exception {
        return executar(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(java.util.Map.of("nomeUsuario", nome, "senha", senha))));
    }
    private String token(String nome, String senha) throws Exception {
        return login(nome, senha).corpo().get("token").asText();
    }
    private Resp getAuth(String url, String token) throws Exception {
        MockHttpServletRequestBuilder b = get(url);
        return executar(token == null ? b : b.header("Authorization", "Bearer " + token));
    }
    private Resp responder(int idExercicio, String corpoJson, String token) throws Exception {
        MockHttpServletRequestBuilder b = post("/api/modulos/exercicios/" + idExercicio + "/resposta")
                .contentType(MediaType.APPLICATION_JSON).content(corpoJson);
        return executar(token == null ? b : b.header("Authorization", "Bearer " + token));
    }
    private Resp responderIndice(int idExercicio, int indice, String token) throws Exception {
        return responder(idExercicio, "{\"resposta\":" + indice + "}", token);
    }
    private List<Integer> idsDoModulo(int idModulo, String token) throws Exception {
        List<Integer> ids = new ArrayList<>();
        getAuth("/api/modulos/" + idModulo, token).corpo().get("exercicios").forEach(e -> ids.add(e.get("id").asInt()));
        return ids;
    }
    private int primeiroModulo(String token) throws Exception {
        return getAuth("/api/modulos", token).corpo().get(0).get("id").asInt();
    }

    @Test
    void loginValidoDevolveOUsuarioNoContratoDoFrontendSemSenhaNemHash() throws Exception {
        Resp r = login("nygel", "senha-forte-123");
        assertEquals(200, r.status());
        JsonNode u = r.corpo();
        assertEquals("nygel", u.get("nomeUsuario").asText());
        assertEquals(0, u.get("pontuacao").asInt());
        assertEquals(0, u.get("xp").asInt());
        assertEquals(1, u.get("nivel").asInt());
        assertEquals(100, u.get("xpProximoNivel").asInt());
        assertTrue(u.get("token").asText().length() >= 40);
        assertTrue(u.get("id").asInt() > 0);
        assertFalse(r.texto().toLowerCase().contains("senha"), "resposta nao pode conter senha/hash: " + r.texto());
        assertFalse(r.texto().contains("pbkdf2"));
        assertNull(u.get("pontos"), "o contrato usa 'pontuacao', nao 'pontos'");
    }

    @Test
    void credenciaisInvalidasRetornam401ComMensagemGenerica() throws Exception {
        Resp senhaErrada = login("nygel", "errada");
        Resp usuarioInexistente = login("fantasma", "qualquer-coisa");
        assertEquals(401, senhaErrada.status());
        assertEquals(401, usuarioInexistente.status());
        assertEquals(senhaErrada.corpo().get("erro").asText(), usuarioInexistente.corpo().get("erro").asText());
        assertNull(senhaErrada.corpo().get("token"));
    }

    @Test
    void camposAusentesOuCorpoInvalidoRetornam400() throws Exception {
        assertEquals(400, executar(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}")).status());
        assertEquals(400, login("  ", "x").status());
        assertEquals(400, login("nygel", "").status());
        assertEquals(400, executar(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{nao-e-json")).status());
        assertEquals(400, executar(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)).status());
    }

    @Test
    void injecaoDeSqlNoLoginNaoAutenticaNemQuebra() throws Exception {
        assertEquals(401, login("nygel' OR '1'='1", "x' OR '1'='1").status());
        assertEquals(401, login("' OR 1=1 --", "qualquer").status());
        assertEquals(1, env.jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE nome_usuario = 'nygel'", Integer.class));
    }

    @Test
    void meExigeTokenEDevolveDadosAtuais() throws Exception {
        String t = token("nygel", "senha-forte-123");
        Resp r = getAuth("/api/auth/me", t);
        assertEquals(200, r.status());
        assertEquals("nygel", r.corpo().get("nomeUsuario").asText());
        assertNull(r.corpo().get("token"), "/me nao reemite o token");
        assertEquals(401, getAuth("/api/auth/me", null).status());
        assertEquals(401, getAuth("/api/auth/me", "token-inventado").status());
    }

    @Test
    void logoutInvalidaOTokenDeVerdadeEEIdempotente() throws Exception {
        String t = token("nygel", "senha-forte-123");
        assertEquals(200, getAuth("/api/modulos", t).status());
        assertEquals(204, executar(post("/api/auth/logout").header("Authorization", "Bearer " + t)).status());
        assertEquals(401, getAuth("/api/modulos", t).status(), "token deve parar de valer no servidor");
        assertEquals(0, env.jdbc.queryForObject("SELECT COUNT(*) FROM sessao", Integer.class));
        assertEquals(204, executar(post("/api/auth/logout").header("Authorization", "Bearer " + t)).status());
        assertEquals(204, executar(post("/api/auth/logout")).status());
    }

    @Test
    void logoutEncerraSomenteASessaoInformada() throws Exception {
        String a = token("nygel", "senha-forte-123"), b = token("nygel", "senha-forte-123");
        executar(post("/api/auth/logout").header("Authorization", "Bearer " + a));
        assertEquals(401, getAuth("/api/auth/me", a).status());
        assertEquals(200, getAuth("/api/auth/me", b).status());
    }

    @Test
    void sessaoExpiradaRetorna401() throws Exception {
        String t = token("nygel", "senha-forte-123");
        env.jdbc.update("UPDATE sessao SET expira_em = ?", Timestamp.from(Instant.now().minusSeconds(60)));
        assertEquals(401, getAuth("/api/modulos", t).status());
    }

    @Test
    void tokenNoBancoEstaSomenteComoHash() throws Exception {
        String t = token("nygel", "senha-forte-123");
        List<String> guardados = env.jdbc.queryForList("SELECT token_hash FROM sessao", String.class);
        assertEquals(1, guardados.size());
        assertNotEquals(t, guardados.get(0));
        assertEquals(64, guardados.get(0).length());
    }

    @Test
    void senhaNuncaFicaEmTextoPuroNoBanco() {
        List<String> hashes = env.jdbc.queryForList("SELECT senha_hash FROM usuario", String.class);
        assertEquals(2, hashes.size());
        for (String h : hashes) {
            assertTrue(h.startsWith("pbkdf2_sha256$"));
            assertFalse(h.contains("senha-forte-123") || h.contains("outra-senha-456"));
        }
    }

    @Test
    void endpointsDeModulosExigemAutenticacao() throws Exception {
        assertEquals(401, getAuth("/api/modulos", null).status());
        assertEquals(401, getAuth("/api/modulos/1", null).status());
        assertEquals(401, responderIndice(1, 0, null).status());
        assertEquals(401, getAuth("/api/modulos", "invalido").status());
        Resp semBearer = executar(get("/api/modulos").header("Authorization", "Basic abc"));
        assertEquals(401, semBearer.status());
    }

    private Resp postJson(String url, Object corpo, String token) throws Exception {
        MockHttpServletRequestBuilder b = post(url).contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(corpo));
        return executar(token == null ? b : b.header("Authorization", "Bearer " + token));
    }

    @Test
    void cadastroSorteiaAvatarPorFinalDoNomeESoCriaUmaVez() throws Exception {
        Resp f = postJson("/api/auth/registrar", java.util.Map.of("nomeUsuario", "Marcia", "senha", "senha-forte-1"), null);
        assertEquals(201, f.status());
        assertTrue(java.util.List.of("folk_03_innkeeper", "folk_07_village_child", "folk_11_baker", "folk_15_noble_lady")
                .contains(f.corpo().get("avatar").asText()));
        Resp m = postJson("/api/auth/registrar", java.util.Map.of("nomeUsuario", "Pedro", "senha", "senha-forte-1"), null);
        assertFalse(m.corpo().get("avatar").asText().startsWith("monster"));
        assertEquals(409, postJson("/api/auth/registrar", java.util.Map.of("nomeUsuario", "Marcia", "senha", "senha-forte-1"), null).status());
        assertEquals(400, postJson("/api/auth/registrar", java.util.Map.of("nomeUsuario", "x", "senha", "senha-forte-1"), null).status());
        assertEquals(400, postJson("/api/auth/registrar", java.util.Map.of("nomeUsuario", "Valido", "senha", "curta"), null).status());
        String antes = f.corpo().get("avatar").asText();
        assertEquals(antes, login("Marcia", "senha-forte-1").corpo().get("avatar").asText());
    }

    @Test
    void contaAntigaRecebePadraoUmaUnicaVezNoLogin() throws Exception {
        String a1 = login("nygel", "senha-forte-123").corpo().get("avatar").asText();
        assertFalse(a1.isBlank());
        assertEquals(a1, login("nygel", "senha-forte-123").corpo().get("avatar").asText());
    }

    @Test
    void escolherAvatarValidaIdEAutenticacaoEPersiste() throws Exception {
        String t = token("nygel", "senha-forte-123");
        assertEquals(401, postJson("/api/auth/me/avatar", java.util.Map.of("avatar", "hero_01_knight"), null).status());
        assertEquals(400, postJson("/api/auth/me/avatar", java.util.Map.of("avatar", "../../x"), t).status());
        assertEquals(200, postJson("/api/auth/me/avatar", java.util.Map.of("avatar", "monster_07_dragon"), t).status());
        assertEquals("monster_07_dragon", getAuth("/api/auth/me", t).corpo().get("avatar").asText());
        assertEquals("monster_07_dragon", login("nygel", "senha-forte-123").corpo().get("avatar").asText());
        assertNotEquals("monster_07_dragon", login("ana", "outra-senha-456").corpo().get("avatar").asText());
    }

    @Test
    void imagemPersonalizadaEValidadaEPersistida() throws Exception {
        String t = token("nygel", "senha-forte-123");
        byte[] jpg = new byte[300]; jpg[0] = (byte) 0xFF; jpg[1] = (byte) 0xD8; jpg[2] = (byte) 0xFF;
        String ok = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(jpg);
        assertEquals(200, postJson("/api/auth/me/avatar/personalizado", java.util.Map.of("imagem", ok), t).status());
        JsonNode me = getAuth("/api/auth/me", t).corpo();
        assertEquals("personalizado", me.get("avatar").asText());
        assertEquals(ok, me.get("avatarImagem").asText());
        assertEquals(400, postJson("/api/auth/me/avatar/personalizado", java.util.Map.of("imagem", "data:image/svg+xml;base64,PHN2Zz4="), t).status());
        assertEquals(400, postJson("/api/auth/me/avatar/personalizado", java.util.Map.of("imagem", "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(new byte[300])), t).status());
        postJson("/api/auth/me/avatar", java.util.Map.of("avatar", "hero_01_knight"), t);
        assertFalse(getAuth("/api/auth/me", t).corpo().has("avatarImagem"));
    }

    private Resp enviar(org.springframework.http.HttpMethod metodo, String url, Object corpo, String token) throws Exception {
        MockHttpServletRequestBuilder b = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(metodo, url)
                .contentType(MediaType.APPLICATION_JSON).content(corpo == null ? "{}" : JSON.writeValueAsString(corpo));
        return executar(token == null ? b : b.header("Authorization", "Bearer " + token));
    }

    private String tokenComPapel(String nome, algoquest.model.Papel papel) throws Exception {
        env.criarUsuario(nome, "senha-adm-12345", papel);
        return token(nome, "senha-adm-12345");
    }

    private static java.util.Map<String, Object> questao(String desc) {
        return java.util.Map.of("descricao", desc, "dificuldade", "FACIL", "opcoes", java.util.List.of("Sim", "Não"), "indiceCorreto", 0);
    }

    @Test
    void permissoesDeAdministracaoSaoAplicadasNoServidor() throws Exception {
        String comum = token("nygel", "senha-forte-123");
        String admin = tokenComPapel("adm", algoquest.model.Papel.ADMIN);
        String master = tokenComPapel("mestre", algoquest.model.Papel.MASTER);
        var GET = org.springframework.http.HttpMethod.GET;
        assertEquals(401, enviar(GET, "/api/admin/modulos", null, null).status());
        assertEquals(403, enviar(GET, "/api/admin/modulos", null, comum).status());
        assertEquals(403, enviar(org.springframework.http.HttpMethod.POST, "/api/admin/modulos", java.util.Map.of("titulo", "Teste", "descricao", "d"), comum).status());
        assertEquals(200, enviar(GET, "/api/admin/modulos", null, admin).status());
        assertEquals(200, enviar(GET, "/api/admin/modulos", null, master).status());
        assertEquals(403, enviar(GET, "/api/admin/usuarios", null, admin).status());
        assertEquals(403, enviar(GET, "/api/admin/usuarios", null, comum).status());
        assertEquals(200, enviar(GET, "/api/admin/usuarios", null, master).status());
        assertFalse(enviar(GET, "/api/admin/usuarios", null, master).texto().contains("senha"));
    }

    @Test
    void somenteMasterAlteraPapeisEComProtecoes() throws Exception {
        String admin = tokenComPapel("adm", algoquest.model.Papel.ADMIN);
        String master = tokenComPapel("mestre", algoquest.model.Papel.MASTER);
        var PUT = org.springframework.http.HttpMethod.PUT;
        int idAna = 0, idMestre = 0, idAdm = 0;
        for (JsonNode u : enviar(org.springframework.http.HttpMethod.GET, "/api/admin/usuarios", null, master).corpo()) {
            switch (u.get("nomeUsuario").asText()) { case "ana" -> idAna = u.get("id").asInt(); case "mestre" -> idMestre = u.get("id").asInt(); case "adm" -> idAdm = u.get("id").asInt(); default -> { } }
        }
        assertEquals(403, enviar(PUT, "/api/admin/usuarios/" + idAna + "/papel", java.util.Map.of("papel", "ADMIN"), admin).status());
        assertEquals(200, enviar(PUT, "/api/admin/usuarios/" + idAna + "/papel", java.util.Map.of("papel", "ADMIN"), master).status());
        assertEquals("ADMIN", login("ana", "outra-senha-456").corpo().get("papel").asText());
        assertEquals(200, enviar(PUT, "/api/admin/usuarios/" + idAna + "/papel", java.util.Map.of("papel", "USUARIO"), master).status());
        assertEquals(403, enviar(PUT, "/api/admin/usuarios/" + idMestre + "/papel", java.util.Map.of("papel", "USUARIO"), master).status());
        assertEquals(403, enviar(PUT, "/api/admin/usuarios/" + idAna + "/papel", java.util.Map.of("papel", "MASTER"), master).status());
        assertEquals(400, enviar(PUT, "/api/admin/usuarios/" + idAna + "/papel", java.util.Map.of("papel", "REI"), master).status());
        assertEquals(404, enviar(PUT, "/api/admin/usuarios/99999/papel", java.util.Map.of("papel", "ADMIN"), master).status());
        enviar(PUT, "/api/admin/usuarios/" + idAdm + "/papel", java.util.Map.of("papel", "USUARIO"), master);
        assertEquals(403, enviar(org.springframework.http.HttpMethod.GET, "/api/admin/modulos", null, admin).status());
    }

    @Test
    void administradorGerenciaModulosEQuestoesEJogadorVeNoApp() throws Exception {
        String admin = tokenComPapel("adm", algoquest.model.Papel.ADMIN), comum = token("nygel", "senha-forte-123");
        var POST = org.springframework.http.HttpMethod.POST;
        Resp m = enviar(POST, "/api/admin/modulos", java.util.Map.of("titulo", "Grafos", "descricao", "BFS e DFS", "tema", "hash"), admin);
        assertEquals(201, m.status());
        int id = m.corpo().get("id").asInt();
        assertEquals(400, enviar(POST, "/api/admin/modulos", java.util.Map.of("titulo", "G", "descricao", "d"), admin).status());
        assertEquals(400, enviar(POST, "/api/admin/modulos", java.util.Map.of("titulo", "Valido", "descricao", "d", "tema", "ninja"), admin).status());
        Resp q = enviar(POST, "/api/admin/modulos/" + id + "/questoes", questao("Qual estrutura a BFS usa?"), admin);
        assertEquals(201, q.status());
        assertEquals(409, enviar(POST, "/api/admin/modulos/" + id + "/questoes", questao("  qual ESTRUTURA a bfs usa? "), admin).status());
        JsonNode pub = getAuth("/api/modulos/" + id, comum).corpo();
        assertEquals("hash", pub.get("tema").asText());
        assertEquals(1, pub.get("exercicios").size());
        assertFalse(pub.toString().contains("indiceCorreto"));
        int idQ = q.corpo().get("id").asInt();
        assertEquals(200, enviar(org.springframework.http.HttpMethod.PUT, "/api/admin/questoes/" + idQ, java.util.Map.of("descricao", "Qual estrutura a BFS usa agora?", "dificuldade", "MEDIO", "pontos", 25,
                "opcoes", java.util.List.of("Pilha", "Fila"), "indiceCorreto", 1, "explicacao", "Fila."), admin).status());
        assertEquals(200, responderIndice(idQ, 1, comum).status());
        assertEquals(204, enviar(org.springframework.http.HttpMethod.DELETE, "/api/admin/questoes/" + idQ, null, admin).status());
        assertEquals(204, enviar(org.springframework.http.HttpMethod.DELETE, "/api/admin/modulos/" + id, null, admin).status());
        assertEquals(404, getAuth("/api/modulos/" + id, comum).status());
        assertEquals(403, enviar(org.springframework.http.HttpMethod.DELETE, "/api/admin/modulos/" + primeiroModulo(comum), null, comum).status());
    }

    @Test
    void visualDeQuestaoRejeitaSvgMalicioso() throws Exception {
        String admin = tokenComPapel("adm", algoquest.model.Papel.ADMIN);
        int id = enviar(org.springframework.http.HttpMethod.POST, "/api/admin/modulos", java.util.Map.of("titulo", "Visuais", "descricao", "d"), admin).corpo().get("id").asInt();
        String[] ruins = {"<svg><script>alert(1)</script></svg>", "<svg onload=\"x()\"></svg>", "<svg><image href=\"http://evil/x.png\"/></svg>", "<svg><foreignObject/></svg>"};
        for (String v : ruins) {
            java.util.Map<String, Object> q = new java.util.HashMap<>(questao("Questão com visual " + v.length()));
            q.put("visual", v);
            assertEquals(400, enviar(org.springframework.http.HttpMethod.POST, "/api/admin/modulos/" + id + "/questoes", q, admin).status(), v);
        }
        java.util.Map<String, Object> ok = new java.util.HashMap<>(questao("Questão com visual válido"));
        ok.put("visual", "<svg viewBox=\"0 0 10 10\"><circle cx=\"5\" cy=\"5\" r=\"4\"/></svg>");
        Resp r = enviar(org.springframework.http.HttpMethod.POST, "/api/admin/modulos/" + id + "/questoes", ok, admin);
        assertEquals(201, r.status());
        assertTrue(r.corpo().get("visual").asText().contains("xmlns"));
    }

    @Test
    void importacaoEAtomicaValidaDuplicatasEModuloDeDestino() throws Exception {
        String admin = tokenComPapel("adm", algoquest.model.Papel.ADMIN), comum = token("nygel", "senha-forte-123");
        var POST = org.springframework.http.HttpMethod.POST;
        int id = enviar(POST, "/api/admin/modulos", java.util.Map.of("titulo", "Importados", "descricao", "d"), admin).corpo().get("id").asInt();
        String url = "/api/admin/modulos/" + id + "/importacao";
        java.util.List<Object> boas = java.util.List.of(questao("Primeira pergunta importada"), questao("Segunda pergunta importada"), questao("primeira  PERGUNTA importada"));
        Resp prev = enviar(POST, url, java.util.Map.of("questoes", boas, "confirmar", false), admin);
        assertEquals(2, prev.corpo().get("validas").asInt());
        assertEquals(1, prev.corpo().get("duplicadas").asInt());
        assertFalse(prev.corpo().get("importado").asBoolean());
        assertEquals(0, enviar(org.springframework.http.HttpMethod.GET, "/api/admin/modulos/" + id + "/questoes", null, admin).corpo().size());
        java.util.List<Object> comErro = java.util.List.of(questao("Pergunta valida que nao deve entrar"), java.util.Map.of("descricao", "x", "dificuldade", "FACIL", "opcoes", java.util.List.of("a"), "indiceCorreto", 3));
        Resp rr = enviar(POST, url, java.util.Map.of("questoes", comErro, "confirmar", true), admin);
        assertFalse(rr.corpo().get("importado").asBoolean());
        assertEquals(1, rr.corpo().get("erros").size());
        assertEquals(0, enviar(org.springframework.http.HttpMethod.GET, "/api/admin/modulos/" + id + "/questoes", null, admin).corpo().size());
        assertFalse(enviar(POST, url, java.util.Map.of("modulo", "Outro", "questoes", boas, "confirmar", true), admin).corpo().get("importado").asBoolean());
        Resp ok = enviar(POST, url, java.util.Map.of("modulo", "Importados", "questoes", boas, "confirmar", true), admin);
        assertTrue(ok.corpo().get("importado").asBoolean());
        assertEquals(2, ok.corpo().get("importadas").asInt());
        assertEquals(2, getAuth("/api/modulos/" + id, comum).corpo().get("exercicios").size());
        assertEquals(403, enviar(POST, url, java.util.Map.of("questoes", boas, "confirmar", true), comum).status());
        assertEquals(404, enviar(POST, "/api/admin/modulos/99999/importacao", java.util.Map.of("questoes", boas), admin).status());
    }

    @Test
    void listaModulosDoBancoNoContratoDoFrontend() throws Exception {
        String t = token("nygel", "senha-forte-123");
        Resp r = getAuth("/api/modulos", t);
        assertEquals(200, r.status());
        assertEquals(5, r.corpo().size());
        JsonNode m = r.corpo().get(0);
        assertEquals("Análise de Algoritmos", m.get("titulo").asText());
        assertEquals(10, m.get("quantidadeExercicios").asInt());
        assertEquals(0.0, m.get("porcentagem").asDouble());
        assertFalse(m.get("descricao").asText().isBlank());
        assertTrue(m.get("id").isInt());
    }

    @Test
    void moduloInativoNaoApareceNemAbre() throws Exception {
        String t = token("nygel", "senha-forte-123");
        int id = primeiroModulo(t);
        env.jdbc.update("UPDATE modulo SET ativo = FALSE WHERE id = ?", id);
        assertEquals(4, getAuth("/api/modulos", t).corpo().size());
        assertEquals(404, getAuth("/api/modulos/" + id, t).status());
        int ex = env.jdbc.queryForObject("SELECT MIN(id) FROM exercicio WHERE modulo_id = ?", Integer.class, id);
        assertEquals(404, responderIndice(ex, 0, t).status());
    }

    @Test
    void moduloDetalhadoTemExerciciosSemExporRespostaCorreta() throws Exception {
        String t = token("nygel", "senha-forte-123");
        int id = primeiroModulo(t);
        Resp r = getAuth("/api/modulos/" + id, t);
        assertEquals(200, r.status());
        JsonNode ex = r.corpo().get("exercicios");
        assertEquals(10, ex.size());
        for (JsonNode e : ex) {
            assertTrue(e.get("id").isInt());
            assertFalse(e.get("descricao").asText().isBlank());
            assertEquals(4, e.get("opcoes").size());
            assertTrue(List.of("FACIL", "MEDIO", "DIFICIL").contains(e.get("dificuldade").asText()));
            assertTrue(e.get("pontos").asInt() > 0);
            assertEquals(5, e.size(), "so id, descricao, opcoes, dificuldade, pontos: " + e);
        }
        String bruto = r.texto().toLowerCase();
        assertFalse(bruto.contains("correto") || bruto.contains("indice") || bruto.contains("feedback"), bruto);
        assertEquals(10, ex.get(0).get("pontos").asInt());
        assertEquals("FACIL", ex.get(0).get("dificuldade").asText());
    }

    @Test
    void moduloInexistenteRetorna404EIdInvalidoRetorna400() throws Exception {
        String t = token("nygel", "senha-forte-123");
        assertEquals(404, getAuth("/api/modulos/99999", t).status());
        assertEquals(404, getAuth("/api/modulos/0", t).status());
        assertEquals(404, getAuth("/api/modulos/-5", t).status());
        assertEquals(400, getAuth("/api/modulos/abc", t).status());
        assertEquals(400, executar(get("/api/modulos/{id}", "1 OR 1=1").header("Authorization", "Bearer " + t)).status());
        assertEquals(400, executar(get("/api/modulos/{id}", "1'; DROP TABLE usuario; --").header("Authorization", "Bearer " + t)).status());
        assertEquals(2, env.jdbc.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class));
    }

    @Test
    void respostaCorretaPersisteProgressoPontosEXp() throws Exception {
        String t = token("nygel", "senha-forte-123");
        int mod = primeiroModulo(t);
        int ex = idsDoModulo(mod, t).get(0);
        Resp r = responderIndice(ex, env.indiceCorreto(ex), t);
        assertEquals(200, r.status());
        assertTrue(r.corpo().get("correta").asBoolean());
        assertFalse(r.corpo().get("feedback").asText().isBlank());
        assertEquals(10, r.corpo().get("pontosGanhos").asInt());
        assertEquals(10, r.corpo().get("usuario").get("pontuacao").asInt());
        assertEquals(10, r.corpo().get("usuario").get("xp").asInt());
        assertEquals(1, env.jdbc.queryForObject("SELECT COUNT(*) FROM progresso_usuario", Integer.class));
        JsonNode novo = login("nygel", "senha-forte-123").corpo();
        assertEquals(10, novo.get("pontuacao").asInt());
        assertEquals(10, novo.get("xp").asInt());
        assertEquals(10.0, getAuth("/api/modulos", t).corpo().get(0).get("porcentagem").asDouble());
    }

    @Test
    void respostaErradaNaoPremiaENaoVazaFeedback() throws Exception {
        String t = token("nygel", "senha-forte-123");
        int ex = idsDoModulo(primeiroModulo(t), t).get(0);
        int errado = (env.indiceCorreto(ex) + 1) % 4;
        Resp r = responderIndice(ex, errado, t);
        assertEquals(200, r.status());
        assertFalse(r.corpo().get("correta").asBoolean());
        assertNull(r.corpo().get("feedback"));
        assertEquals(0, r.corpo().get("pontosGanhos").asInt());
        assertEquals(0, env.jdbc.queryForObject("SELECT COUNT(*) FROM progresso_usuario", Integer.class));
        assertEquals(0, getAuth("/api/auth/me", t).corpo().get("xp").asInt());
    }

    @Test
    void reenvioDoMesmoExercicioNaoPremiaDeNovo() throws Exception {
        String t = token("nygel", "senha-forte-123");
        int ex = idsDoModulo(primeiroModulo(t), t).get(0);
        Resp primeira = responderIndice(ex, env.indiceCorreto(ex), t);
        Resp segunda = responderIndice(ex, env.indiceCorreto(ex), t);
        Resp terceira = responderIndice(ex, env.indiceCorreto(ex), t);
        assertEquals(10, primeira.corpo().get("pontosGanhos").asInt());
        for (Resp r : List.of(segunda, terceira)) {
            assertEquals(200, r.status());
            assertTrue(r.corpo().get("correta").asBoolean());
            assertEquals(0, r.corpo().get("pontosGanhos").asInt());
            assertEquals(10, r.corpo().get("usuario").get("pontuacao").asInt());
            assertEquals(10, r.corpo().get("usuario").get("xp").asInt());
        }
        assertEquals(1, env.jdbc.queryForObject("SELECT COUNT(*) FROM progresso_usuario", Integer.class));
    }

    @Test
    void envioSimultaneoDoMesmoExercicioPremiaExatamenteUmaVez() throws Exception {
        String t = token("nygel", "senha-forte-123");
        int ex = idsDoModulo(primeiroModulo(t), t).get(0);
        int certo = env.indiceCorreto(ex);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<Integer>> futuros = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                Callable<Integer> c = () -> responderIndice(ex, certo, t).corpo().get("pontosGanhos").asInt();
                futuros.add(pool.submit(c));
            }
            int totalGanho = 0;
            for (Future<Integer> f : futuros) totalGanho += f.get();
            assertEquals(10, totalGanho, "so uma das 16 requisicoes pode ter premiado");
        } finally {
            pool.shutdown();
        }
        assertEquals(1, env.jdbc.queryForObject("SELECT COUNT(*) FROM progresso_usuario", Integer.class));
        assertEquals(10, env.jdbc.queryForObject("SELECT xp FROM usuario WHERE nome_usuario = 'nygel'", Integer.class));
    }

    @Test
    void respostasInvalidasRetornam400SemAlterarNada() throws Exception {
        String t = token("nygel", "senha-forte-123");
        int ex = idsDoModulo(primeiroModulo(t), t).get(0);
        for (String corpo : List.of("{\"resposta\":-1}", "{\"resposta\":4}", "{\"resposta\":99}", "{\"resposta\":\"abc\"}",
                "{\"resposta\":true}", "{\"resposta\":1.5}", "{\"resposta\":null}", "{}", "{\"resposta\":[1]}", "{\"resposta\":99999999999}")) {
            assertEquals(400, responder(ex, corpo, t).status(), corpo);
        }
        assertEquals(400, executar(post("/api/modulos/exercicios/" + ex + "/resposta").header("Authorization", "Bearer " + t)
                .contentType(MediaType.APPLICATION_JSON).content("nao json")).status());
        assertEquals(0, env.jdbc.queryForObject("SELECT COUNT(*) FROM progresso_usuario", Integer.class));
        assertEquals(0, env.jdbc.queryForObject("SELECT xp FROM usuario WHERE nome_usuario = 'nygel'", Integer.class));
    }

    @Test
    void exercicioInexistenteRetorna404EIdInvalidoRetorna400() throws Exception {
        String t = token("nygel", "senha-forte-123");
        assertEquals(404, responderIndice(987654, 0, t).status());
        assertEquals(400, executar(post("/api/modulos/exercicios/abc/resposta").header("Authorization", "Bearer " + t)
                .contentType(MediaType.APPLICATION_JSON).content("{\"resposta\":0}")).status());
    }

    @Test
    void respostaEnviadaComoTextoNumericoTambemFunciona() throws Exception {
        String t = token("nygel", "senha-forte-123");
        int ex = idsDoModulo(primeiroModulo(t), t).get(0);
        Resp r = responder(ex, "{\"resposta\":\"" + env.indiceCorreto(ex) + "\"}", t);
        assertEquals(200, r.status());
        assertTrue(r.corpo().get("correta").asBoolean());
    }

    @Test
    void progressaoEIsoladaPorUsuarioESobeDeNivel() throws Exception {
        String nygel = token("nygel", "senha-forte-123"), ana = token("ana", "outra-senha-456");
        int mod1 = getAuth("/api/modulos", nygel).corpo().get(0).get("id").asInt();
        int mod2 = getAuth("/api/modulos", nygel).corpo().get(1).get("id").asInt();
        for (int ex : idsDoModulo(mod1, nygel).subList(0, 5)) responderIndice(ex, env.indiceCorreto(ex), nygel);
        JsonNode apos90 = getAuth("/api/auth/me", nygel).corpo();
        assertEquals(90, apos90.get("xp").asInt());
        assertEquals(1, apos90.get("nivel").asInt());
        int ex2 = idsDoModulo(mod2, nygel).get(0);
        Resp r = responderIndice(ex2, env.indiceCorreto(ex2), nygel);
        assertEquals(100, r.corpo().get("usuario").get("xp").asInt());
        assertEquals(100, r.corpo().get("usuario").get("pontuacao").asInt());
        assertEquals(2, r.corpo().get("usuario").get("nivel").asInt());
        assertEquals(200, r.corpo().get("usuario").get("xpProximoNivel").asInt());

        JsonNode listaNygel = getAuth("/api/modulos", nygel).corpo(), listaAna = getAuth("/api/modulos", ana).corpo();
        assertEquals(50.0, listaNygel.get(0).get("porcentagem").asDouble());
        assertEquals(10.0, listaNygel.get(1).get("porcentagem").asDouble());
        assertEquals(0.0, listaAna.get(0).get("porcentagem").asDouble());
        JsonNode meAna = getAuth("/api/auth/me", ana).corpo();
        assertEquals(0, meAna.get("xp").asInt());
        assertEquals(1, meAna.get("nivel").asInt());
        JsonNode novo = login("nygel", "senha-forte-123").corpo();
        assertEquals(100, novo.get("xp").asInt());
        assertEquals(2, novo.get("nivel").asInt());
    }

    @Test
    void erroInternoNaoVazaDetalhes() throws Exception {
        String t = token("nygel", "senha-forte-123");
        env.jdbc.execute("DROP TABLE progresso_usuario CASCADE");
        Resp r = getAuth("/api/modulos", t);
        assertEquals(500, r.status());
        String bruto = r.texto().toLowerCase();
        assertFalse(bruto.contains("sql") || bruto.contains("exception") || bruto.contains("progresso_usuario"), bruto);
    }
}
