package com.veterinaria.mascota.dto.response;

import com.veterinaria.mascota.enums.Especie;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MascotaResponse {
    private Long id;
    private String nombre;
    private Especie especie;
    private String raza;
    private Integer edad;
    private Long clienteId;
}