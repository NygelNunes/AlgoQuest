package algoquest.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/modulos")
@CrossOrigin
public class ModuloController {

    @GetMapping
    public void listarModulos() {
        // TODO: endpoint chamado pelo frontend -> listar modulos disponiveis
    }

    @GetMapping("/{id}")
    public void abrirModulo() {
        // TODO: endpoint chamado pelo frontend -> abrir modulo escolhido
    }

    @PostMapping("/exercicios/{id}/resposta")
    public void responderExercicio() {
        // TODO: endpoint chamado pelo frontend -> verificar resposta e acionar ServicoGamificacao
    }
}
