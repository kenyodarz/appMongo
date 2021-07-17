package com.bykenyodarz.springboot.appmongo;

import com.bykenyodarz.springboot.appmongo.models.Producto;
import com.bykenyodarz.springboot.appmongo.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppMongoApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductoService service;

    @Test
    void listarTest() {
        client.get()
                .uri("/api/v2/productos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class)
                //.hasSize(9);
                .consumeWith(r -> {
                    List<Producto> productos = r.getResponseBody();
                    assert productos != null;
                    productos.forEach(p -> {
                        System.out.println(p.getNombre());
                    });
                    Assertions.assertTrue(productos.size() > 0);
                });
    }

    @Test
    void testVerDetalle() {
        var producto = service.findByNombre("Sony Notebook").block();
        client.get()
                .uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Sony Notebook");
    }

    @Test
    void testCrear() {
        var categoria = service.findByCategoryNombre("Muebles").block();
        var producto = new Producto("Mesa Comedor", 100.00, categoria);
        client.post()
                .uri("/api/v2/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Mesa Comedor")
                .jsonPath("$.category.nombre").isEqualTo("Muebles");


    }
}
