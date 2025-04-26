package backend.controller;

import backend.exception.QuizNotFoundException;
import backend.model.QuizModel;
import backend.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    @Autowired
    private QuizRepository quizRepository;

    @PostMapping
    public QuizModel createQuiz(@RequestBody QuizModel quiz) {
        if (quiz.getUserID() == null || quiz.getUserID().isEmpty()) {
            throw new IllegalArgumentException("UserID is required to create a quiz.");
        }
        return quizRepository.save(quiz);
    }

    @GetMapping
    public List<QuizModel> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @GetMapping("/{id}")
    public QuizModel getQuizById(@PathVariable String id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new QuizNotFoundException("Quiz not found with id " + id));
    }