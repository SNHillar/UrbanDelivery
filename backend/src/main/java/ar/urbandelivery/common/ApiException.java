package ar.urbandelivery.common;

import org.springframework.http.HttpStatus;

/**
 * Error de negocio con su código HTTP. Lo traduce {@link GlobalExceptionHandler}.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	private ApiException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public static ApiException notFound(String message) {
		return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
	}

	public static ApiException forbidden(String message) {
		return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
	}

	public static ApiException badRequest(String code, String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, code, message);
	}

	/** La operación no es válida para el estado actual del recurso. */
	public static ApiException conflict(String code, String message) {
		return new ApiException(HttpStatus.CONFLICT, code, message);
	}

	/** La petición está bien formada pero viola una regla de negocio. */
	public static ApiException businessRule(String code, String message) {
		return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, code, message);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

}
