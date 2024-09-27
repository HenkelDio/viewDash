package com.viewdash.service;

import com.viewdash.document.User;
import com.viewdash.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    AuthService authService;

    @Qualifier("mongoTemplate")
    @Autowired
    private MongoTemplate mongoTemplate;


    public ResponseEntity<User> findUserByDocument(String document, String username) {
        logger.info("searching user by document: {}, user {}", document, username);
        Optional<User> user = userRepository.findByDocument(document);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<List<User>> findAllUsers(String username) {
        logger.info("searching all users by username: {}", username);
        return ResponseEntity.ok(userRepository.findAll());
    }

    public ResponseEntity<User> createUser(User user, String usernamePrincipal) {
        logger.info("creating user: {}, by user {}", user.getDocument(), usernamePrincipal);

        try {

            Optional<User> userOptional = userRepository.findByDocument(user.getDocument())
                    .or(() -> userRepository.findByEmail(user.getEmail()));

            if(userOptional.isPresent()) {
                logger.info("user already exists");
                return ResponseEntity.badRequest().build();
            }

            userRepository.save(user);

            logger.info("user created, sending email");

            String token = authService.generateToken(user);
            emailService.sendHtmlEmail(user.getEmail(), "Crie sua senha", user.getName(), token);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("error creating user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<User> updateUser(User user, String usernamePrincipal) {
        logger.info("updating user: {}, by user {}", user.getDocument(), usernamePrincipal);

        Optional<User> userOptional = userRepository.findByDocument(user.getDocument());
        if(userOptional.isPresent()) {
            Query query = new Query(Criteria.where("document").is(user.getDocument()));
            Update update = new Update();
            update.set("name", user.getName());
            update.set("email", user.getEmail());
            update.set("department", user.getDepartment());
            update.set("role", user.getRole());

            mongoTemplate.updateFirst(query, update, User.class);
            return ResponseEntity.ok(user);
        }

        return ResponseEntity.badRequest().build();
    }
}
