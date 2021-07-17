package com.bykenyodarz.springboot.appmongo.services;

import com.bykenyodarz.springboot.appmongo.models.Categoria;
import com.bykenyodarz.springboot.appmongo.models.Producto;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

    Flux<Producto> findAll();

    Flux<Categoria> findAllCategoria();

    Flux<Producto> findAllWithNameUppercase();

    Mono<Producto> findById(String product);

    Mono<Categoria> findCategoriaById(String category);

    Mono<Producto> save(Producto producto);

    Mono<Categoria> saveCategoria(Categoria categoria);

    Mono<Void> delete(Producto producto);

    Mono<Producto> findByNombre(String nombre);

    Mono<Producto> obtenerPorNombre(String nombre);

    Mono<Categoria> findByCategoryNombre(String nombre);

}
