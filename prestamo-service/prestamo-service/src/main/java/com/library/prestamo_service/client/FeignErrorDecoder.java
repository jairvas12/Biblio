package com.library.prestamo_service.client;

import com.library.prestamo_service.exception.RecursoNoEncontradoException;
import com.library.prestamo_service.exception.ReglaNegocioException;
import com.library.prestamo_service.exception.ServicioRemotoException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder decoderPredeterminado =
            new ErrorDecoder.Default();

    @Override
    public Exception decode(
            String methodKey,
            Response response
    ) {
        int codigoEstado = response.status();

        String urlRemota =
                response.request() != null
                        ? response.request().url()
                        : "URL desconocida";

        log.error(
                "Error Feign en método {}. Código HTTP: {}. URL: {}",
                methodKey,
                codigoEstado,
                urlRemota
        );

        return switch (codigoEstado) {

            case 400, 409, 422 ->
                    new ReglaNegocioException(
                            "El servicio remoto rechazó la operación "
                                    + "porque incumple una regla de negocio"
                    );

            case 404 ->
                    new RecursoNoEncontradoException(
                            "El recurso solicitado no existe "
                                    + "en el servicio remoto"
                    );

            case 500, 502, 503, 504 ->
                    new ServicioRemotoException(
                            "El servicio remoto no se encuentra "
                                    + "disponible temporalmente"
                    );

            default ->
                    decoderPredeterminado.decode(
                            methodKey,
                            response
                    );
        };
    }
}