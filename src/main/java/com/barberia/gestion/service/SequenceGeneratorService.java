package com.barberia.gestion.service;

import com.barberia.gestion.model.DatabaseSequence;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {

    private final MongoOperations mongoOperations;

    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    /** Devuelve el siguiente numero de la secuencia (1, 2, 3...). */
    public long generarSecuencia(String nombreSecuencia) {
        DatabaseSequence contador = mongoOperations.findAndModify(
                new Query(Criteria.where("_id").is(nombreSecuencia)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                DatabaseSequence.class);

        return contador != null ? contador.getSeq() : 1;
    }
}
