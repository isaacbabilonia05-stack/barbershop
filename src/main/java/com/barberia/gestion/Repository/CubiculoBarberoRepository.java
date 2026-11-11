package com.barberia.gestion.Repository;

import com.barberia.gestion.model.CubiculoBarbero;
import com.barberia.gestion.model.Cubiculo;
import com.barberia.gestion.model.Barbero;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface CubiculoBarberoRepository extends MongoRepository<CubiculoBarbero, Long> {
    List<CubiculoBarbero> findByBarberoAndActivoTrue(Barbero barbero);
    List<CubiculoBarbero> findByBarbero(Barbero barbero);
    List<CubiculoBarbero> findByCubiculo(Cubiculo cubiculo);
    List<CubiculoBarbero> findByFechaFinAfter(LocalDate fecha);
}
