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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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

    // ------------------ LOST ITEM ------------------
    // ------------------ LOST ITEM ------------------
    @PostMapping("/report/lost")
    public ResponseEntity<?> reportLostItem(
            @RequestParam Long user_id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam(required = false) String event_date,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            User user = userRepo.findById(user_id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + user_id));

            if (!new File(uploadDir).exists()) new File(uploadDir).mkdirs();

            String filePath = null;
            if (image != null && !image.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                File dest = new File(uploadDir, fileName);
                image.transferTo(dest);
                filePath = "/uploads/" + fileName;
            }

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
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
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
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            User user = userRepo.findById(user_id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + user_id));

            if (!new File(uploadDir).exists()) new File(uploadDir).mkdirs();

            String filePath = null;
            if (image != null && !image.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                File dest = new File(uploadDir, fileName);
                image.transferTo(dest);
                filePath = "/uploads/" + fileName;
            }

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
        } catch (Exception e) {
            e.printStackTrace();
            String msg = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : ("Server error: " + e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Add this in ReportController.java
    @GetMapping("/user/matches")
    public ResponseEntity<?> getUserMatches(@RequestParam Long user_id) {
        try {
            User user = userRepo.findById(user_id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + user_id));

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

}
