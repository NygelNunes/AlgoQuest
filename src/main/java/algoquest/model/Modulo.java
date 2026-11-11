package algoquest.model;

import java.util.ArrayList;
import java.util.List;

public class Modulo {

    private int id;
    private String titulo;
    private String descricao;
    private String tema;
    private List<Exercicio> exercicios;

    public Modulo(int id, String titulo, String descricao) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.exercicios = new ArrayList<>();
    }

    public void adicionarExercicio(Exercicio exercicio) {
        if (exercicio == null) {
            throw new IllegalArgumentException("Exercicio nao pode ser nulo");
        }
        this.exercicios.add(exercicio);
    }

    public int getQuantidadeExercicios() {
        return exercicios.size();
    }

    public double calcularPorcentagem(ProgressoUsuario progresso) {
        if (exercicios.isEmpty() || progresso == null) {
            return 0.0;
        }
        long concluidos = exercicios.stream().filter(e -> progresso.isConcluido(e.getId())).count();
        return concluidos * 100.0 / exercicios.size();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public List<Exercicio> getExercicios() { return exercicios; }
    public void setExercicios(List<Exercicio> exercicios) { this.exercicios = exercicios; }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
}
