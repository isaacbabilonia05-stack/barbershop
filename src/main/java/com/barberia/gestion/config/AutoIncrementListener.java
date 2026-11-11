package com.barberia.gestion.config;

import com.barberia.gestion.service.SequenceGeneratorService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Antes de guardar cualquier documento, si tiene un campo "id" de tipo Long
 * que esta en null, le asigna el siguiente numero de su secuencia.
 * Asi seguimos teniendo ids 1, 2, 3... como en MySQL.
 */
@Component
public class AutoIncrementListener extends AbstractMongoEventListener<Object> {

    private final SequenceGeneratorService sequenceGeneratorService;

    public AutoIncrementListener(SequenceGeneratorService sequenceGeneratorService) {
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object documento = event.getSource();
        try {
            Field campoId = documento.getClass().getDeclaredField("id");
            if (campoId.getType() != Long.class) {
                return;
            }
            campoId.setAccessible(true);
            if (campoId.get(documento) == null) {
                String secuencia = documento.getClass().getSimpleName().toLowerCase() + "_seq";
                campoId.set(documento, sequenceGeneratorService.generarSecuencia(secuencia));
            }
        } catch (NoSuchFieldException e) {
            // El documento no tiene campo "id" (o lo tiene heredado): no hacemos nada
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("No se pudo asignar el id automatico", e);
        }
    }
}
