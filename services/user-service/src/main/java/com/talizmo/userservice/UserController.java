package com.talizmo.userservice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    public record RegisterRequest(@Email @NotBlank String email,
                                  @NotBlank String displayName,
                                  @Size(min = 8) String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record UserDto(Long id, String email, String displayName) {}
    public record LoginResponse(String token, UserDto user) {}

    private final AppUserRepository repo;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public UserController(AppUserRepository repo, PasswordEncoder encoder, TokenService tokens) {
        this.repo = repo; this.encoder = encoder; this.tokens = tokens;

    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody RegisterRequest r) {
        if (repo.existsByEmailIgnoreCase(r.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        AppUser u = repo.save(new AppUser(r.email(), r.displayName(), encoder.encode(r.password())));
        return new UserDto(u.getId(), u.getEmail(), u.getDisplayName());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) {
        AppUser u = repo.findByEmailIgnoreCase(r.email())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));
        return new LoginResponse(tokens.issue(u), new UserDto(u.getId(), u.getEmail(), u.getDisplayName()));
    }

    @GetMapping
    public List<UserDto> all() {
        return repo.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getEmail(), u.getDisplayName())).toList();
    }

    @GetMapping("/{id}")
    public UserDto one(@PathVariable Long id) {
        return repo.findById(id).map(u -> new UserDto(u.getId(), u.getEmail(), u.getDisplayName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}