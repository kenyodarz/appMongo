package com.bykenyodarz.springboot.appmongo.controllers.rest;

import com.bykenyodarz.springboot.appmongo.models.Producto;
import com.bykenyodarz.springboot.appmongo.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/productos")
public class ProductoRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductoRestController.class);

    private final ProductoService service;
    @Value("${config.upload.path}")
    private String path;

    @PostMapping("/v2")
    public Mono<ResponseEntity<Producto>> createWithPhoto(Producto producto, @RequestPart FilePart file) {
        if (producto.getCreatedAt() == null) {
            producto.setCreatedAt(LocalDateTime.now());
        }
        producto.setFoto(UUID.randomUUID() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", ""));
        return file
                .transferTo(new File(path + producto.getFoto()))
                .then(service.save(producto))
                .map(p ->
                        ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                                .body(p));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id,
                                                 @RequestPart FilePart file) {
        return service.findById(id).flatMap(p -> {
            p.setFoto(UUID.randomUUID() + "-" + file.filename()
                    .replace(" ", "")
                    .replace(":", "")
                    .replace("\\", ""));
            return file.transferTo(new File(path + p.getFoto()))
                    .then(service.save(p));
        }).map(p -> ResponseEntity.ok().body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> index() {
        return Mono.just(ResponseEntity.ok().body(service.findAll()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> show(@PathVariable String id) {
        return service.findById(id)
                .map(producto -> ResponseEntity.ok().body(producto))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> create(@Valid @RequestBody Mono<Producto> monoProducto) {
        Map<String, Object> response = new HashMap<>();
        return monoProducto.flatMap(producto -> {
            if (producto.getCreatedAt() == null) {
                producto.setCreatedAt(LocalDateTime.now());
            }
            return service.save(producto).map(p -> {
                response.put("producto", p);
                response.put("mensaje", "Producto creado con éxito");
                response.put("timestamp", LocalDateTime.now());
                return
                        ResponseEntity
                                .created(URI.create("/api/productos/".concat(p.getId())))
                                .body(response);
            });
        }).onErrorResume(t -> Mono.just(t)
                .cast(WebExchangeBindException.class)
                .flatMap(e -> Mono.just(e.getFieldErrors()))
                .flatMapMany(Flux::fromIterable)
                .map(fieldError -> String.format("El campo %s %s",
                        fieldError.getField(), fieldError.getDefaultMessage()))
                .collectList()
                .flatMap(list -> {
                    response.put("errors", list);
                    response.put("timestamp", LocalDateTime.now());
                    response.put("status", HttpStatus.BAD_REQUEST.value());
                    return Mono.just(
                            ResponseEntity.badRequest().body(response));
                }));
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Producto>> update(@RequestBody Producto producto,
                                                 @PathVariable String id) {
        return service.findById(id).flatMap(p -> {
            p.setNombre(producto.getNombre());
            p.setPrecio(producto.getPrecio());
            p.setCategory(producto.getCategory());
            return service.save(p);
        }).map(p -> ResponseEntity.created(URI.create("/api/productos/"
                .concat(p.getId()))).body(p)).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return service.findById(id)
                .flatMap(p -> service.delete(p)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
