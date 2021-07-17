package com.bykenyodarz.springboot.appmongo.vendors;

import com.bykenyodarz.springboot.appmongo.models.Categoria;
import com.bykenyodarz.springboot.appmongo.models.Producto;
import com.bykenyodarz.springboot.appmongo.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@RequiredArgsConstructor
public class ProductoHandlers {

    private final ProductoService service;
    private final Validator validator;
    @Value("${config.upload.path}")
    private String path;

    public Mono<ServerResponse> upload(ServerRequest request) {
        var id = request.pathVariable("id");
        return request.multipartData().map(multipartData ->
                multipartData.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart ->
                        service.findById(id).flatMap(producto -> {
                            producto.setFoto(UUID.randomUUID() + "-" + filePart.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", ""));
                            return filePart.transferTo(new File(path + producto.getFoto()))
                                    .then(service.save(producto));
                        })).flatMap(p -> ServerResponse
                        .created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(service.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> uploadWithProduct(ServerRequest request) {
        Mono<Producto> productoMono = request.multipartData().map(multipartData -> {
            FormFieldPart nombre = (FormFieldPart) multipartData.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) multipartData.toSingleValueMap().get("precio");
            FormFieldPart categoriaId = (FormFieldPart) multipartData.toSingleValueMap().get("categoria.id");
            FormFieldPart categoriaNombre = (FormFieldPart) multipartData.toSingleValueMap().get("categoria.nombre");

            var categoria = new Categoria(categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
        });
        return request.multipartData().map(multipartData ->
                multipartData.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart ->
                        productoMono.flatMap(producto -> {
                            producto.setFoto(UUID.randomUUID() + "-" + filePart.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", ""));
                            producto.setCreatedAt(LocalDateTime.now());
                            return filePart.transferTo(new File(path + producto.getFoto()))
                                    .then(service.save(producto));
                        })).flatMap(p -> ServerResponse
                        .created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(service.save(p), Producto.class));
    }

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request) {
        var id = request.pathVariable("id");
        return service.findById(id).flatMap(producto ->
                ServerResponse
                        .ok()
                        .body(fromValue(producto)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);

        return producto.flatMap(p -> {
            // Validación de Errores
            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            validator.validate(p, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> String.format("El campo %s %s",
                                fieldError.getField(), fieldError.getDefaultMessage()))
                        .collectList()
                        .flatMap(list -> ServerResponse.badRequest().body(fromValue(list)));
            }// Fin de la validación de Errores
            else {
                if (p.getCreatedAt() == null) {
                    p.setCreatedAt(LocalDateTime.now());
                }
                return service.save(p).flatMap(pdb -> ServerResponse
                        .created(URI.create("/api/v2/productos/".concat(pdb.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(pdb)));
            }
        });
    }

    public Mono<ServerResponse> actualizar(ServerRequest request) {
        var id = request.pathVariable("id");
        var producto = request.bodyToMono(Producto.class);
        var productoDB = service.findById(id);

        return productoDB.zipWith(producto, (db, req) -> {
            db.setNombre(req.getNombre());
            db.setPrecio(req.getPrecio());
            db.setCategory(req.getCategory());
            return db;
        }).flatMap(p -> ServerResponse
                .created(URI.create("/api/v2/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        var id = request.pathVariable("id");
        var productoDB = service.findById(id);

        return productoDB.flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
