package algoquest.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import algoquest.dto.AdminDtos.AdminModuloResposta;
import algoquest.dto.AdminDtos.AdminQuestaoResposta;
import algoquest.dto.AdminDtos.AdminUsuarioResposta;
import algoquest.dto.AdminDtos.ErroImportacao;
import algoquest.dto.AdminDtos.ImportacaoRequisicao;
import algoquest.dto.AdminDtos.ImportacaoResposta;
import algoquest.dto.AdminDtos.ModuloRequisicao;
import algoquest.dto.QuestaoRequisicao;
import algoquest.exception.ErroApi;
import algoquest.model.Papel;
import algoquest.model.Usuario;
import algoquest.model.ValidadorConteudo;
import algoquest.repository.ConteudoRepositorio;
import algoquest.repository.UsuarioRepositorio;

@Service
public class ServicoAdmin {

    private final ConteudoRepositorio conteudo;
    private final UsuarioRepositorio usuarios;
    private final TransactionTemplate transacao;

    public ServicoAdmin(ConteudoRepositorio conteudo, UsuarioRepositorio usuarios, PlatformTransactionManager gerente) {
        this.conteudo = conteudo;
        this.usuarios = usuarios;
        this.transacao = new TransactionTemplate(gerente);
    }

    private static void exigirConteudo(Usuario ator) {
        if (!ator.getPapel().podeAdministrarConteudo()) throw ErroApi.proibido("Acesso restrito a administradores.");
    }

    private static void exigirMaster(Usuario ator) {
        if (!ator.getPapel().podeGerenciarUsuarios()) throw ErroApi.proibido("Acesso exclusivo do Master.");
    }

    private AdminModuloResposta modulo(int id) {
        AdminModuloResposta m = conteudo.buscarModulo(id);
        if (m == null) throw ErroApi.naoEncontrado("Módulo não encontrado.");
        return m;
    }

    private static QuestaoRequisicao validar(QuestaoRequisicao q) {
        try {
            return ValidadorConteudo.validarQuestao(q);
        } catch (IllegalArgumentException e) {
            throw ErroApi.requisicaoInvalida(e.getMessage());
        }
    }

    public List<AdminModuloResposta> listarModulos(Usuario ator) {
        exigirConteudo(ator);
        return conteudo.listarModulos();
    }

    public AdminModuloResposta criarModulo(Usuario ator, ModuloRequisicao r) {
        exigirConteudo(ator);
        String erro = r == null ? "Dados do módulo ausentes." : ValidadorConteudo.validarModulo(r.titulo(), r.descricao(), r.tema());
        if (erro != null) throw ErroApi.requisicaoInvalida(erro);
        int id = conteudo.inserirModulo(r.titulo().trim(), r.descricao().trim(), temaOuNulo(r.tema()), r.ativo() == null || r.ativo());
        return conteudo.buscarModulo(id);
    }

    public AdminModuloResposta editarModulo(Usuario ator, int id, ModuloRequisicao r) {
        exigirConteudo(ator);
        AdminModuloResposta atual = modulo(id);
        String erro = r == null ? "Dados do módulo ausentes." : ValidadorConteudo.validarModulo(r.titulo(), r.descricao(), r.tema());
        if (erro != null) throw ErroApi.requisicaoInvalida(erro);
        conteudo.atualizarModulo(id, r.titulo().trim(), r.descricao().trim(), temaOuNulo(r.tema()), r.ativo() == null ? atual.ativo() : r.ativo());
        return conteudo.buscarModulo(id);
    }

    public void excluirModulo(Usuario ator, int id) {
        exigirConteudo(ator);
        modulo(id);
        conteudo.excluirModulo(id);
    }

    public List<AdminModuloResposta> moverModulo(Usuario ator, int id, Integer direcao) {
        exigirConteudo(ator);
        modulo(id);
        transacao.executeWithoutResult(s -> {
            List<Integer> ids = new ArrayList<>(conteudo.idsModulosOrdenados());
            mover(ids, id, direcao);
            conteudo.gravarOrdemModulos(ids);
        });
        return conteudo.listarModulos();
    }

    private static String temaOuNulo(String t) {
        return t == null || t.isBlank() ? null : t;
    }

    private static void mover(List<Integer> ids, int id, Integer direcao) {
        if (direcao == null || (direcao != -1 && direcao != 1)) throw ErroApi.requisicaoInvalida("Direção inválida (use -1 ou 1).");
        int i = ids.indexOf(id), j = i + direcao;
        if (i < 0 || j < 0 || j >= ids.size()) return;
        ids.set(i, ids.get(j));
        ids.set(j, id);
    }

    public List<AdminQuestaoResposta> listarQuestoes(Usuario ator, int moduloId) {
        exigirConteudo(ator);
        modulo(moduloId);
        return conteudo.listarQuestoes(moduloId);
    }

    public AdminQuestaoResposta criarQuestao(Usuario ator, int moduloId, QuestaoRequisicao r) {
        exigirConteudo(ator);
        modulo(moduloId);
        QuestaoRequisicao q = validar(r);
        if (existentes(moduloId).contains(ValidadorConteudo.chave(q.descricao()))) {
            throw ErroApi.conflito("Já existe uma questão com este enunciado neste módulo.");
        }
        int id = transacao.execute(s -> conteudo.inserirQuestao(moduloId, q));
        return conteudo.buscarQuestao(id);
    }

    public AdminQuestaoResposta editarQuestao(Usuario ator, int id, QuestaoRequisicao r) {
        exigirConteudo(ator);
        AdminQuestaoResposta atual = conteudo.buscarQuestao(id);
        if (atual == null) throw ErroApi.naoEncontrado("Questão não encontrada.");
        QuestaoRequisicao q = validar(r);
        for (AdminQuestaoResposta o : conteudo.listarQuestoes(atual.moduloId())) {
            if (o.id() != id && ValidadorConteudo.chave(o.descricao()).equals(ValidadorConteudo.chave(q.descricao()))) {
                throw ErroApi.conflito("Já existe uma questão com este enunciado neste módulo.");
            }
        }
        transacao.executeWithoutResult(s -> conteudo.atualizarQuestao(id, q));
        return conteudo.buscarQuestao(id);
    }

    public void excluirQuestao(Usuario ator, int id) {
        exigirConteudo(ator);
        if (!conteudo.excluirQuestao(id)) throw ErroApi.naoEncontrado("Questão não encontrada.");
    }

    public List<AdminQuestaoResposta> moverQuestao(Usuario ator, int id, Integer direcao) {
        exigirConteudo(ator);
        AdminQuestaoResposta q = conteudo.buscarQuestao(id);
        if (q == null) throw ErroApi.naoEncontrado("Questão não encontrada.");
        transacao.executeWithoutResult(s -> {
            List<Integer> ids = new ArrayList<>(conteudo.idsQuestoesOrdenadas(q.moduloId()));
            mover(ids, id, direcao);
            conteudo.gravarOrdemQuestoes(ids);
        });
        return conteudo.listarQuestoes(q.moduloId());
    }

    private Set<String> existentes(int moduloId) {
        Set<String> s = new HashSet<>();
        for (String d : conteudo.enunciados(moduloId)) s.add(ValidadorConteudo.chave(d));
        return s;
    }

    public ImportacaoResposta importar(Usuario ator, int moduloId, ImportacaoRequisicao r) {
        exigirConteudo(ator);
        AdminModuloResposta alvo = modulo(moduloId);
        if (r == null || r.questoes() == null || r.questoes().isEmpty()) throw ErroApi.requisicaoInvalida("O arquivo não contém questões.");
        if (r.questoes().size() > ValidadorConteudo.MAX_QUESTOES_IMPORTACAO) {
            throw ErroApi.requisicaoInvalida("Máximo de " + ValidadorConteudo.MAX_QUESTOES_IMPORTACAO + " questões por importação.");
        }
        boolean confirmar = Boolean.TRUE.equals(r.confirmar());
        boolean ignorar = r.ignorarDuplicadas() == null || r.ignorarDuplicadas();
        List<ErroImportacao> erros = new ArrayList<>();
        if (r.modulo() != null && !r.modulo().isBlank()) {
            String m = r.modulo().trim();
            if (!m.equalsIgnoreCase(alvo.titulo()) && !m.equals(String.valueOf(alvo.id()))) {
                erros.add(new ErroImportacao(0, "O arquivo é do módulo \"" + m + "\", mas o destino escolhido é \"" + alvo.titulo() + "\"."));
            }
        }
        Set<String> vistas = existentes(moduloId);
        List<QuestaoRequisicao> aceitas = new ArrayList<>();
        int duplicadas = 0;
        for (int i = 0; i < r.questoes().size(); i++) {
            try {
                QuestaoRequisicao q = ValidadorConteudo.validarQuestao(r.questoes().get(i));
                if (!vistas.add(ValidadorConteudo.chave(q.descricao()))) {
                    duplicadas++;
                    if (!ignorar) erros.add(new ErroImportacao(i + 1, "Enunciado duplicado."));
                    continue;
                }
                aceitas.add(q);
            } catch (IllegalArgumentException e) {
                erros.add(new ErroImportacao(i + 1, e.getMessage()));
            }
        }
        List<AdminQuestaoResposta> previa = new ArrayList<>();
        for (QuestaoRequisicao q : aceitas) {
            previa.add(new AdminQuestaoResposta(0, moduloId, 0, q.descricao(), q.dificuldade(), q.pontos(), q.opcoes(),
                    q.indiceCorreto(), q.explicacao(), q.visual()));
        }
        boolean gravar = confirmar && erros.isEmpty() && !aceitas.isEmpty();
        if (gravar) {
            transacao.executeWithoutResult(s -> {
                for (QuestaoRequisicao q : aceitas) conteudo.inserirQuestao(moduloId, q);
            });
        }
        return new ImportacaoResposta(r.questoes().size(), aceitas.size(), duplicadas, gravar ? aceitas.size() : 0, gravar, erros, previa);
    }

    public List<AdminUsuarioResposta> listarUsuarios(Usuario ator) {
        exigirMaster(ator);
        return conteudo.listarUsuarios();
    }

    public AdminUsuarioResposta alterarPapel(Usuario ator, int idAlvo, String papelTexto) {
        exigirMaster(ator);
        Papel novo;
        try {
            novo = Papel.valueOf(papelTexto == null ? "" : papelTexto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ErroApi.requisicaoInvalida("Papel inválido (use USUARIO ou ADMIN).");
        }
        Usuario alvo = usuarios.buscarPorId(idAlvo);
        if (alvo == null) throw ErroApi.naoEncontrado("Usuário não encontrado.");
        String recusa = Papel.validarAlteracao(ator.getPapel(), ator.getId(), alvo.getPapel(), alvo.getId(), novo);
        if (recusa != null) throw ErroApi.proibido(recusa);
        usuarios.atualizarPapel(idAlvo, novo);
        return conteudo.listarUsuarios().stream().filter(u -> u.id() == idAlvo).findFirst().orElseThrow();
    }
}
