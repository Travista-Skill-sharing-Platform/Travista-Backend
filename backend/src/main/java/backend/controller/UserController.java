package backend.controller;

import backend.exception.UserNotFoundException;
import backend.model.NotificationModel;
import backend.model.UserModel;
import backend.repository.NotificationRepository;
import backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:3000")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    //Insert
    @PostMapping("/user")
    public ResponseEntity<?> newUserModel(@RequestBody UserModel newUserModel) {
        if (userRepository.existsByEmail(newUserModel.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already exists!"));
        }
        UserModel savedUser = userRepository.save(newUserModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}
