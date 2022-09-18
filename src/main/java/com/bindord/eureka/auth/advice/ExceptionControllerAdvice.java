package com.bindord.eureka.auth.advice;

import com.bindord.eureka.auth.domain.exception.ApiError;
import com.bindord.eureka.auth.domain.exception.ApiSubError;
import com.bindord.resourceserver.model.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.bindord.eureka.auth.configuration.JacksonFactory.getObjectMapper;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    private static final ObjectMapper mapper = getObjectMapper();

    private static final String SQL_UNIQUE_VIOLATION_CODE = "23505";
    private static final String SQL_DUP_EXCEP_PREFIX = "duplicate key value violates unique constraint";
    private static final String SQL_DUP_EXCEP_PREFIX_ES = "llave duplicada viola restricción de unicidad";
    private static final String TEMP_UNIQUE_CONS_ONE = "uk8jmdau039u32ktqkckcdgnvkt";
    private static final String TEMP_UNIQUE_CONS_TWO = "uk_7rr530m3pxabetp6s9r0fjp37";
    private static final String TEMP_UC_ONE_MSG = "¡El código del producto ya ha sido escaneado con anterioridad, no puede haber códigos duplicados!";
    private static final String TEMP_UC_TWO_MSG = "El usuario que intenta registrar, ya ha sido registrado con anterioridad. Si no lo encuentra en su lista de contactos, es porque este ha sido registrado por otro vendedor.";

    private static final Logger LOGGER = LogManager.getLogger(ExceptionControllerAdvice.class);
    public static final String BINDING_ERROR = "Validation has failed";

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ApiError> handleBindException(WebExchangeBindException ex) {
        ex.getModel().entrySet().forEach(e -> {
            LOGGER.warn(e.getKey() + ": " + e.getValue());
        });
        List<ApiSubError> errors = new ArrayList<>();

        for (FieldError x : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ApiSubError(x.getObjectName(), x.getField(), x.getRejectedValue(), x.getDefaultMessage()));
        }
        return Mono.just(new ApiError(HttpStatus.BAD_REQUEST, BINDING_ERROR, errors));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ApiError> handleBindException(IllegalArgumentException ex) {
        return Mono.just(new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundValidationException.class)
    public @ResponseBody
    Mono<ApiError> handlerNotFoundValidationException(NotFoundValidationException ex) {
        return Mono.just(new ApiError(HttpStatus.NOT_FOUND, ex));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(WebClientResponseException.class)
    public @ResponseBody
    ErrorResponse handlerWebClientResponseException(WebClientResponseException ex)
            throws JsonProcessingException {
        LOGGER.warn(ex.getMessage());
        var lenStackTrace = ex.getStackTrace().length;
        for (int i = 0; i < lenStackTrace; i++) {
            LOGGER.warn(ex.getStackTrace()[i].toString());
        }
        return mapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);
    }
}


