package com.bykenyodarz.springboot.appmongo.models;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "productos")
public class Producto {
    @Id
    private String id;
    @NotEmpty
    private String nombre;
    @NotNull
    private Double precio;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt;
    @Valid
    @NotNull
    private Categoria category;
    private String foto;

    public Producto(String nombre, Double precio) {
        this.nombre = nombre;
        this.precio = precio;
        this.createdAt = LocalDateTime.now();
    }

    public Producto(String nombre, Double precio, Categoria categoria) {
        this(nombre, precio);
        this.category = categoria;
    }
}
