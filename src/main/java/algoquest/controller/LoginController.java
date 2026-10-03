package algoquest.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class LoginController {

    @PostMapping("/login")
    public void autenticar() {
        // TODO: endpoint chamado pelo frontend -> validar credenciais e iniciar sessao
    }

    @PostMapping("/logout")
    public void sair() {
        // TODO: encerrar sessao do usuario
    }
}
