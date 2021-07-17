package com.bykenyodarz.springboot.appmongo;

import com.bykenyodarz.springboot.appmongo.models.Categoria;
import com.bykenyodarz.springboot.appmongo.models.Producto;
import com.bykenyodarz.springboot.appmongo.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@SpringBootApplication
public class AppMongoApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppMongoApplication.class);
    private final ProductoService service;

    private final ReactiveMongoTemplate mongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(AppMongoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();

        var electronico = new Categoria("Electronico");
        var deporte = new Categoria("Deporte");
        var computation = new Categoria("Computación");
        var muebles = new Categoria("Muebles");

        Flux.just(electronico, deporte, computation, muebles)
                .flatMap(service::saveCategoria)
                .doOnNext(categoria -> LOGGER.info(String.format("Insert: %s with id %s",
                        categoria.getNombre(), categoria.getId())))
                .thenMany(
                        Flux.just(
                                new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
                                new Producto("Sony Camara HD Digital", 177.89, electronico),
                                new Producto("Apple iPod", 46.89, electronico),
                                new Producto("Sony Notebook", 846.89, computation),
                                new Producto("Hewlett Packard Multifuncional", 200.89, computation),
                                new Producto("Bianchi Bicicleta", 70.89, deporte),
                                new Producto("HP Notebook Omen 17", 2500.89, electronico),
                                new Producto("Mica Cómoda 5 cajones", 150.89, muebles),
                                new Producto("TC Sony Bravia OLED 4K Ultra HD", 2255.89, electronico))
                                .flatMap(service::save))
                .subscribe(
                        productoMono -> LOGGER.info(String.format("Insert: %s with price %s",
                                productoMono.getNombre(), productoMono.getPrecio())));
    }
}
