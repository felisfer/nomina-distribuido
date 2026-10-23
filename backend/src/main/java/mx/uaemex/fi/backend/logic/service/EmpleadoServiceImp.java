package mx.uaemex.fi.backend.logic.service;

import lombok.RequiredArgsConstructor;
import mx.uaemex.fi.backend.logic.exception.NotFoundException;
import mx.uaemex.fi.backend.persistence.repository.EmpleadoRepository;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoResponse;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImp implements EmpleadoService {
    private final EmpleadoRepository empleadoRepository;

    @Override
    public EmpleadoResponse buscarPorRFC(String rfc) throws NotFoundException {
        var empleado = empleadoRepository
                .findByRfc(rfc)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        return new EmpleadoResponse(
                empleado.getId(),
                empleado.getRfc(),
                empleado.getNombres(),
                empleado.getApellidos(),
                empleado.getCorreo()
        );
    }

    @Override
    public void actualizar(Integer id, EmpleadoUpdateRequest req) throws NotFoundException {
        var empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        empleado.setRfc(req.rfc() == null ? empleado.getRfc() :  req.rfc());
        empleado.setNombres(req.nombre() == null ? empleado.getNombres() :  req.nombre());
        empleado.setApellidos(req.apellidos() == null ? empleado.getApellidos() :  req.apellidos());
        empleado.setCorreo(req.correo() == null ? empleado.getCorreo() :  req.correo());

        empleadoRepository.save(empleado);
    }

    @Override
    public List<EmpleadoResponse> obtenerTodos() {
        return empleadoRepository
                .findAll()
                .stream()
                .map((empleado -> new EmpleadoResponse(
                        empleado.getId(),
                        empleado.getRfc(),
                        empleado.getNombres(),
                        empleado.getApellidos(),
                        empleado.getCorreo()
                )))
                .toList();
    }
}
