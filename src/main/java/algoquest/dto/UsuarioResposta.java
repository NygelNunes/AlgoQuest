package algoquest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import algoquest.model.Usuario;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioResposta(int id, String nomeUsuario, int pontuacao, int xp, int nivel,
                              int xpProximoNivel, String avatar, String avatarImagem, String papel, String token) {

    public static UsuarioResposta de(Usuario u) {
        return new UsuarioResposta(u.getId(), u.getNomeUsuario(), u.getPontuacao(), u.getXp(), u.getNivel(),
                u.getXpProximoNivel(), u.getAvatar(), u.getAvatarImagem(), u.getPapel().name(), null);
    }

    public static UsuarioResposta comToken(Usuario u, String token) {
        return new UsuarioResposta(u.getId(), u.getNomeUsuario(), u.getPontuacao(), u.getXp(), u.getNivel(),
                u.getXpProximoNivel(), u.getAvatar(), u.getAvatarImagem(), u.getPapel().name(), token);
    }
}
