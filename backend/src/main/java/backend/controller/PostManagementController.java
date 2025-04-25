package backend.controller;

import backend.exception.PostManagementNotFoundException;
import backend.model.Comment;
import backend.model.NotificationModel;
import backend.model.PostManagementModel;
import backend.repository.NotificationRepository;
import backend.repository.PostManagementRepository;
import backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
public class PostManagementController {
    @Autowired
    private PostManagementRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${media.upload.dir}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestParam String userID,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam List<MultipartFile> mediaFiles) {

        if (mediaFiles.size() < 1 || mediaFiles.size() > 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You must upload between 1 and 3 media files.");
        }

        // Resolve the upload directory as an absolute path
        final File uploadDirectory = new File(uploadDir.isBlank() ? uploadDir : System.getProperty("user.dir"), uploadDir);

        // Ensure the upload directory exists
        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();
            if (!created) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create upload directory.");
            }
        }

        List<String> mediaUrls = mediaFiles.stream()
                .filter(file -> file.getContentType().matches("image/(jpeg|png|jpg)|video/mp4"))
                .map(file -> {
                    try {
                        // Generate a unique filename
                        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
                        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + extension;

                        Path filePath = uploadDirectory.toPath().resolve(uniqueFileName);
                        file.transferTo(filePath.toFile());
                        return "/media/" + uniqueFileName; // URL to access the file
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
                    }
                })
                .collect(Collectors.toList());

        PostManagementModel post = new PostManagementModel();
        post.setUserID(userID);
        post.setTitle(title);
        post.setDescription(description);
        post.setMedia(mediaUrls);

        PostManagementModel savedPost = postRepository.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    @GetMapping
    public List<PostManagementModel> getAllPosts() {
        return postRepository.findAll();
    }

    @GetMapping("/user/{userID}")
    public List<PostManagementModel> getPostsByUser(@PathVariable String userID) {
        return postRepository.findAll().stream()
                .filter(post -> post.getUserID().equals(userID))
                .collect(Collectors.toList());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable String postId) {
        PostManagementModel post = postRepository.findById(postId)
                .orElseThrow(() -> new PostManagementNotFoundException("Post not found: " + postId));
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{postId}/like")
    public ResponseEntity<PostManagementModel> likePost(@PathVariable String postId, @RequestParam String userID) {
        return postRepository.findById(postId)
                .map(post -> {
                    post.getLikes().put(userID, !post.getLikes().getOrDefault(userID, false));
                    postRepository.save(post);

                    // Create a notification for the post owner
                    if (!userID.equals(post.getUserID())) {
                        String userFullName = userRepository.findById(userID)
                                .map(user -> user.getFullname())
                                .orElse("Someone");
                        String message = String.format("%s liked your %s post", userFullName, post.getTitle());
                        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        NotificationModel notification = new NotificationModel(post.getUserID(), message, false, currentDateTime);
                        notificationRepository.save(notification);
                    }

                    return ResponseEntity.ok(post);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<PostManagementModel> addComment(@PathVariable String postId, @RequestBody Map<String, String> request) {
        String userID = request.get("userID");
        String content = request.get("content");

        return postRepository.findById(postId)
                .map(post -> {
                    Comment comment = new Comment();
                    comment.setId(UUID.randomUUID().toString());
                    comment.setUserID(userID);
                    comment.setContent(content);

                    // Fetch user's full name
                    String userFullName = userRepository.findById(userID)
                            .map(user -> user.getFullname())
                            .orElse("Anonymous");
                    comment.setUserFullName(userFullName);

                    post.getComments().add(comment);
                    postRepository.save(post);

                    // Create a notification for the post owner
                    if (!userID.equals(post.getUserID())) {
                        String message = String.format("%s commented on your post: %s", userFullName, post.getTitle());
                        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        NotificationModel notification = new NotificationModel(post.getUserID(), message, false, currentDateTime);
                        notificationRepository.save(notification);
                    }

                    return ResponseEntity.ok(post);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<PostManagementModel> updateComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestBody Map<String, String> request) {
        String userID = request.get("userID");
        String content = request.get("content");

        return postRepository.findById(postId)
                .map(post -> {
                    post.getComments().stream()
                            .filter(comment -> comment.getId().equals(commentId) && comment.getUserID().equals(userID))
                            .findFirst()
                            .ifPresent(comment -> comment.setContent(content));
                    postRepository.save(post);
                    return ResponseEntity.ok(post);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File size exceeds the maximum limit!");
    }
}
