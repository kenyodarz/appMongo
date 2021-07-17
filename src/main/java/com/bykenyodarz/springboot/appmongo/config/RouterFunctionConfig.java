package com.bykenyodarz.springboot.appmongo.config;

import com.bykenyodarz.springboot.appmongo.vendors.ProductoHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterFunctionConfig {


    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandlers handler) {

        return route(GET("/api/v2/productos").or(GET("/api/v3/productos")),
                handler::listar)
                .andRoute(GET("/api/v2/productos/{id}"), handler::ver)
                .andRoute(POST("/api/v2/productos"), handler::crear)
                .andRoute(POST("/api/v2/productos/upload/{id}"), handler::upload)
                .andRoute(POST("/api/v2/productos/upload"), handler::uploadWithProduct)
                .andRoute(PUT("/api/v2/productos/{id}"), handler::actualizar)
                .andRoute(DELETE("/api/v2/productos/{id}"), handler::eliminar);

    }
}
