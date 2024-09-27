package com.viewdash.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.viewdash.document.DTO.RegisterUserRequestDTO;
import com.viewdash.document.ResponseAuthDTO;
import com.viewdash.document.User;
import com.viewdash.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${api.security.token.secret")
    private String secret;

    @Autowired
    UserRepository userRepository;


    public ResponseEntity<?> login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(passwordEncoder.matches(password, user.getPassword())) {
            String token = generateToken(user);
            return ResponseEntity.ok(new ResponseAuthDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("login")
                    .withSubject(user.getEmail())
                    .withClaim("document", user.getDocument())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);

        } catch (JWTCreationException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while generating token");
        }
    }

    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("login")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    public ResponseEntity<?> register(RegisterUserRequestDTO registerUserRequestDTO) {
        Optional<User> user = userRepository.findByEmail(registerUserRequestDTO.email())
                .or(() -> userRepository.findByDocument(registerUserRequestDTO.document()));

        if(user.isEmpty()) {
            User newUser = new User();
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            newUser.setPassword(passwordEncoder.encode(registerUserRequestDTO.password()));
            newUser.setEmail(registerUserRequestDTO.email());
            newUser.setName(registerUserRequestDTO.name());
            newUser.setDocument(registerUserRequestDTO.document());
            userRepository.save(newUser);

            String token = generateToken(newUser);
            return ResponseEntity.ok(new ResponseAuthDTO(newUser.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }


}
