package com.bykenyodarz.springboot.appmongo.repositories;

import com.bykenyodarz.springboot.appmongo.models.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaRepository extends ReactiveMongoRepository<Categoria, String> {
    Mono<Categoria> findByNombre(String nombre);
}
