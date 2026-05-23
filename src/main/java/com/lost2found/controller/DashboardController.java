package com.lost2found.controller;

import com.lost2found.model.LostItem;
import com.lost2found.model.FoundItem;
import com.lost2found.model.User;
import com.lost2found.repository.FoundItemRepository;
import com.lost2found.repository.LostItemRepository;
import com.lost2found.repository.UserRepository;
import com.lost2found.service.DashboardService;
import com.lost2found.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private LostItemRepository lostItemRepo;

    @Autowired
    private FoundItemRepository foundItemRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MatchService matchService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {
        try {
            String username = principal.getName();
            if (principal == null) {
                throw new RuntimeException("User not logged in.");
            }

            System.out.println("Principal = " + principal);
            System.out.println("Logged in username: " + username);

            Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(username);
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("User not found: " + username);
            }
            User user = optionalUser.get();
            Long userId = user.getId();

            long totalLost = lostItemRepo.count();
            long totalFound = foundItemRepo.count();

            long totalMatched = matchService.getAllMatches().size(); // ✅ Count matches, not individual items

            long totalOpen = lostItemRepo.findByStatus(LostItem.Status.OPEN).size() +
                    foundItemRepo.findByStatus(FoundItem.Status.OPEN).size();


            model.addAttribute("totalLost", totalLost);
            model.addAttribute("totalFound", totalFound);
            model.addAttribute("totalOpen", totalOpen);
            model.addAttribute("totalMatched", totalMatched);

            // fetch items only for the logged-in user
            model.addAttribute("lostItems", dashboardService.getAllLostItemsByUser(userId));
            model.addAttribute("foundItems", dashboardService.getAllFoundItemsByUser(userId));

            return "dashboard";
        } catch (Exception e) {
            e.printStackTrace(); // check console for exact exception
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
