package algoquest.service;

import java.util.List;

import org.springframework.stereotype.Service;

import algoquest.dto.ModuloDetalheResposta;
import algoquest.dto.ModuloResumoResposta;
import algoquest.exception.ErroApi;
import algoquest.model.Modulo;
import algoquest.model.ProgressoUsuario;
import algoquest.model.Usuario;
import algoquest.repository.ModuloRepositorio;
import algoquest.repository.ProgressoRepositorio;

@Service
public class ServicoModulo {

    private final ModuloRepositorio modulos;
    private final ProgressoRepositorio progressos;

    public ServicoModulo(ModuloRepositorio modulos, ProgressoRepositorio progressos) {
        this.modulos = modulos;
        this.progressos = progressos;
    }

    public List<ModuloResumoResposta> listar(Usuario usuario) {
        ProgressoUsuario progresso = progressos.buscarPorUsuario(usuario.getId());
        return modulos.buscarModulosAtivos().stream()
                .map(m -> ModuloResumoResposta.de(m, m.calcularPorcentagem(progresso)))
                .toList();
    }

    public ModuloDetalheResposta abrir(int idModulo) {
        Modulo modulo = modulos.buscarAtivoPorId(idModulo);
        if (modulo == null) {
            throw ErroApi.naoEncontrado("Módulo não encontrado.");
        }
        return ModuloDetalheResposta.de(modulo);
    }
}
