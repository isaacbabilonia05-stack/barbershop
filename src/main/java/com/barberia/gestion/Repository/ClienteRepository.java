package com.barberia.gestion.Repository;

import com.barberia.gestion.model.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ClienteRepository extends MongoRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
}
