package algoquest.config;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import algoquest.model.Usuario;
import algoquest.service.ServicoAutenticacao;

@Component
public class ResolvedorUsuarioAutenticado implements HandlerMethodArgumentResolver {

    private final ServicoAutenticacao autenticacao;

    public ResolvedorUsuarioAutenticado(ServicoAutenticacao autenticacao) {
        this.autenticacao = autenticacao;
    }

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return parametro.hasParameterAnnotation(UsuarioAutenticado.class)
                && Usuario.class.isAssignableFrom(parametro.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer container,
                                  NativeWebRequest requisicao, WebDataBinderFactory fabrica) {
        return autenticacao.autenticar(requisicao.getHeader("Authorization"));
    }
}
