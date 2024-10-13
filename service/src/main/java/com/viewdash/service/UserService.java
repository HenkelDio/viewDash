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
    public static final String INACTIVE = "INACTIVE";
    public static final String ACTIVE = "ACTIVE";

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

    public ResponseEntity<List<User>> findAllUsers(String username, String status) {
        logger.info("searching all users by username: {}", username);

        Query query = new Query(Criteria.where("status").is(status));
        return ResponseEntity.ok(mongoTemplate.find(query, User.class));
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

            user.setDocument(user.getDocument().replace("-", "").replace(".", ""));
            user.setStatus(User.STATUS.ACTIVE);

            User.Permissions permissions = new User.Permissions();
            permissions.setFirstLogin(true);
            user.setPermissions(permissions);

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

        user.setDocument(user.getDocument().replace("-", "").replace(".", ""));
        Optional<User> userOptional = userRepository.findByDocument(user.getDocument());
        if(userOptional.isPresent()) {
            Query query = new Query(Criteria.where("document").is(user.getDocument()));
            Update update = new Update();
            update.set("name", user.getName());
            update.set("email", user.getEmail());
            update.set("departments", user.getDepartments());
            update.set("role", user.getRole());

            mongoTemplate.updateFirst(query, update, User.class);
            return ResponseEntity.ok(user);
        }

        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<?> deleteUser(String username, String userDocument) {
        logger.info("deleting user: {}, by user {}", userDocument, username);

        try {
            Optional<User> userOptional = userRepository.findByDocument(userDocument);

            if(userOptional.isPresent()) {
                userRepository.deleteUserByDocument(userDocument);
                logger.info("user deleted");
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("error deleting user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<?> disableUser(String username, String userDocument) {
        logger.info("disabling user: {}, by user {}", userDocument, username);

        try {
            Optional<User> userOptional = userRepository.findByDocument(userDocument);

            if(userOptional.isPresent()) {
                Query query = new Query(Criteria.where("document").is(userDocument));
                Update update = new Update().set("status", INACTIVE);
                mongoTemplate.updateFirst(query, update, User.class);

                logger.info("user disabled");
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("error disabling user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<?> reactivateUser(String username, String userDocument) {
        logger.info("Reactivating user: {}, by user {}", userDocument, username);

        try {
            Optional<User> userOptional = userRepository.findByDocument(userDocument);

            if(userOptional.isPresent()) {
                Query query = new Query(Criteria.where("document").is(userDocument));
                Update update = new Update().set("status", ACTIVE);
                mongoTemplate.updateFirst(query, update, User.class);

                logger.info("user reactivated");
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("error reactivating user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<?> updateUserName(String username, String document, String name) {
        logger.info("updating user name: {}, by user {}", name, username);

        try {
            Query query = new Query(Criteria.where("document").is(document));
            Update update = new Update().set("name", name);
            mongoTemplate.updateFirst(query, update, User.class);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("error updating user name", e);
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<?> resetPassword(String username, String userDocument) {
        logger.info("reset password for user: {}, by user {}", username, userDocument);

        try {
            userDocument = userDocument.replace(".", "").replace("-", "");
            Optional<User> userOptional = userRepository.findByDocument(userDocument);

            if(userOptional.isPresent()) {
                String token = authService.generateToken(userOptional.get());
                emailService.sendResetPasswordEmail(userOptional.get().getEmail(), "Redefinir sua senha", userOptional.get().getName(), token);

                logger.info("Sending email");
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("error resetting password", e);
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<?> countUsers() {
        Query query = new Query(Criteria.where("status").is(ACTIVE));
        return ResponseEntity.ok(mongoTemplate.count(query, User.class));
    }

    public ResponseEntity<?> setNotFirstLogin(User principal) {
        logger.info("Setting not first login to " + principal.getDocument());

        Query query = new Query(Criteria.where("document").is(principal.getDocument()));
        Update update = new Update().set("permissions.firstLogin", false);

        try {
            mongoTemplate.updateFirst(query, update, User.class);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("error setting not first login", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
