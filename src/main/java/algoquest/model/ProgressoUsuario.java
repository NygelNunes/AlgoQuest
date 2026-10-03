package algoquest.model;

import java.util.ArrayList;
import java.util.List;

public class ProgressoUsuario {

    private int idUsuario;
    private List<Integer> exerciciosConcluidos;

    public ProgressoUsuario() {
        this.exerciciosConcluidos = new ArrayList<>();
    }

    public ProgressoUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
        this.exerciciosConcluidos = new ArrayList<>();
    }

    public ProgressoUsuario(int idUsuario, List<Integer> exerciciosConcluidos) {
        this.idUsuario = idUsuario;
        this.exerciciosConcluidos = exerciciosConcluidos;
    }

    public void marcarExercicioConcluido(int idExercicio) {
        // TODO: adicionar o id a lista, evitando duplicidade
    }

    public boolean isConcluido(int idExercicio) {
        // TODO: verificar se o exercicio consta na lista
        return false;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public List<Integer> getExerciciosConcluidos() { return exerciciosConcluidos; }
    public void setExerciciosConcluidos(List<Integer> exerciciosConcluidos) {
        this.exerciciosConcluidos = exerciciosConcluidos;
    }
}
