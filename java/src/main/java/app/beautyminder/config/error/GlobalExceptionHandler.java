package app.beautyminder.config.error;

import app.beautyminder.config.error.exception.BusinessBaseException;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.OpenSearchStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@ControllerAdvice // 모든 컨트롤러에서 발생하는 예외를 잡아서 처리
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessBaseException.class)
    protected ResponseEntity<ErrorResponse> handle(BusinessBaseException e) {
        log.error("BusinessException", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handle(AccessDeniedException e) {
        log.error("AccessBaseException", e);
        return createErrorResponseEntity(ErrorCode.ACCESS_DENIED_ERROR);
    }

    // TODO stacktrace is too long
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handle(Exception e) {
        log.error("Exception", e);
        return createErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({IOException.class})
    protected ResponseEntity<ErrorResponse> handleIOException(Exception e) {
        log.error("IOException", e);
        return createErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String errorDetails = String.format("Request method '%s' not supported for the endpoint. Supported methods are %s.", e.getMethod(), Arrays.toString(e.getSupportedMethods()));

        log.error("Method not supported: {}", errorDetails);
        return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
        StringBuilder errorDetails = new StringBuilder();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errorDetails.append("Field error in object '").append(error.getObjectName()).append("' on field '").append(error.getField()).append("': rejected value [").append(error.getRejectedValue()).append("]; ").append(error.getDefaultMessage()).append(". ");
        }
        for (ObjectError error : e.getBindingResult().getGlobalErrors()) {
            errorDetails.append("Object error: ").append(error.getDefaultMessage()).append(". ");
        }

        log.error("Validation failed: {}", errorDetails);
        return createErrorResponseEntity(ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handle(MissingServletRequestParameterException e) {
        String errorDetails = String.format("Required request parameter '%s' of type %s is not present", e.getParameterName(), e.getParameterType());

        log.error("Missing request parameter: {}", errorDetails);
        return createErrorResponseEntity(ErrorCode.MISSING_REQUEST_PARAMETER);
    }

    @ExceptionHandler(MultipartException.class)
    protected ResponseEntity<ErrorResponse> handleMultipartException(MultipartException e) {
        String errorDetails = String.format("Wrong Multipart format: '%s'", e.getMessage());

        log.error("Cannot digest multipart data: {}", errorDetails);
        return createErrorResponseEntity(ErrorCode.INVALID_MULTIPART);
    }

    @ExceptionHandler(OpenSearchStatusException.class)
    protected ResponseEntity<ErrorResponse> handleOpenSearchStatusException(OpenSearchStatusException e) {
        String errorDetails = e.getMessage();

        log.error("OpenSearchStatusException: {}", errorDetails);
        return createErrorResponseEntity(ErrorCode.ELASTICSEARCH_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        // Log the detailed error message for server-side debugging.
        String errorDetails = "Required request body is missing or not readable. Please check your request payload.";
        log.error("{}: {}", errorDetails, e.getMessage());

        // Create an ErrorResponse object and return it with a BAD_REQUEST status.
        return createErrorResponseEntity(ErrorCode.MISSING_OR_UNREADABLE_BODY);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {
        return new ResponseEntity<>(ErrorResponse.of(errorCode), errorCode.getStatus());
    }
}
