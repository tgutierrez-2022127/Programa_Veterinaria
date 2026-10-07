package com.veterinaria.mascota.service;

import com.veterinaria.mascota.dto.request.MascotaRequest;
import com.veterinaria.mascota.dto.response.MascotaResponse;
import com.veterinaria.mascota.entity.Mascota;
import com.veterinaria.mascota.exception.RecursoNoEncontradoException;
import com.veterinaria.mascota.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;

    @Transactional
    public MascotaResponse crear(MascotaRequest req, Long clienteId) {
        Mascota mascota = Mascota.builder()
                .nombre(req.getNombre())
                .especie(req.getEspecie())
                .raza(req.getRaza())
                .edad(req.getEdad())
                .clienteId(clienteId)
                .build();

        Mascota guardada = mascotaRepository.save(mascota);
        return mapearAResponse(guardada);
    }

    public List<MascotaResponse> listarPorCliente(Long clienteId) {
        return mascotaRepository.findByClienteId(clienteId).stream()
                .map(this::mapearAResponse)
                .toList();
    }

    public MascotaResponse obtenerPorId(Long id) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada: " + id));
        return mapearAResponse(mascota);
    }

    private MascotaResponse mapearAResponse(Mascota mascota) {
        return new MascotaResponse(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getEspecie(),
                mascota.getRaza(),
                mascota.getEdad(),
                mascota.getClienteId()
        );
    }
}