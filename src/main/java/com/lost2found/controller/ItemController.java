package com.lost2found.controller;

import com.lost2found.model.FoundItem;
import com.lost2found.model.LostItem;
import com.lost2found.model.User;
import com.lost2found.repository.FoundItemRepository;
import com.lost2found.repository.LostItemRepository;
import com.lost2found.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private LostItemRepository lostItemRepo;

    @Autowired
    private FoundItemRepository foundItemRepo;

    @Autowired
    private UserRepository userRepository;

    // ✅ Updated Search Method: Fetch only logged-in user's items
    @GetMapping("/searchItems")
    public ResponseEntity<?> searchItems(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false, defaultValue = "") String location,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("User not logged in");
        }

        // 🔹 Fetch user from DB
        String username = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        Long userId = optionalUser.get().getId();

        List<Map<String, Object>> results = new ArrayList<>();

        // 🔹 LOST ITEMS (for logged-in user)
        if (status.equalsIgnoreCase("lost") || status.equalsIgnoreCase("all")) {
            List<LostItem> lostItems;
            if (location.isEmpty()) {
                lostItems = lostItemRepo.findByUserId(userId);
            } else {
                lostItems = lostItemRepo.findByUserIdAndLocationContainingIgnoreCase(userId, location);
            }

            lostItems.forEach(item -> results.add(Map.of(
                    "id", item.getLostId(),
                    "title", item.getTitle(),
                    "description", item.getDescription(),
                    "location", item.getLocation(),
                    "eventDate", item.getEventDate(),
                    "imagePath", item.getImagePath(),
                    "status", item.getStatus(),
                    "type", "lost"
            )));
        }

        // 🔹 FOUND ITEMS (for logged-in user)
        if (status.equalsIgnoreCase("found") || status.equalsIgnoreCase("all")) {
            List<FoundItem> foundItems;
            if (location.isEmpty()) {
                foundItems = foundItemRepo.findByUserId(userId);
            } else {
                foundItems = foundItemRepo.findByUserIdAndLocationContainingIgnoreCase(userId, location);
            }

            foundItems.forEach(item -> results.add(Map.of(
                    "id", item.getFoundId(),
                    "title", item.getTitle(),
                    "description", item.getDescription(),
                    "location", item.getLocation(),
                    "eventDate", item.getEventDate(),
                    "imagePath", item.getImagePath(),
                    "status", item.getStatus(),
                    "type", "found"
            )));
        }

        return ResponseEntity.ok(results);
    }

    // --- Delete items safely (no changes here) ---
    @DeleteMapping("/delete/{type}/{id}")
    public String deleteItem(@PathVariable String type, @PathVariable Long id) {
        if (type.equalsIgnoreCase("lost")) {
            return lostItemRepo.findById(id)
                    .map(item -> {
                        lostItemRepo.delete(item);
                        return "Lost item deleted successfully";
                    })
                    .orElse("Lost item not found");
        } else if (type.equalsIgnoreCase("found")) {
            return foundItemRepo.findById(id)
                    .map(item -> {
                        foundItemRepo.delete(item);
                        return "Found item deleted successfully";
                    })
                    .orElse("Found item not found");
        } else {
            return "Invalid type";
        }
    }
}
