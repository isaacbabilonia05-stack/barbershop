package com.barberia.gestion.Repository;

import com.barberia.gestion.model.Servicio;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ServicioRepository extends MongoRepository<Servicio, Long> {
    List<Servicio> findByTipo(Servicio.TipoServicio tipo);
}
