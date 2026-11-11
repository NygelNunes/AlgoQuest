package algoquest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResultadoResposta(boolean correta, String feedback, int pontosGanhos, UsuarioResposta usuario) {
}
