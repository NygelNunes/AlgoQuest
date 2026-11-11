package algoquest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import algoquest.config.UsuarioAutenticado;
import algoquest.dto.AdminDtos.AdminModuloResposta;
import algoquest.dto.AdminDtos.AdminQuestaoResposta;
import algoquest.dto.AdminDtos.AdminUsuarioResposta;
import algoquest.dto.AdminDtos.ImportacaoRequisicao;
import algoquest.dto.AdminDtos.ImportacaoResposta;
import algoquest.dto.AdminDtos.ModuloRequisicao;
import algoquest.dto.AdminDtos.MoverRequisicao;
import algoquest.dto.AdminDtos.PapelRequisicao;
import algoquest.dto.QuestaoRequisicao;
import algoquest.model.Usuario;
import algoquest.service.ServicoAdmin;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ServicoAdmin admin;

    public AdminController(ServicoAdmin admin) {
        this.admin = admin;
    }

    @GetMapping("/modulos")
    public List<AdminModuloResposta> modulos(@UsuarioAutenticado Usuario u) {
        return admin.listarModulos(u);
    }

    @PostMapping("/modulos")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminModuloResposta criarModulo(@UsuarioAutenticado Usuario u, @RequestBody ModuloRequisicao r) {
        return admin.criarModulo(u, r);
    }

    @PutMapping("/modulos/{id}")
    public AdminModuloResposta editarModulo(@UsuarioAutenticado Usuario u, @PathVariable("id") int id, @RequestBody ModuloRequisicao r) {
        return admin.editarModulo(u, id, r);
    }

    @DeleteMapping("/modulos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirModulo(@UsuarioAutenticado Usuario u, @PathVariable("id") int id) {
        admin.excluirModulo(u, id);
    }

    @PostMapping("/modulos/{id}/mover")
    public List<AdminModuloResposta> moverModulo(@UsuarioAutenticado Usuario u, @PathVariable("id") int id, @RequestBody MoverRequisicao r) {
        return admin.moverModulo(u, id, r == null ? null : r.direcao());
    }

    @GetMapping("/modulos/{id}/questoes")
    public List<AdminQuestaoResposta> questoes(@UsuarioAutenticado Usuario u, @PathVariable("id") int id) {
        return admin.listarQuestoes(u, id);
    }

    @PostMapping("/modulos/{id}/questoes")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminQuestaoResposta criarQuestao(@UsuarioAutenticado Usuario u, @PathVariable("id") int id, @RequestBody QuestaoRequisicao r) {
        return admin.criarQuestao(u, id, r);
    }

    @PostMapping("/modulos/{id}/importacao")
    public ImportacaoResposta importar(@UsuarioAutenticado Usuario u, @PathVariable("id") int id, @RequestBody ImportacaoRequisicao r) {
        return admin.importar(u, id, r);
    }

    @PutMapping("/questoes/{id}")
    public AdminQuestaoResposta editarQuestao(@UsuarioAutenticado Usuario u, @PathVariable("id") int id, @RequestBody QuestaoRequisicao r) {
        return admin.editarQuestao(u, id, r);
    }

    @DeleteMapping("/questoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirQuestao(@UsuarioAutenticado Usuario u, @PathVariable("id") int id) {
        admin.excluirQuestao(u, id);
    }

    @PostMapping("/questoes/{id}/mover")
    public List<AdminQuestaoResposta> moverQuestao(@UsuarioAutenticado Usuario u, @PathVariable("id") int id, @RequestBody MoverRequisicao r) {
        return admin.moverQuestao(u, id, r == null ? null : r.direcao());
    }

    @GetMapping("/usuarios")
    public List<AdminUsuarioResposta> usuarios(@UsuarioAutenticado Usuario u) {
        return admin.listarUsuarios(u);
    }

    @PutMapping("/usuarios/{id}/papel")
    public AdminUsuarioResposta papel(@UsuarioAutenticado Usuario u, @PathVariable("id") int id, @RequestBody PapelRequisicao r) {
        return admin.alterarPapel(u, id, r == null ? null : r.papel());
    }
}
