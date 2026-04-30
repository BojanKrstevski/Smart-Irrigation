package irrigation_system.controller;

import irrigation_system.dto.LoginRequest;
import irrigation_system.dto.RegisterRequest;
import irrigation_system.dto.UserResponse;
import irrigation_system.mapper.DtoMapper;
import irrigation_system.model.User;
import irrigation_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        User registeredUser = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toUserResponse(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return ResponseEntity.ok(DtoMapper.toUserResponse(user));
    }
}
