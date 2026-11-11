package algoquest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import algoquest.config.UsuarioAutenticado;
import algoquest.dto.ModuloDetalheResposta;
import algoquest.dto.ModuloResumoResposta;
import algoquest.dto.RespostaRequisicao;
import algoquest.dto.ResultadoResposta;
import algoquest.exception.ErroApi;
import algoquest.model.Usuario;
import algoquest.service.ServicoModulo;
import algoquest.service.ServicoResposta;

@RestController
@RequestMapping("/api/modulos")
public class ModuloController {

    private final ServicoModulo servicoModulo;
    private final ServicoResposta servicoResposta;

    public ModuloController(ServicoModulo servicoModulo, ServicoResposta servicoResposta) {
        this.servicoModulo = servicoModulo;
        this.servicoResposta = servicoResposta;
    }

    @GetMapping
    public List<ModuloResumoResposta> listar(@UsuarioAutenticado Usuario usuario) {
        return servicoModulo.listar(usuario);
    }

    @GetMapping("/{id}")
    public ModuloDetalheResposta abrir(@UsuarioAutenticado Usuario usuario, @PathVariable("id") int id) {
        return servicoModulo.abrir(id);
    }

    @PostMapping("/exercicios/{id}/resposta")
    public ResultadoResposta responder(@UsuarioAutenticado Usuario usuario, @PathVariable("id") int id,
                                       @RequestBody RespostaRequisicao requisicao) {
        if (requisicao == null || requisicao.resposta() == null) {
            throw ErroApi.requisicaoInvalida("Informe a resposta.");
        }
        return servicoResposta.responder(usuario, id, requisicao.resposta());
    }
}
