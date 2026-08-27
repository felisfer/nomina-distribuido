package mx.uaemex.fi.backend.presentation.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mx.uaemex.fi.backend.logic.exception.InvalidCredentialsException;
import mx.uaemex.fi.backend.logic.service.AuthService;
import mx.uaemex.fi.backend.presentation.dto.EmpleadoResponse;
import mx.uaemex.fi.backend.presentation.dto.JwtResponse;
import mx.uaemex.fi.backend.presentation.dto.LoginRequest;
import mx.uaemex.fi.backend.presentation.dto.RegisterRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<@NonNull EmpleadoResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<@NonNull JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        var res = authService.login(request);
        return ResponseEntity.ok().body(res);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<@NonNull Void> handleInvalidCredentials() {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Credenciales inválidas")).build();
    }
}
