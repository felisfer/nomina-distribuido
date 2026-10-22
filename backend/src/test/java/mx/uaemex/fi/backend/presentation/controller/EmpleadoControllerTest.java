package mx.uaemex.fi.backend.presentation.controller;

import mx.uaemex.fi.backend.config.SecurityConfig;
import mx.uaemex.fi.backend.logic.exception.NotFoundException;
import mx.uaemex.fi.backend.logic.service.CustomUserDetailsService;
import mx.uaemex.fi.backend.logic.service.EmpleadoService;
import mx.uaemex.fi.backend.logic.service.JwtServiceImp;
import mx.uaemex.fi.backend.persistence.repository.AccesoRepository;
import mx.uaemex.fi.backend.persistence.repository.EmpleadoRepository;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoResponse;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoUpdateRequest;
import mx.uaemex.fi.backend.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static mx.uaemex.fi.backend.util.TestDataBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmpleadoController.class)
@Import(SecurityConfig.class)
class EmpleadoControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EmpleadoService empleadoService;

    @MockitoBean
    private JwtServiceImp jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private EmpleadoRepository empleadoRepository;

    @MockitoBean
    private AccesoRepository accesoRepository;

    @Test
    @DisplayName("obtenerEmpleados - Retorna todos los empleados")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void obtenerEmpleados_retornaEmpleados() throws Exception {
        List<EmpleadoResponse> empleados = TestDataBuilder.crearListaEmpleadosResponse();

        when(empleadoService.obtenerTodos()).thenReturn(empleados);

        mvc.perform(get("/empleado/"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    // Verificar que la respuesta contiene los empleados esperados
                    for (EmpleadoResponse emp : empleados) {
                        assert json.contains(emp.rfc());
                        assert json.contains(emp.nombre());
                        assert json.contains(emp.apellidos());
                        assert json.contains(emp.correo());
                    }
                });
    }

    @Test
    @DisplayName("obtenerEmpleados - Retorna 401")
    void obtenerEmpleados_retorna401() throws Exception {
        mvc.perform(get("/empleado/"))
                .andExpect(status().isUnauthorized());

        verify(empleadoService, never()).obtenerTodos();
    }

    @Test
    @DisplayName("obtenerEmpleado - RFC existente retorna empleado")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void obtenerEmpleado_rfcExistente_retornaEmpleado() throws Exception {
        EmpleadoResponse empleado = TestDataBuilder.crearEmpleadoResponse();

        when(empleadoService.buscarPorRFC(TEST_RFC)).thenReturn(empleado);

        mvc.perform(get("/empleado/" + TEST_RFC))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    assert json.contains(empleado.rfc());
                    assert json.contains(empleado.nombre());
                    assert json.contains(empleado.apellidos());
                    assert json.contains(empleado.correo());
                });

        verify(empleadoService).buscarPorRFC(TEST_RFC);
    }

    @Test
    @DisplayName("obtenerEmpleado - RFC inexistente retorna 404")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void obtenerEmpleado_rfcInexistente_retornaEmpleado() throws Exception {
        when(empleadoService.buscarPorRFC(TEST_RFC)).thenThrow(new NotFoundException("Empleado no encontrado"));

        mvc.perform(get("/empleado/" + TEST_RFC))
                .andExpect(status().isNotFound());

        verify(empleadoService).buscarPorRFC(TEST_RFC);
    }

    @Test
    @DisplayName("obtenerEmpleado - Retorna 401")
    void obtenerEmpleado_retorna401() throws Exception {
        mvc.perform(get("/empleado/" + TEST_RFC))
                .andExpect(status().isUnauthorized());

        verify(empleadoService, never()).buscarPorRFC(anyString());
    }

    @Test
    @DisplayName("actualizaEmpleado correctamente - Retorna 200")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void actualizaEmpleado_retorna200() throws Exception {
        EmpleadoUpdateRequest req = TestDataBuilder.crearEmpleadoUpdateRequest();

        mvc.perform(patch("/empleado/" + TEST_ID)
                        .content(asJsonString(req))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(empleadoService).actualizar(TEST_ID, req);
    }

    @Test
    @DisplayName("actualizaEmpleado empleado no existente - Retorna 404")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void actualizaEmpleado_empleadoNoExistente_retorna404() throws Exception {
        EmpleadoUpdateRequest req = TestDataBuilder.crearEmpleadoUpdateRequest();

        doThrow(NotFoundException.class).when(empleadoService).actualizar(TEST_ID, req);

        mvc.perform(patch("/empleado/" + TEST_ID)
                        .content(asJsonString(req))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("actualizaEmpleado sin autenticacion - Retorna 401")
    void actualizaEmpleado_sinAutenticacion_retorna401() throws Exception {
        mvc.perform(patch("/empleado/" + TEST_ID))
                .andExpect(status().isUnauthorized());

        verify(empleadoService, never()).actualizar(anyInt(), any());
    }

    @Test
    @DisplayName("actualizaEmpleado con body vacio - Retorna 400")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void actualizaEmpleado_bodyVacio_retorna_400() throws Exception {
        mvc.perform(patch("/empleado/" + TEST_ID)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(empleadoService, never()).actualizar(anyInt(), any());
    }

    @ParameterizedTest
    @CsvSource({
            "AAAA123456", // Sin homoclave
            "AAA123456XXX", // 3 letras al inicio
            "AAAAA23456XX", // 5 letras al inicio
            "AAAA12345XXX", // 5 dígitos
            "AAAA1234567X", // 7 dígitos
            "1AAA123456XXX", // 1 digito al inicio
    })
    @DisplayName("actualizaEmpleado campo RFC invalido - Retorna 400")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void actualizaEmpleado_campoRFCInvalido_retorna_400(String rfc) throws Exception {

        EmpleadoUpdateRequest req = TestDataBuilder.crearEmpleadoUpdateRequest(
                rfc,
                TEST_NOMBRE,
                TEST_APELLIDOS,
                TEST_CORREO
        );

        mvc.perform(patch("/empleado/" + TEST_ID)
                        .content(asJsonString(req))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(empleadoService, never()).actualizar(anyInt(), any());
    }

    @ParameterizedTest
    @CsvSource({
            "juan", // tod* en minúsculas
            "Juan", // primera en mayúscula
            "JUAN1", // con número
            "'JUAN '", // espacio al final
            "' JUAN'", // espacio al inicio
            "JUAN   JOSE", // múltiples espacios entre nombres
    })
    @DisplayName("actualizaEmpleado campo nombre invalido - Retorna 400")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void actualizaEmpleado_campoNombreInvalido_retorna_400(String nombre) throws Exception {

        EmpleadoUpdateRequest req = TestDataBuilder.crearEmpleadoUpdateRequest(
                TEST_RFC,
                nombre,
                TEST_APELLIDOS,
                TEST_CORREO
        );

        mvc.perform(patch("/empleado/" + TEST_ID)
                        .content(asJsonString(req))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(empleadoService, never()).actualizar(anyInt(), any());
    }

    @ParameterizedTest
    @CsvSource({
            "perez", // tod* en minúsculas
            "Perez", // primera en mayúscula
            "PEREZ1", // con número
            "'PEREZ '", // espacio al final
            "' PEREZ'", // espacio al inicio
            "PEREZ   GARCIA", // múltiples espacios entre nombres
    })
    @DisplayName("actualizaEmpleado campo apellidos invalido - Retorna 400")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void actualizaEmpleado_campoApellidosInvalido_retorna_400(String apellidos) throws Exception {

        EmpleadoUpdateRequest req = TestDataBuilder.crearEmpleadoUpdateRequest(
                TEST_RFC,
                TEST_NOMBRE,
                apellidos,
                TEST_CORREO
        );

        mvc.perform(patch("/empleado/" + TEST_ID)
                        .content(asJsonString(req))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(empleadoService, never()).actualizar(anyInt(), any());
    }

    @ParameterizedTest
    @CsvSource({
            "@ejemplo.com", // sin usuario
            "juan.perez.ejemplo.com", // sin arroba
            "juan.perez@.com", // sin nombre de dominio
            "juan..perez@com", // sin punto en dominio
            "juan.perez@com.", // punto al final
            "juan.perez@.com", // punto al inicio
            "juan  .123@ejemplo.com", // con espacios
    })
    @DisplayName("actualizaEmpleado campo correo invalido - Retorna 400")
    @WithMockUser(username = TEST_RFC, roles = {TEST_ADMIN_ROLE})
    void actualizaEmpleado_campoCorreoInvalido_retorna_400(String correo) throws Exception {

        EmpleadoUpdateRequest req = TestDataBuilder.crearEmpleadoUpdateRequest(
                TEST_RFC,
                TEST_NOMBRE,
                TEST_APELLIDOS,
                correo
        );

        mvc.perform(patch("/empleado/" + TEST_ID)
                        .content(asJsonString(req))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(empleadoService, never()).actualizar(anyInt(), any());
    }
}