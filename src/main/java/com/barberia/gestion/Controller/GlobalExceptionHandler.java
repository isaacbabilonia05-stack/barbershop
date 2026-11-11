package com.barberia.gestion.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/**
 * Control de errores centralizado: ninguna excepcion llega al usuario como pagina en blanco
 * ni como stack trace. Todas se muestran en templates/error.html con el codigo HTTP correcto.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static ModelAndView vista(int codigo, String titulo, String mensaje) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatusCode.valueOf(codigo));
        mav.addObject("codigo", codigo);
        mav.addObject("titulo", titulo);
        mav.addObject("mensaje", mensaje);
        return mav;
    }

    /** repository.findById(...).orElseThrow() cuando el registro no existe. */
    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView noEncontrado(NoSuchElementException ex) {
        return vista(404, "No encontrado", "El registro que buscas no existe o ya fue eliminado.");
    }

    @ExceptionHandler({TypeMismatchException.class, NumberFormatException.class, DateTimeParseException.class})
    public ModelAndView datosInvalidos(Exception ex) {
        return vista(400, "Solicitud no valida", "Alguno de los datos enviados no tiene el formato correcto.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView accesoDenegado(AccessDeniedException ex) {
        return vista(403, "Acceso denegado", "No tienes permiso para realizar esta accion.");
    }

    /** Violacion de indices unicos de MongoDB (email, telefono...). */
    @ExceptionHandler(DuplicateKeyException.class)
    public ModelAndView duplicado(DuplicateKeyException ex) {
        return vista(409, "Dato duplicado", "Ya existe un registro con ese email o telefono.");
    }

    /** Resto: errores de Spring MVC (404 de ruta, 405 metodo, falta de parametro...) y fallos inesperados. */
    @ExceptionHandler(Exception.class)
    public ModelAndView generico(Exception ex) {
        if (ex instanceof ErrorResponse er) {
            int codigo = er.getStatusCode().value();
            if (codigo == 404) return vista(404, "Pagina no encontrada", "La direccion que buscas no existe.");
            if (codigo == 405) return vista(405, "Metodo no permitido", "Esa accion no esta permitida desde aqui.");
            if (codigo >= 400 && codigo < 500) return vista(codigo, "Solicitud no valida", "No pudimos procesar tu solicitud.");
        }
        log.error("Error inesperado", ex);
        return vista(500, "Error del servidor", "Ocurrio un problema inesperado. Intenta de nuevo mas tarde.");
    }
}
