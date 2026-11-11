package algoquest.exception;

import org.springframework.http.HttpStatus;

public class ErroApi extends RuntimeException {

    private final HttpStatus status;

    public ErroApi(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static ErroApi requisicaoInvalida(String mensagem) { return new ErroApi(HttpStatus.BAD_REQUEST, mensagem); }
    public static ErroApi naoAutenticado(String mensagem) { return new ErroApi(HttpStatus.UNAUTHORIZED, mensagem); }
    public static ErroApi naoEncontrado(String mensagem) { return new ErroApi(HttpStatus.NOT_FOUND, mensagem); }
    public static ErroApi conflito(String mensagem) { return new ErroApi(HttpStatus.CONFLICT, mensagem); }
    public static ErroApi proibido(String mensagem) { return new ErroApi(HttpStatus.FORBIDDEN, mensagem); }
}
