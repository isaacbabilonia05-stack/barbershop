package com.barberia.gestion.Repository;

import com.barberia.gestion.model.Cita;
import com.barberia.gestion.model.Barbero;
import com.barberia.gestion.model.Cliente;
import com.barberia.gestion.model.EstadoCita;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends MongoRepository<Cita, Long> {

    List<Cita> findByCliente(Cliente cliente);

    List<Cita> findByBarbero(Barbero barbero);

    List<Cita> findByEstado(EstadoCita estado);

    List<Cita> findByBarberoAndEstado(Barbero barbero, EstadoCita estado);

    List<Cita> findByBarberoAndFecha(Barbero barbero, LocalDate fecha);

    void deleteByCliente(Cliente cliente);

    /**
     * Avalua si existeix alguna cita activa per a aquest barber i data 
     * que col·lisioni amb el rang d'hores sol·licitat.
     */
    default boolean existsByBarberoAndFechaAndHorarios(Barbero barbero, LocalDate fecha,
                                                       LocalTime horaInicio, LocalTime horaFin) {
        if (barbero == null || fecha == null || horaInicio == null || horaFin == null) {
            return false;
        }

        return findByBarberoAndFecha(barbero, fecha).stream()
                .filter(c -> c.getEstado() != EstadoCita.CANCELADA)
                .anyMatch(c -> c.getHoraInicio() != null && c.getHoraFin() != null
                        && c.getHoraInicio().isBefore(horaFin)
                        && c.getHoraFin().isAfter(horaInicio));
    }

    /**
     * Suma els totals de les cites completades.
     */
    default Double findIngresosTotales() {
        return findByEstado(EstadoCita.COMPLETADA).stream()
                .mapToDouble(Cita::getTotal)
                .sum();
    }

    /**
     * Calcula la qualificació mitjana global de les cites qualificades.
     */
    default Double findCalificacionPromedio() {
        return findAll().stream()
                .filter(c -> c.getCalificacion() != null)
                .mapToInt(Cita::getCalificacion)
                .average()
                .orElse(0.0);
    }

    /** Cuantas citas usan un servicio (para impedir borrarlo si esta en uso). */
    default boolean existsByServicioId(Long servicioId) {
        return findAll().stream().anyMatch(c -> c.getDetalles() != null && c.getDetalles().stream()
                .anyMatch(d -> d.getServicio() != null && servicioId.equals(d.getServicio().getId())));
    }

    /** Hay citas que usan este cubiculo? */
    default boolean existsByCubiculoId(Long cubiculoId) {
        return findAll().stream()
                .anyMatch(c -> c.getCubiculo() != null && cubiculoId.equals(c.getCubiculo().getId()));
    }
}
