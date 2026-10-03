package algoquest.model;

import java.util.ArrayList;
import java.util.List;

public class Modulo {

    private int id;
    private String titulo;
    private String descricao;
    private List<Exercicio> exercicios;

    public Modulo() {
        this.exercicios = new ArrayList<>();
    }

    public Modulo(int id, String titulo, String descricao) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.exercicios = new ArrayList<>();
    }

    public Modulo(int id, String titulo, String descricao, List<Exercicio> exercicios) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.exercicios = exercicios;
    }

    public void adicionarExercicio(Exercicio exercicio) {
        // TODO: adicionar exercicio a lista do modulo
    }

    public int getQuantidadeExercicios() {
        // TODO: retornar o total de exercicios do modulo
        return 0;
    }

    public double calcularPorcentagem(ProgressoUsuario progresso) {
        // TODO: calcular % de exercicios deste modulo concluidos pelo usuario
        return 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public List<Exercicio> getExercicios() { return exercicios; }
    public void setExercicios(List<Exercicio> exercicios) { this.exercicios = exercicios; }
}
