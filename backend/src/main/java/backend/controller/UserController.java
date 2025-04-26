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

//User Login
@PostMapping("/login")
public ResponseEntity<Map<String, Object>> login(@RequestBody UserModel loginDetails) {
    System.out.println("Login attempt for email: " + loginDetails.getEmail()); // Log email for debugging

    UserModel user = userRepository.findByEmail(loginDetails.getEmail())
            .orElseThrow(() -> new UserNotFoundException("Email not found: " + loginDetails.getEmail()));

    if (user.getPassword().equals(loginDetails.getPassword())) {
        System.out.println("Login successful for email: " + loginDetails.getEmail()); // Log success
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login Successful");
        response.put("id", user.getId());
        response.put("fullName", user.getFullname());
        return ResponseEntity.ok(response);
    } else {
        System.out.println("Invalid password for email: " + loginDetails.getEmail()); // Log invalid password
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials!"));
    }
}

//Display
@GetMapping("/user")
List<UserModel> getAllUsers() {
    return userRepository.findAll();
}

@GetMapping("/user/{id}")
UserModel getUserId(@PathVariable String id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
}
