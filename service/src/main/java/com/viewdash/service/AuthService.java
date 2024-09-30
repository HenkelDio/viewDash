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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${api.security.token.secret")
    private String secret;

    @Autowired
    UserRepository userRepository;

    @Qualifier("mongoTemplate")
    @Autowired
    private MongoTemplate mongoTemplate;


    public ResponseEntity<?> login(String email, String password) {
        logger.info("Logging In {}", email);


        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

            if(!user.getStatus().equals(User.STATUS.ACTIVE)) {
                return ResponseEntity.badRequest().build();
            }

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if(passwordEncoder.matches(password, user.getPassword())) {
                String token = generateToken(user);
                user.setToken(token);
                return ResponseEntity.ok(user);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }


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

    public ResponseEntity<?> definePassword(String password, User user) {
        logger.info("Defining new password to user: {}", user.getDocument());

        try {
            Query query = new Query(Criteria.where("document").is(user.getDocument()));

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            Update update = new Update().set("password", passwordEncoder.encode(password));
            mongoTemplate.updateFirst(query, update, User.class);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


}
