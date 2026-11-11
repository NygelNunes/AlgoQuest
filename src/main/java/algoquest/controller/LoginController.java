package algoquest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import algoquest.config.UsuarioAutenticado;
import algoquest.dto.AvatarRequisicao;
import algoquest.dto.LoginRequisicao;
import algoquest.dto.RegistroRequisicao;
import algoquest.dto.UsuarioResposta;
import algoquest.exception.ErroApi;
import algoquest.model.Usuario;
import algoquest.service.ServicoAutenticacao;
import algoquest.service.ServicoAvatar;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final ServicoAutenticacao autenticacao;
    private final ServicoAvatar avatares;

    public LoginController(ServicoAutenticacao autenticacao, ServicoAvatar avatares) {
        this.autenticacao = autenticacao;
        this.avatares = avatares;
    }

    @PostMapping("/login")
    public UsuarioResposta login(@RequestBody LoginRequisicao requisicao) {
        if (requisicao == null) {
            throw ErroApi.requisicaoInvalida("Informe usuário e senha.");
        }
        return autenticacao.login(requisicao.nomeUsuario(), requisicao.senha());
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta registrar(@RequestBody RegistroRequisicao requisicao) {
        if (requisicao == null) {
            throw ErroApi.requisicaoInvalida("Informe usuário e senha.");
        }
        return autenticacao.registrar(requisicao.nomeUsuario(), requisicao.senha());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "Authorization", required = false) String autorizacao) {
        autenticacao.logout(autorizacao);
    }

    @GetMapping("/me")
    public UsuarioResposta eu(@UsuarioAutenticado Usuario usuario) {
        return UsuarioResposta.de(avatares.comImagem(usuario));
    }

    @PostMapping("/me/avatar")
    public UsuarioResposta escolherAvatar(@UsuarioAutenticado Usuario usuario, @RequestBody AvatarRequisicao req) {
        if (req == null) {
            throw ErroApi.requisicaoInvalida("Informe o avatar.");
        }
        return UsuarioResposta.de(avatares.escolher(usuario, req.avatar()));
    }

    @PostMapping("/me/avatar/personalizado")
    public UsuarioResposta enviarAvatar(@UsuarioAutenticado Usuario usuario, @RequestBody AvatarRequisicao req) {
        if (req == null) {
            throw ErroApi.requisicaoInvalida("Informe a imagem.");
        }
        return UsuarioResposta.de(avatares.enviarPersonalizado(usuario, req.imagem()));
    }
}
