package algoquest.service;

import org.springframework.stereotype.Service;

import algoquest.model.Exercicio;
import algoquest.model.Usuario;

@Service
public class ServicoGamificacao {

    public void processarRespostaCorreta(Usuario usuario, Exercicio exercicio) {
        usuario.adicionarPontos(exercicio.getPontos());
        usuario.adicionarXp(exercicio.getPontos());
    }
}
