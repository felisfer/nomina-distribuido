package mx.uaemex.fi.backend.presentation.controller;

import mx.uaemex.fi.backend.config.SecurityConfig;
import mx.uaemex.fi.backend.logic.exception.InvalidCredentialsException;
import mx.uaemex.fi.backend.logic.service.*;
import mx.uaemex.fi.backend.persistence.repository.AccesoRepository;
import mx.uaemex.fi.backend.persistence.repository.EmpleadoRepository;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoResponse;
import mx.uaemex.fi.backend.presentation.dto.JwtResponse;
import mx.uaemex.fi.backend.presentation.dto.LoginRequest;
import mx.uaemex.fi.backend.presentation.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static mx.uaemex.fi.backend.util.TestDataBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class})
class AuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtServiceImp jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private EmpleadoRepository empleadoRepository;

    @MockitoBean
    private AccesoRepository accesoRepository;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("registrarEmpleado - Válido retorna 200")
    void registrarEmpleado_valido_retorna200() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, false, null, null);

        EmpleadoResponse empleadoResponse = crearEmpleadoResponse();

        when(authService.register(registerRequest)).thenReturn(empleadoResponse);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id").value(empleadoResponse.id()),
                        jsonPath("$.rfc").value(empleadoResponse.rfc()),
                        jsonPath("$.nombre").value(empleadoResponse.nombre()),
                        jsonPath("$.apellidos").value(empleadoResponse.apellidos()),
                        jsonPath("$.correo").value(empleadoResponse.correo())
                );

        verify(authService).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - RFC existente retorna 400 Bad request")
    void registrarEmpleado_rfcExistente_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, false, null, null);

        when(empleadoRepository.existsByRfc(TEST_RFC)).thenReturn(true);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
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
    @DisplayName("registrarEmpleado - RFC inválido retorna 400 Bad request")
    void registrarEmpleado_rfcInvalido_retorna400(String rfc) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(rfc, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, false, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - RFC nulo retorna 400 Bad request")
    void registrarEmpleado_rfcNull_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(null, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, false, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
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
    @DisplayName("registrarEmpleado - Nombre inválido retorna 400 Bad request")
    void registrarEmpleado_nombreInvalido_retorna400(String nombre) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, nombre, TEST_APELLIDOS, TEST_CORREO, false, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - Nombre nulo retorna 400 Bad request")
    void registrarEmpleado_nombreNull_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, null, TEST_APELLIDOS, TEST_CORREO, false, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
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
    @DisplayName("registrarEmpleado - Apellidos inválido retorna 400 Bad request")
    void registrarEmpleado_apellidosInvalido_retorna400(String apellidos) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, apellidos, TEST_CORREO, false, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - Apellidos nulo retorna 400 Bad request")
    void registrarEmpleado_apellidosNull_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, null, TEST_CORREO, false, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
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
    @DisplayName("registrarEmpleado - Correo inválido retorna 400 Bad request")
    void registrarEmpleado_correoInvalido_retorna400(String correo) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, correo, false, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - Correo nulo retorna 400 Bad request")
    void registrarEmpleado_correoNull_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, null, false, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - Administrador sin password retorna 400 Bad request")
    void registrarEmpleado_administradorSinPassword_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, true, null, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - Administrador password diferente retorna 400 Bad request")
    void registrarEmpleado_administradorPasswordDiferente_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, true, TEST_PASSWORD, "OtroPassword");

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - Administrador con confirm password nulo retorna 400 Bad request")
    void registrarEmpleado_administradorConfirmPasswordNull_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, true, TEST_PASSWORD, null);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("registrarEmpleado - Administrador con password nulo retorna 400 Bad request")
    void registrarEmpleado_administradorPasswordNull_retorna400() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, true, null, TEST_PASSWORD);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @ParameterizedTest
    @CsvSource({
            "Qwertyuio*1", // 11 caracteres
            "Qwertyuiop*", // sin un digito
            "Qwertyuiop1", // sin un caracter especial
            "qwertyuiop*1", // sin una letra mayúscula
            "QWERTYUIOP*1", // sin una letra minúscula
    })
    @DisplayName("registrarEmpleado - Administrador con password inválida retorna 400 Bad request")
    void registrarEmpleado_administradorPasswordInvalida_retorna400(String password) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO, true, password, password);

        mvc.perform(post("/auth/register")
                        .with(csrf())
                        .content(asJsonString(registerRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(registerRequest);
    }

    @Test
    @DisplayName("login - Credenciales válidas retorna 200")
    void login_credencialesValidas_retorna200() throws Exception {
        LoginRequest loginRequest = new LoginRequest(TEST_CORREO, TEST_PASSWORD);
        JwtResponse jwtResponse = new JwtResponse("TOKEN", "Bearer", 3600000L);

        when(authService.login(loginRequest)).thenReturn(jwtResponse);

        mvc.perform(post("/auth/login")
                .with(csrf())
                .content(asJsonString(loginRequest))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpectAll(
                        header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.token").value(jwtResponse.token()),
                        jsonPath("$.type").value(jwtResponse.type()),
                        jsonPath("$.expiresInMs").value(jwtResponse.expiresInMs())
                );

        verify(authService).login(loginRequest);
    }

    @Test
    @DisplayName("login - Credenciales inválidas retorna 400")
    void login_credencialesInvalidas_retorna200() throws Exception {
        LoginRequest loginRequest = new LoginRequest(TEST_CORREO, TEST_PASSWORD);

        when(authService.login(loginRequest)).thenThrow(new InvalidCredentialsException("Credenciales incorrectas"));

        mvc.perform(post("/auth/login")
                        .with(csrf())
                        .content(asJsonString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService).login(loginRequest);
    }
}