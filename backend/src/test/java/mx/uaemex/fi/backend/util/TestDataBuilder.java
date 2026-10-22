package mx.uaemex.fi.backend.util;

import mx.uaemex.fi.backend.persistence.model.Acceso;
import mx.uaemex.fi.backend.persistence.model.Empleado;
import mx.uaemex.fi.backend.persistence.model.Nomina;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoResponse;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoUpdateRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Clase de utilidad para crear datos de prueba reutilizables.
 * Proporciona métodos factory para crear instancias de objetos de dominio
 * con datos de prueba consistentes.
 */
public class TestDataBuilder {

    // Constantes para datos de prueba
    public static final Integer TEST_ID = 1;
    public static final String TEST_RFC = "AAAA012345XXX";
    public static final String TEST_RFC_2 = "BBBB987654YYY";
    public static final String TEST_NOMBRE = "JUAN";
    public static final String TEST_APELLIDOS = "PEREZ LOPEZ";
    public static final String TEST_CORREO = "juan.perez@test.com";
    public static final String TEST_CORREO_2 = "maria.lopez@test.com";
    public static final String TEST_PASSWORD = "Qwertyuiop1*";
    public static final String TEST_HASHED_PASSWORD = "$2a$10$dummyHashedPasswordForTesting";
    public static final String TEST_ADMIN_ROLE = "ADMIN";

    // Constantes para datos de nómina
    public static final Double TEST_SALARIO_MINIMO = 500.00;
    public static final Double TEST_SALARIO_RANGO_2 = 5000.00;

    public static final LocalDate TEST_FECHA_INICIO = LocalDate.of(2025, 1, 1);
    public static final LocalDate TEST_FECHA_FIN = LocalDate.of(2025, 1, 15);

    /**
     * Crea un empleado de prueba básico sin acceso ni nóminas
     */
    public static Empleado crearEmpleado() {
        var empleado = new Empleado();
        empleado.setId(TEST_ID);
        empleado.setRfc(TEST_RFC);
        empleado.setNombres(TEST_NOMBRE);
        empleado.setApellidos(TEST_APELLIDOS);
        empleado.setCorreo(TEST_CORREO);
        return empleado;
    }

    /**
     * Crea un empleado de prueba con ID personalizado
     */
    public static Empleado crearEmpleado(Integer id) {
        var empleado = new Empleado();
        empleado.setId(id);
        empleado.setRfc(TEST_RFC);
        empleado.setNombres(TEST_NOMBRE);
        empleado.setApellidos(TEST_APELLIDOS);
        empleado.setCorreo(TEST_CORREO);
        return empleado;
    }

    /**
     * Crea un empleado de prueba con todos los campos personalizados
     */
    public static Empleado crearEmpleado(Integer id, String rfc, String nombre, String apellidos, String correo) {
        var empleado = new Empleado();
        empleado.setId(id);
        empleado.setRfc(rfc);
        empleado.setNombres(nombre);
        empleado.setApellidos(apellidos);
        empleado.setCorreo(correo);
        return empleado;
    }

    /**
     * Crea un objeto Acceso de prueba
     */
    public static Acceso crearAcceso(Empleado empleado) {
        var acceso = new Acceso();
        acceso.setId(1);
        acceso.setIdEmpleado(empleado.getId());
        acceso.setHashedPassword(TEST_HASHED_PASSWORD);
        return acceso;
    }

    /**
     * Crea una nómina de prueba con salario por defecto
     */
    public static Nomina crearNomina(Empleado empleado) {
        return crearNomina(empleado, TEST_SALARIO_RANGO_2);
    }

    /**
     * Crea una nómina de prueba con salario personalizado
     */
    public static Nomina crearNomina(Empleado empleado, Double salario) {
        var nomina = new Nomina();
        nomina.setId(1);
        nomina.setEmpleado(empleado);
        nomina.setSalarioBruto(salario);
        nomina.setExcedente(salario - 746.05);
        nomina.setCuotaFija(14.32);
        nomina.setPorcentaje(0.0640);
        nomina.setPeriodoInicio(LocalDate.of(2025, 1, 1));
        nomina.setPeriodoFin(LocalDate.of(2025, 1, 15));
        return nomina;
    }

    /**
     * Crea una lista de empleados de prueba
     */
    public static java.util.List<Empleado> crearListaEmpleados() {
        java.util.List<Empleado> empleados = new ArrayList<>();
        empleados.add(crearEmpleado(1));
        empleados.add(crearEmpleado(2, TEST_RFC_2, "MARIA", "LOPEZ GARCIA", TEST_CORREO_2));
        return empleados;
    }

    public static EmpleadoResponse crearEmpleadoResponse() {
        return new EmpleadoResponse(1, TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO);
    }

    /**
     * Crea una lista de empleados de prueba
     */
    public static java.util.List<EmpleadoResponse> crearListaEmpleadosResponse() {
        java.util.List<EmpleadoResponse> empleados = new ArrayList<>();
        empleados.add(new EmpleadoResponse(1, TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO));
        empleados.add(new EmpleadoResponse(2, TEST_RFC_2, "MARIA", "LOPEZ GARCIA", TEST_CORREO_2));
        return empleados;
    }

    public static EmpleadoUpdateRequest crearEmpleadoUpdateRequest() {
        return new EmpleadoUpdateRequest(TEST_RFC, TEST_NOMBRE, TEST_APELLIDOS, TEST_CORREO);
    }

    public static EmpleadoUpdateRequest crearEmpleadoUpdateRequest(
            String rfc,
            String nombre,
            String apellidos,
            String correo
    ) {
        return new EmpleadoUpdateRequest(rfc, nombre, apellidos, correo);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

