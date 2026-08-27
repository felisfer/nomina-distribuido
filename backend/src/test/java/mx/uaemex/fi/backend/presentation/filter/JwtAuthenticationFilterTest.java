package mx.uaemex.fi.backend.presentation.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.uaemex.fi.backend.logic.service.CustomUserDetailsService;
import mx.uaemex.fi.backend.logic.service.JwtService;
import mx.uaemex.fi.backend.logic.service.UserDetailsAdapter;
import mx.uaemex.fi.backend.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

import static mx.uaemex.fi.backend.util.TestDataBuilder.TEST_RFC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para JwtAuthenticationFilter
 * Verifica el filtro de autenticación basado en JWT
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - Pruebas de filtro de seguridad JWT")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Limpiar el contexto de seguridad antes de cada prueba
        SecurityContextHolder.clearContext();

        // Crear UserDetails de prueba
        userDetails = new UserDetailsAdapter(TestDataBuilder.crearEmpleado(), TestDataBuilder.TEST_HASHED_PASSWORD);
    }

    // ==================== PRUEBAS DE shouldNotFilter ====================

    @Test
    @DisplayName("shouldNotFilter - Ruta /auth/ retorna true")
    void shouldNotFilter_rutaAuth_retornaTrue() {
        // Arrange
        when(request.getServletPath()).thenReturn("/auth/login");

        // Act
        boolean resultado = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(resultado, "Las rutas /auth/* no deben ser filtradas");
    }

    @Test
    @DisplayName("shouldNotFilter - Ruta admin retorna false")
    void shouldNotFilter_rutaAdmin_retornaFalse() {
        // Arrange
        when(request.getServletPath()).thenReturn("/admin/dashboard");

        // Act
        boolean resultado = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(resultado, "Las rutas protegidas deben ser filtradas");
    }

    // ==================== PRUEBAS DE doFilterInternal ====================

    @Test
    @DisplayName("doFilterInternal - Token válido autentica usuario")
    void doFilterInternal_tokenValido_autenticaUsuario() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtService.getRfcFromToken(TEST_TOKEN)).thenReturn(TEST_RFC);
        when(userDetailsService.loadUserByUsername(TEST_RFC)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateToken(TEST_TOKEN);
        verify(jwtService).getRfcFromToken(TEST_TOKEN);
        verify(userDetailsService).loadUserByUsername(TEST_RFC);
        verify(filterChain).doFilter(request, response);

        // Verificar que se estableció la autenticación
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
                "El contexto de seguridad debe tener autenticación");
        assertEquals(TEST_RFC, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("doFilterInternal - Token válido usuario inexistente detiene filtros")
    void doFilterInternal_tokenValidoUsuarioInexistente_autenticaUsuario() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtService.getRfcFromToken(TEST_TOKEN)).thenReturn(TEST_RFC);
        when(userDetailsService.loadUserByUsername(TEST_RFC)).thenThrow(new UsernameNotFoundException("Empleado no encontrado"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateToken(TEST_TOKEN);
        verify(jwtService).getRfcFromToken(TEST_TOKEN);
        verify(userDetailsService).loadUserByUsername(TEST_RFC);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Sin token continúa sin autenticar")
    void doFilterInternal_sinToken_continuaSinAutenticar() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "No debe haber autenticación sin token");
    }

    @Test
    @DisplayName("doFilterInternal - Token inválido detiene filtros")
    void doFilterInternal_tokenInvalido_redireccionaLogin() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateToken(TEST_TOKEN);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Token expirado detiene filtros")
    void doFilterInternal_tokenExpirado_redireccionaLogin() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(false); // Token expirado es inválido

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateToken(TEST_TOKEN);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Token válido establece SecurityContext")
    void doFilterInternal_tokenValido_estableceSecurityContext() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtService.getRfcFromToken(TEST_TOKEN)).thenReturn(TEST_RFC);
        when(userDetailsService.loadUserByUsername(TEST_RFC)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(TEST_RFC, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
    }

    @Test
    @DisplayName("doFilterInternal - Authorization header extrae token")
    void doFilterInternal_authorizationHeader_extraeToken() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtService.getRfcFromToken(TEST_TOKEN)).thenReturn(TEST_RFC);
        when(userDetailsService.loadUserByUsername(TEST_RFC)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateToken(TEST_TOKEN);
    }

    @Test
    @DisplayName("doFilterInternal - Sin header de autorización retorna")
    void doFilterInternal_sinHeaderAutorizacion_retorna() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Token inválido retorna")
    void doFilterInternal_tokenInvalido_retorna() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateToken(TEST_TOKEN);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Tipo de autorización inválido retorna")
    void doFilterInternal_authorizationTokenTypeInvalido_retorna() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Something " + TEST_TOKEN);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService, never()).validateToken(TEST_TOKEN);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Token vacío retorna")
    void doFilterInternal_authorizationTokenVacio_retorna() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService, never()).validateToken(TEST_TOKEN);
        verify(filterChain).doFilter(request, response);
    }
}

