package algoquest.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import algoquest.dto.QuestaoRequisicao;

public final class ValidadorConteudo {

    public static final Set<String> TEMAS = Set.of("arvore", "busca", "ordenacao", "hash", "analise", "padrao");
    public static final int MAX_QUESTOES_IMPORTACAO = 500;

    private ValidadorConteudo() {
    }

    public static String validarModulo(String titulo, String descricao, String tema) {
        if (titulo == null || titulo.trim().length() < 2 || titulo.trim().length() > 120) return "O título deve ter de 2 a 120 caracteres.";
        if (descricao == null || descricao.trim().isEmpty() || descricao.trim().length() > 500) return "A descrição é obrigatória (até 500 caracteres).";
        if (tema != null && !tema.isBlank() && !TEMAS.contains(tema)) return "Tema de ilustração desconhecido.";
        return null;
    }

    public static Dificuldade dificuldade(String texto) {
        try {
            return texto == null ? null : Dificuldade.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int pontosPadrao(Dificuldade d) {
        return switch (d) { case FACIL -> 10; case MEDIO -> 20; case DIFICIL -> 30; };
    }

    public static String chave(String descricao) {
        return descricao == null ? "" : descricao.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public static QuestaoRequisicao validarQuestao(QuestaoRequisicao q) {
        if (q == null) throw new IllegalArgumentException("Questão vazia.");
        String desc = q.descricao() == null ? "" : q.descricao().trim();
        if (desc.length() < 5 || desc.length() > 1000) throw new IllegalArgumentException("O enunciado deve ter de 5 a 1000 caracteres.");
        Dificuldade dif = dificuldade(q.dificuldade());
        if (dif == null) throw new IllegalArgumentException("Dificuldade inválida (use FACIL, MEDIO ou DIFICIL).");
        int pontos = q.pontos() == null ? pontosPadrao(dif) : q.pontos();
        if (pontos < 1 || pontos > 1000) throw new IllegalArgumentException("Pontos devem ficar entre 1 e 1000.");
        if (q.opcoes() == null || q.opcoes().size() < 2 || q.opcoes().size() > 6) throw new IllegalArgumentException("Informe de 2 a 6 alternativas.");
        List<String> ops = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        for (String o : q.opcoes()) {
            String t = o == null ? "" : o.trim();
            if (t.isEmpty() || t.length() > 500) throw new IllegalArgumentException("Cada alternativa deve ter de 1 a 500 caracteres.");
            if (!vistas.add(t.toLowerCase(Locale.ROOT))) throw new IllegalArgumentException("Há alternativas repetidas.");
            ops.add(t);
        }
        if (q.indiceCorreto() == null || q.indiceCorreto() < 0 || q.indiceCorreto() >= ops.size()) {
            throw new IllegalArgumentException("Indique a alternativa correta (índice dentro das alternativas).");
        }
        String exp = q.explicacao() == null || q.explicacao().isBlank() ? null : q.explicacao().trim();
        if (exp != null && exp.length() > 1000) throw new IllegalArgumentException("A explicação deve ter até 1000 caracteres.");
        return new QuestaoRequisicao(desc, dif.name(), pontos, ops, q.indiceCorreto(), exp, Visual.validar(q.visual()));
    }
}
