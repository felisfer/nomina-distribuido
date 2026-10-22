package mx.uaemex.fi.backend.presentation.dto;

import jakarta.validation.constraints.*;
import mx.uaemex.fi.backend.logic.validation.UniqueEmail;

@NotNull
public record EmpleadoUpdateRequest(
        @Pattern(
                regexp = "^([A-Z&Ñ]{4}[0-9]{6}[A-Z0-9]{3})?$",
                message = "El RFC debe tener el siguiente formato: LLLLAAMMDDXXX; en donde L=(A-Z) letra o caracteres & o Ñ, AA=año, MM=mes, DD=día, XXX=alfanumérico; todo en mayúsculas y sin espacios"
        )
        String rfc,
        @Pattern(
                regexp = "^([A-ZÁÉÍÓÚÑ]+( [A-ZÁÉÍÓÚÑ]+)*)?$",
                message = "El nombre debe contener solo letras mayúsculas, un espacio de separación y sin espacios extras al inicio o final"
        )
        String nombre,
        @Pattern(
                regexp = "^([A-ZÁÉÍÓÚÑ]+( [A-ZÁÉÍÓÚÑ]+)*)?$",
                message = "Los apellidos deben contener solo letras mayúsculas, un espacio de separación y sin espacios extras al inicio o final"
        )
        String apellidos,
        @Email(message = "El correo debe tener un formato válido")
        @UniqueEmail
        String correo
) {
}
