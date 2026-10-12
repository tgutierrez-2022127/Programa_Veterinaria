package com.veterinaria.mascota.service.impl;

import com.veterinaria.comun.exception.AccesoDenegadoException;
import com.veterinaria.comun.exception.RecursoNoEncontradoException;
import com.veterinaria.comun.exception.ReglaNegocioException;
import com.veterinaria.mascota.client.UsuarioClienteDto;
import com.veterinaria.mascota.client.UsuarioClient;
import com.veterinaria.mascota.dto.request.MascotaRequest;
import com.veterinaria.mascota.dto.response.MascotaResponse;
import com.veterinaria.mascota.entity.Mascota;
import com.veterinaria.mascota.repository.MascotaRepository;
import com.veterinaria.mascota.service.MascotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de mascotas.
 */
@Service
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;
    private final UsuarioClient usuarioClient;

    public MascotaServiceImpl(MascotaRepository mascotaRepository, UsuarioClient usuarioClient) {
        this.mascotaRepository = mascotaRepository;
        this.usuarioClient = usuarioClient;
    }

    @Override
    @Transactional
    public MascotaResponse registrar(MascotaRequest request, Long usuarioId, String rol) {
        Long clienteId;

        if ("CLIENTE".equals(rol)) {
            // SEGURIDAD: un cliente NO puede elegir dueño; siempre es el mismo.
            clienteId = usuarioId;
        } else {
            // ADMIN: debe indicar el dueño en el body.
            if (request.clienteId() == null) {
                throw new ReglaNegocioException(
                        "Un ADMIN debe indicar 'clienteId' para saber a quien pertenece la mascota");
            }
            clienteId = request.clienteId();
        }

        // Validacion entre microservicios: el cliente debe existir y tener rol CLIENTE.
        UsuarioClienteDto cliente = usuarioClient.obtenerUsuario(clienteId);
        if (cliente != null && !"CLIENTE".equals(cliente.rol())) {
            throw new ReglaNegocioException("El usuario con id " + clienteId
                    + " no es un CLIENTE (rol actual: " + cliente.rol() + ")");
        }

        Mascota mascota = Mascota.builder()
                .nombre(request.nombre().trim())
                .especie(request.especie())
                .raza(request.raza())
                .edad(request.edad())
                .clienteId(clienteId)
                .build();

        return MascotaResponse.from(mascotaRepository.save(mascota));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MascotaResponse> misMascotas(Long clienteId) {
        return mascotaRepository.findByClienteId(clienteId).stream()
                .map(MascotaResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MascotaResponse obtener(Long id) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada con id " + id));
        return MascotaResponse.from(mascota);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MascotaResponse> listarTodas() {
        return mascotaRepository.findAll().stream()
                .map(MascotaResponse::from)
                .toList();
    }

    /** Verifica que la mascota pertenece al cliente; en caso contrario lanza 403. */
    @Transactional(readOnly = true)
    public void verificarPropietario(Long mascotaId, Long clienteId) {
        if (!mascotaRepository.existsByIdAndClienteId(mascotaId, clienteId)) {
            throw new AccesoDenegadoException("La mascota no pertenece al cliente autenticado");
        }
    }
}
