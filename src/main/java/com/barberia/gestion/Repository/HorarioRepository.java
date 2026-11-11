package com.barberia.gestion.Repository;

import com.barberia.gestion.model.Horario;
import com.barberia.gestion.model.Barbero;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface HorarioRepository extends MongoRepository<Horario, Long> {
    List<Horario> findByBarbero(Barbero barbero);
    List<Horario> findByBarberoAndDiaSemana(Barbero barbero, Integer diaSemana);
}
