package com.lost2found.controller;

import com.lost2found.model.LostItem;
import com.lost2found.model.FoundItem;
import com.lost2found.model.User;
import com.lost2found.repository.LostItemRepository;
import com.lost2found.repository.FoundItemRepository;
import com.lost2found.repository.UserRepository;
import com.lost2found.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ReportController {

    @Autowired
    private LostItemRepository lostRepo;

    @Autowired
    private FoundItemRepository foundRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MatchService matchService;

    private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "webp");

    // ------------------ LOST ITEM ------------------
    // ------------------ LOST ITEM ------------------
    @PostMapping("/report/lost")
    public ResponseEntity<?> reportLostItem(
            @RequestParam Long user_id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam(required = false) String event_date,
            @RequestParam(required = false) MultipartFile image,
            Principal principal
    ) {
        try {
            User user = getAuthenticatedUser(principal, user_id);
            String filePath = saveImage(image);

            LostItem item = new LostItem();
            item.setUser(user);
            item.setTitle(title);
            item.setDescription(description);
            item.setLocation(location);
            if (event_date == null || event_date.isBlank()) {
                item.setEventDate(LocalDate.now());
            } else {
                item.setEventDate(LocalDate.parse(event_date, formatter));
            }
            item.setImagePath(filePath);
            item.setStatus(LostItem.Status.OPEN);

            lostRepo.save(item);
            System.out.println("✅ Lost item reported successfully: " + title);

            // 🔹 Trigger AI Matching Engine for LOST item
            try {
                System.out.println("🤖 Triggering AI Matching Engine for Lost Item ID: " + item.getLostId());
                matchService.processNewLostItem(item.getLostId()); // Async matching call
            } catch (Exception e) {
                System.err.println("⚠️ AI Matching Engine failed: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Lost item reported successfully!"));
        } catch (RuntimeException | IOException e) {
            String message = e.getMessage() == null ? "Unable to save lost item" : e.getMessage();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", message));
        }
    }


    // ------------------ FOUND ITEM ------------------
    @PostMapping("/report/found")
    public ResponseEntity<?> reportFoundItem(
            @RequestParam Long user_id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String event_date,
            @RequestParam(required = false) MultipartFile image,
            Principal principal
    ) {
        try {
            User user = getAuthenticatedUser(principal, user_id);
            String filePath = saveImage(image);

            FoundItem item = new FoundItem();
            item.setUser(user);
            item.setTitle(title);
            item.setDescription(description);
            item.setLocation(location);
            item.setEventDate(LocalDate.parse(event_date, formatter));
            item.setImagePath(filePath);
            item.setStatus(FoundItem.Status.OPEN);

            foundRepo.save(item);

            // 🔹 Trigger AI Matching Engine for FOUND item
            try {
                System.out.println("🤖 Triggering AI Matching Engine for Found Item ID: " + item.getFoundId());
                matchService.processNewFoundItem(item.getFoundId()); // Async matching call
            } catch (Exception e) {
                System.err.println("⚠️ AI Matching Engine failed: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Found item reported successfully!"));
        } catch (RuntimeException | IOException e) {
            String msg = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : ("Server error: " + e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", msg));
        }
    }

    // Add this in ReportController.java
    @GetMapping("/user/matches")
    public ResponseEntity<?> getUserMatches(@RequestParam Long user_id, Principal principal) {
        try {
            User user = getAuthenticatedUser(principal, user_id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "matches", matchService.getMatchesForUser(user)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    private User getAuthenticatedUser(Principal principal, Long requestedUserId) {
        if (principal == null) {
            throw new IllegalArgumentException("Authentication required");
        }

        User user = userRepo.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        if (!user.getId().equals(requestedUserId)) {
            throw new IllegalArgumentException("You can only modify your own reports");
        }
        return user;
    }

    private String saveImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Image must be 5 MB or smaller");
        }

        String originalName = image.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.lastIndexOf('.') >= 0) {
            extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_IMAGE_TYPES.contains(extension)) {
            throw new IllegalArgumentException("Only JPG, PNG, and WEBP images are allowed");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        String fileName = UUID.randomUUID() + "." + extension;
        Path destination = uploadPath.resolve(fileName).normalize();
        if (!destination.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid image path");
        }
        Files.copy(image.getInputStream(), destination);
        return "/uploads/" + fileName;
    }

}
