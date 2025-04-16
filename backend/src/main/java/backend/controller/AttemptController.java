package backend.controller;

import backend.model.AttemptModel;
import backend.model.NotificationModel;
import backend.model.QuizModel;
import backend.repository.AttemptRepository;
import backend.repository.NotificationRepository;
import backend.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attempts")
public class AttemptController {
    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private QuizRepository quizRepository;

    @PostMapping
    public AttemptModel saveAttempt(@RequestBody AttemptModel attempt) {
        AttemptModel savedAttempt = attemptRepository.save(attempt);

        // Fetch the quiz title using the quizID
        QuizModel quiz = quizRepository.findById(attempt.getQuizID())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + attempt.getQuizID()));

        // Create a notification for the user
        String message = "You scored " + attempt.getScore() + "% on the quiz \"" + quiz.getTitle() + "\"!";
        NotificationModel notification = new NotificationModel(
                attempt.getUserID(),
                message,
                false,
                java.time.LocalDateTime.now().toString()
        );
        notificationRepository.save(notification);

        return savedAttempt;
    }
}
