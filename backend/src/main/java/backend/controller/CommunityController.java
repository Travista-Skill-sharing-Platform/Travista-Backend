package backend.controller;

import backend.exception.CommunityNotFoundException;
import backend.model.CommunityModel;
import backend.model.NoticeModel;
import backend.model.UserModel;
import backend.repository.CommunityRepository;
import backend.repository.NoticeRepository;
import backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/communities")
public class CommunityController {

    private static final Logger logger = LoggerFactory.getLogger(CommunityController.class);

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<CommunityModel> getAllCommunities() {
        return communityRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createCommunity(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String userId = request.get("userId"); // Ensure this matches the frontend key

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User ID is required"));
        }

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CommunityModel community = new CommunityModel();
        community.setName(name);
        community.setOwnerId(userId); // Set the owner ID
        community.getUsers().add(user); // Associate the user with the community

        CommunityModel savedCommunity = communityRepository.save(community);

        return ResponseEntity.ok(savedCommunity);
    }

    @PostMapping("/{communityId}/notices")
    public ResponseEntity<?> addNotice(@PathVariable String communityId, @RequestBody NoticeModel notice) {
        logger.info("Adding notice to community with ID: {}", communityId);

        try {
            CommunityModel community = communityRepository.findById(communityId)
                    .orElseThrow(() -> {
                        logger.error("Community with ID {} not found", communityId);
                        return new CommunityNotFoundException("Community not found");
                    });

            if (notice.getUserId() == null || notice.getUserId().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "User ID is required"));
            }

            noticeRepository.save(notice);
            community.getNotices().add(notice);
            communityRepository.save(community);

            return ResponseEntity.ok(notice);
        } catch (CommunityNotFoundException e) {
            logger.error("Error adding notice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Community not found", "communityId", communityId));
        }
    }

    @PutMapping("/notices/{noticeId}")
    public ResponseEntity<?> updateNotice(@PathVariable String noticeId, @RequestBody NoticeModel updatedNotice) {
        try {
            // Find the community containing the notice
            CommunityModel community = communityRepository.findAll().stream()
                    .filter(c -> c.getNotices().stream().anyMatch(n -> n.getId().equals(noticeId)))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Notice not found in any community"));

            // Find and update the notice in the community's notices array
            community.getNotices().forEach(notice -> {
                if (notice.getId().equals(noticeId)) {
                    notice.setTitle(updatedNotice.getTitle());
                    notice.setContent(updatedNotice.getContent());
                }
            });

            // Save the updated community back to the database
            communityRepository.save(community);

            return ResponseEntity.ok(Map.of("message", "Notice updated successfully"));
        } catch (Exception e) {
            logger.error("Error updating notice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update the notice"));
        }
    }

    @GetMapping("/{communityId}/notices")
    public List<NoticeModel> getNotices(@PathVariable String communityId) {
        CommunityModel community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException("Community not found"));
        return community.getNotices(); // Only notices for the given community ID are returned
    }

    @GetMapping("/notices/{noticeId}")
    public ResponseEntity<?> getNoticeById(@PathVariable String noticeId) {
        try {
            NoticeModel notice = noticeRepository.findById(noticeId)
                    .orElseThrow(() -> new RuntimeException("Notice not found"));
            return ResponseEntity.ok(notice);
        } catch (Exception e) {
            logger.error("Error fetching notice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notice not found"));
        }
    }

    @PutMapping("/{communityId}/addUser")
    public ResponseEntity<?> addUserToCommunity(@PathVariable String communityId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CommunityModel community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException("Community not found"));
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        community.getUsers().add(user);
        communityRepository.save(community);
        return ResponseEntity.ok(Map.of("message", "User added to community successfully"));
    }

    @PutMapping("/{communityId}/removeUser")
    public ResponseEntity<?> removeUserFromCommunity(@PathVariable String communityId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CommunityModel community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException("Community not found"));

        boolean removed = community.getUsers().removeIf(user -> user.getId().equals(userId));
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found in community"));
        }

        communityRepository.save(community);
        return ResponseEntity.ok(Map.of("message", "User removed from community successfully"));
    }

    @GetMapping("/{communityId}/users")
    public List<UserModel> getCommunityUsers(@PathVariable String communityId) {
        CommunityModel community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException("Community not found"));
        return community.getUsers();
    }

    @GetMapping("/{communityId}")
    public ResponseEntity<?> getCommunityDetails(@PathVariable String communityId) {
        CommunityModel community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException("Community not found"));

        // Create a response object that includes the ownerId and name
        Map<String, Object> response = new HashMap<>();
        response.put("ownerId", community.getOwnerId());
        response.put("name", community.getName()); // Include the community name
        // Add any other community details you need

        return ResponseEntity.ok(response);
    }

    @GetMapping("/myCommunities/{userId}")
    public List<CommunityModel> getUserCommunities(@PathVariable String userId) {
        return communityRepository.findAll().stream()
                .filter(community -> community.getUsers().stream()
                        .anyMatch(user -> user.getId().equals(userId)))
                .toList();
    }

    @DeleteMapping("/{communityId}/notices/{noticeId}")
    public ResponseEntity<?> deleteNoticeFromCommunity(@PathVariable String communityId, @PathVariable String noticeId) {
        CommunityModel community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException("Community not found"));

        boolean removed = community.getNotices().removeIf(notice -> notice.getId().equals(noticeId));
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notice not found in community"));
        }

        communityRepository.save(community);
        return ResponseEntity.ok(Map.of("message", "Notice deleted successfully"));
    }
}
