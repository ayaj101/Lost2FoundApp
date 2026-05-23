package com.lost2found.controller;

import com.lost2found.model.FoundItem;
import com.lost2found.model.LostItem;
import com.lost2found.model.Match;
import com.lost2found.model.User;
import com.lost2found.repository.FoundItemRepository;
import com.lost2found.repository.LostItemRepository;
import com.lost2found.repository.MatchRepository;
import com.lost2found.repository.UserRepository;
import com.lost2found.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LostItemRepository lostItemRepo;

    @Autowired
    private FoundItemRepository foundItemRepo;

    @Autowired
    private MatchRepository matchRepository;

    // Show matches for a specific FoundItem
    @GetMapping("/found/{id}")
    public String getMatchesForFound(@PathVariable("id") Long foundId, Model model) {
        List<Match> matches = matchService.getMatchesForFoundItem(foundId);
        model.addAttribute("matches", matches);
        return "matches";
    }

    // Show matches for a specific LostItem
    @GetMapping("/lost/{id}")
    public String getMatchesForLost(@PathVariable("id") Long lostId, Model model) {
        List<Match> matches = matchService.getMatchesForLostItem(lostId);
        model.addAttribute("matches", matches);
        return "matches";
    }

    // View all matches
    @GetMapping
    public String viewAllMatches(Model model) {
        List<Match> matches = matchService.getAllMatches();
        model.addAttribute("matches", matches);
        return "matches";
    }

    // Save a match (used for manual creation)
    @PostMapping("/save")
    public String saveMatch(@ModelAttribute Match match) {
        // Ensure LostItem and FoundItem exist
        Optional<LostItem> lostOpt = lostItemRepo.findById(match.getLostItem().getLostId());
        Optional<FoundItem> foundOpt = foundItemRepo.findById(match.getFoundItem().getFoundId());

        if (lostOpt.isEmpty() || foundOpt.isEmpty()) {
            return "redirect:/matches?error=invalidItems";
        }

        match.setLostItem(lostOpt.get());
        match.setFoundItem(foundOpt.get());
        matchService.saveMatch(match);

        return "redirect:/matches/found/" + match.getFoundItem().getFoundId();
    }

    // Fetch user info for modal popup
    @GetMapping("/user/info/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        return ResponseEntity.ok(data);
    }

    @PostMapping("/confirm/{id}")
    @ResponseBody
    public ResponseEntity<String> confirmMatch(@PathVariable Long id) {

        Optional<Match> matchOpt = matchService.getMatchById(id);

        if (matchOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Match not found.");
        }

        Match match = matchOpt.get();

        // If already verified
        if (!match.isNeedsUserVerification()) {
            return ResponseEntity.ok("This match is already verified.");
        }

        // Update match details
        match.setUserConfirmed(true);
        match.setNeedsUserVerification(false);
        match.setAutoVerified(false);  // Since user manually confirmed
        match.setSystemNote("Match manually confirmed by user.");

        // Update LostItem status
        LostItem lost = match.getLostItem();
        if (lost.getStatus() == LostItem.Status.OPEN) {
            lost.setStatus(LostItem.Status.MATCHED);
            lostItemRepo.save(lost);
        }

        // Update FoundItem status
        FoundItem found = match.getFoundItem();
        if (found.getStatus() == FoundItem.Status.OPEN) {
            found.setStatus(FoundItem.Status.MATCHED);
            foundItemRepo.save(found);
        }

        // Save match
        matchService.saveMatch(match);

        return ResponseEntity.ok("✅ Match manually verified successfully!");
    }


}
