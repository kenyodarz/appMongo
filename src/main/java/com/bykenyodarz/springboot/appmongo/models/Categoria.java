package com.bykenyodarz.springboot.appmongo.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Document(collection = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {
    @Id
    @NotEmpty
    private String id;

    private String nombre;

    public Categoria(String nombre) {
        this.nombre = nombre;
    }
}
