package algoquest.service;

import org.springframework.stereotype.Service;

import algoquest.model.Exercicio;
import algoquest.model.Usuario;

@Service
public class ServicoGamificacao {

    public void processarRespostaCorreta(Usuario usuario, Exercicio exercicio) {
        // TODO: ler exercicio.getDificuldade() e calcular pontos/XP a conceder
        //       (futuro ponto de aplicacao do padrao Strategy por dificuldade)
        //       depois chamar usuario.adicionarPontos(...) e usuario.adicionarXp(...)
    }
}
