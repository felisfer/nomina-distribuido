package mx.uaemex.fi.backend.logic.service;

import mx.uaemex.fi.backend.logic.exception.NotFoundException;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoResponse;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoUpdateRequest;

import java.util.List;

public interface EmpleadoService {
    EmpleadoResponse buscarPorRFC(String rfc) throws NotFoundException;
    List<EmpleadoResponse> obtenerTodos();
    void actualizar(Integer id, EmpleadoUpdateRequest req) throws NotFoundException;
}
