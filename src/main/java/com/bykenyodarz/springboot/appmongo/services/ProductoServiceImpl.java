package com.bykenyodarz.springboot.appmongo.services;

import com.bykenyodarz.springboot.appmongo.models.Categoria;
import com.bykenyodarz.springboot.appmongo.models.Producto;
import com.bykenyodarz.springboot.appmongo.repositories.CategoriaRepository;
import com.bykenyodarz.springboot.appmongo.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ProductoServiceImpl implements ProductoService{

    private final ProductoRepository repository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public Flux<Producto> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Producto> findAllWithNameUppercase() {
        return repository.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Producto> findById(String product) {
        return repository.findById(product);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return repository.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return repository.delete(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Categoria> findAllCategoria() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Categoria> findCategoriaById(String category) {
        return categoriaRepository.findById(category);
    }

    @Override
    public Mono<Categoria> saveCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Producto> findByNombre(String nombre) {
        return repository.findByNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Producto> obtenerPorNombre(String nombre) {
        return repository.obtenerPorNombre(nombre);
    }

    @Override
    public Mono<Categoria> findByCategoryNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre);
    }
}
