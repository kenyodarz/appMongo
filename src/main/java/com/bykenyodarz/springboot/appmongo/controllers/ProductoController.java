package com.bykenyodarz.springboot.appmongo.controllers;

import com.bykenyodarz.springboot.appmongo.models.Categoria;
import com.bykenyodarz.springboot.appmongo.models.Producto;
import com.bykenyodarz.springboot.appmongo.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProductoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoService productoService;

    @Value("${config.upload.path}")
    private String path;


    @ModelAttribute("categorias")
    public Flux<Categoria> categorias() {
        return productoService.findAllCategoria();
    }

    @GetMapping({"/", "/listar"})
    public Mono<String> listar(Model model) {
        Flux<Producto> productos = productoService.findAllWithNameUppercase();
        productos.subscribe(producto -> LOGGER.info(producto.getNombre()));
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("titulo", "Formulario de Producto");
        model.addAttribute("boton", "Crear");
        model.addAttribute("producto", new Producto());
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> guardar(@Valid @ModelAttribute("producto") Producto p,
                                BindingResult result, Model model,
                                @RequestPart FilePart file) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Errores en formulario producto");
            model.addAttribute("boton", "Guardar");
            return Mono.just("form");
        }
        var category = productoService.findCategoriaById(p.getCategory().getId());

        return category.flatMap(c -> {
            if (p.getCreatedAt() == null) {
                p.setCreatedAt(LocalDateTime.now());
            }

            if (!file.filename().isEmpty()) {
                p.setFoto(UUID.randomUUID() + " - " + file.filename()
                        .replace(" ", "")
                        .replace(":", "")
                        .replace("\\", ""));
            }

            p.setCategory(c);
            return productoService.save(p);
        }).doOnNext(producto -> LOGGER.info(
                String.format("Producto Guardado: %s , Id: %s, Categoría: %s",
                        producto.getNombre(), producto.getId(),
                        producto.getCategory().getNombre())
        ))
                .flatMap(f -> {
                    if (!file.filename().isEmpty()) {
                        return file.transferTo(new File(path + f.getFoto()));
                    }
                    return Mono.empty();
                })
                .thenReturn("redirect:/listar?success=producto+guardado+con+éxito");
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        var producto = productoService.findById(id).doOnNext(
                producto1 -> LOGGER.info(producto1.getNombre())
        );

        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", producto);

        return Mono.just("form");

    }

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id, Model model) {
        return productoService.findById(id)
                .defaultIfEmpty(new Producto())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    LOGGER.info(MessageFormat.format("Eliminando el Producto: {0} con id: {1}",
                            p.getNombre(), p.getId()));
                    return Mono.just(p);
                })
                .flatMap(productoService::delete)
                .thenReturn("redirect:/listar?success=producto+eliminado+con+éxito")
                .onErrorResume(ex ->
                        Mono.just("redirect:/listar?error=no+existe+el+producto+ha+eliminar"));
    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable String id) {
        return productoService.findById(id)
                .doOnNext(producto -> {
                    model.addAttribute("producto", producto);
                    model.addAttribute("titulo", "Detalle Producto");
                }).switchIfEmpty(Mono.just(new Producto()))
                .flatMap(producto -> {
                    if (producto.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(producto);
                })
                .then(Mono.just("ver"))
                .onErrorResume(ex ->
                        Mono.just("redirect:/listar?error=no+existe+el+producto+ha+eliminar"));
    }

    @GetMapping("/uploads/img/{nombreFoto:.+}")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException {
        Path ruta = Paths.get(path).resolve(nombreFoto).toAbsolutePath();
        Resource image = new UrlResource(ruta.toUri());

        return Mono.just(ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment: filename=\"" + image.getFilename() + "\"")
                .body(image));
    }

    @GetMapping("/listar-data-driven")
    public String listarDataDriver(Model model) {
        Flux<Producto> productos = productoService.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                }).delayElements(Duration.ofSeconds(1));
        productos.subscribe(producto -> LOGGER.info(producto.getNombre()));
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de productos");
        return "form";
    }


    @GetMapping("/listar-full")
    public String listarFull(Model model) {
        Flux<Producto> productos = productoService.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                }).repeat(5000);
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "form";
    }


    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {
        Flux<Producto> productos = productoService.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                }).repeat(5000);
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar-chunked";
    }
}
