package com.lost2found.service;

import com.lost2found.model.Match;
import com.lost2found.model.FoundItem;
import com.lost2found.model.LostItem;
import com.lost2found.model.User;
import com.lost2found.repository.MatchRepository;
import com.lost2found.repository.FoundItemRepository;
import com.lost2found.repository.LostItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    // ⭐ NEW: Email Service added
    @Autowired
    private EmailService emailService;

    // ---------------- EXISTING METHODS ----------------

    public List<Match> getMatchesForFoundItem(Long foundId) {
        return matchRepository.findByFoundItem_FoundIdOrderBySimilarityScoreDesc(foundId);
    }

    public List<Match> getMatchesForLostItem(Long lostId) {
        return matchRepository.findByLostItem_LostIdOrderBySimilarityScoreDesc(lostId);
    }

    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }


    // -----------------------------------------------------
    //                   AUTO MATCHING LOGIC
    // -----------------------------------------------------

    @Transactional
    public void processNewFoundItem(Long foundId) {
        Optional<FoundItem> optionalFound = foundItemRepository.findById(foundId);
        if (optionalFound.isEmpty()) return;

        FoundItem found = optionalFound.get();

        List<LostItem> allLost = lostItemRepository.findByStatus(LostItem.Status.OPEN);
        List<LostItem> candidates = new ArrayList<>();

        for (LostItem lost : allLost) {
            if (lost.getLocation() != null && found.getLocation() != null &&
                    (lost.getLocation().toLowerCase().contains(found.getLocation().toLowerCase()) ||
                            found.getLocation().toLowerCase().contains(lost.getLocation().toLowerCase()))) {
                candidates.add(lost);
            }
        }

        System.out.println("✓ Found " + candidates.size() + " matching candidates for FoundItem ID: " + foundId);

        List<Match> matchesToSave = new ArrayList<>();

        for (LostItem lost : candidates) {

            if (matchRepository.existsByLostItemAndFoundItem(lost, found)) continue;

            double score = computeSimilarity(lost, found);

            if (score < 0.6) continue;

            Match match = new Match(lost, found, score);

            // ----------------------------------------------------
            // AUTO VERIFIED
            // ----------------------------------------------------
            if (score >= 0.8) {
                match.setAutoVerified(true);
                match.setNeedsUserVerification(false);
                match.setUserConfirmed(false);
                match.setSystemNote("Auto-verified match (score ≥ 0.8)");

                lost.setStatus(LostItem.Status.MATCHED);
                found.setStatus(FoundItem.Status.MATCHED);

                lostItemRepository.save(lost);
                foundItemRepository.save(found);

                // ⭐ Existing console notify
                notifyUser(lost.getUser(),
                        "🎯 Auto-verified match found for your lost item: " + lost.getTitle());
                notifyUser(found.getUser(),
                        "🎯 Your found item matched automatically with: " + lost.getTitle());

                // ⭐ NEW — Email Notification
                emailService.sendMatchNotification(
                        lost.getUser().getEmail(),
                        lost.getTitle(),
                        found.getTitle(),
                        "Auto Verified Match"
                );

                emailService.sendMatchNotification(
                        found.getUser().getEmail(),
                        lost.getTitle(),
                        found.getTitle(),
                        "Auto Verified Match"
                );
            }

            // ----------------------------------------------------
            // USER VERIFICATION NEEDED
            // ----------------------------------------------------
            else {
                match.setAutoVerified(false);
                match.setNeedsUserVerification(true);
                match.setUserConfirmed(false);
                match.setSystemNote("User confirmation required (0.6 ≤ score < 0.8)");

                notifyUser(lost.getUser(),
                        "⚠️ Possible match found for your lost item: " + lost.getTitle());
                notifyUser(found.getUser(),
                        "⚠️ Your found item might match: " + lost.getTitle());

                // ⭐ NEW — Email Notification (Manual Verification)
                emailService.sendMatchNotification(
                        lost.getUser().getEmail(),
                        lost.getTitle(),
                        found.getTitle(),
                        "Manual Verification Needed"
                );

                emailService.sendMatchNotification(
                        found.getUser().getEmail(),
                        lost.getTitle(),
                        found.getTitle(),
                        "Manual Verification Needed"
                );
            }

            matchesToSave.add(match);
        }

        if (!matchesToSave.isEmpty()) {
            matchRepository.saveAll(matchesToSave);
            System.out.println("💾 Saved " + matchesToSave.size() + " match(es).");
        }
    }

    // -----------------------------------------------------
    //   SAME CHANGES APPLIED IN processNewLostItem()
    // -----------------------------------------------------

    @Transactional
    public void processNewLostItem(Long lostId) {

        Optional<LostItem> optionalLost = lostItemRepository.findById(lostId);
        if (optionalLost.isEmpty()) return;

        LostItem lost = optionalLost.get();

        List<FoundItem> allFound = foundItemRepository.findByStatus(FoundItem.Status.OPEN);
        List<FoundItem> candidates = new ArrayList<>();

        for (FoundItem found : allFound) {

            if (lost.getLocation() != null && found.getLocation() != null &&
                    (lost.getLocation().toLowerCase().contains(found.getLocation().toLowerCase()) ||
                            found.getLocation().toLowerCase().contains(lost.getLocation().toLowerCase()))) {
                candidates.add(found);
            }
        }

        List<Match> matchesToSave = new ArrayList<>();

        for (FoundItem found : candidates) {

            if (matchRepository.existsByLostItemAndFoundItem(lost, found)) continue;

            double score = computeSimilarity(lost, found);
            if (score < 0.6) continue;

            Match match = new Match(lost, found, score);

            if (score >= 0.8) {

                match.setAutoVerified(true);
                match.setNeedsUserVerification(false);
                match.setUserConfirmed(false);
                match.setSystemNote("Auto-verified match (score ≥ 0.8)");

                lost.setStatus(LostItem.Status.MATCHED);
                found.setStatus(FoundItem.Status.MATCHED);

                lostItemRepository.save(lost);
                foundItemRepository.save(found);

                notifyUser(lost.getUser(), "🎯 Auto-verified match found for your lost item.");
                notifyUser(found.getUser(), "🎯 Your found item automatically matched with a lost report.");

                // ⭐ Email Notifications
                emailService.sendMatchNotification(
                        lost.getUser().getEmail(),
                        lost.getTitle(),
                        found.getTitle(),
                        "Auto Verified Match"
                );

                emailService.sendMatchNotification(
                        found.getUser().getEmail(),
                        lost.getTitle(),
                        found.getTitle(),
                        "Auto Verified Match"
                );

            } else {
                match.setAutoVerified(false);
                match.setNeedsUserVerification(true);
                match.setUserConfirmed(false);
                match.setSystemNote("User confirmation required (0.6 ≤ score < 0.8)");

                notifyUser(lost.getUser(), "⚠️ Possible match found for your lost item. Please review.");
                notifyUser(found.getUser(), "⚠️ Your found item might match someone's lost item. Please confirm.");

                // ⭐ Email Notifications
                emailService.sendMatchNotification(
                        lost.getUser().getEmail(),
                        lost.getTitle(),
                        found.getTitle(),
                        "Manual Verification Needed"
                );

                emailService.sendMatchNotification(
                        found.getUser().getEmail(),
                        lost.getTitle(),
                        found.getTitle(),
                        "Manual Verification Needed"
                );
            }

            matchesToSave.add(match);
        }

        if (!matchesToSave.isEmpty()) {
            matchRepository.saveAll(matchesToSave);
        }
    }


    // ---------------- UTILITY METHODS ----------------

    private double computeSimilarity(LostItem lost, FoundItem found) {
        double score = 0.0;

        if (lost.getTitle() != null && found.getTitle() != null) {
            if (lost.getTitle().equalsIgnoreCase(found.getTitle())) score += 0.6;
            else if (lost.getTitle().toLowerCase().contains(found.getTitle().toLowerCase()) ||
                    found.getTitle().toLowerCase().contains(lost.getTitle().toLowerCase())) score += 0.4;
        }

        if (lost.getDescription() != null && found.getDescription() != null &&
                lost.getDescription().toLowerCase().contains(found.getDescription().toLowerCase())) {
            score += 0.3;
        }

        if (lost.getLocation() != null && found.getLocation() != null &&
                (lost.getLocation().equalsIgnoreCase(found.getLocation()) ||
                        lost.getLocation().toLowerCase().contains(found.getLocation().toLowerCase()) ||
                        found.getLocation().toLowerCase().contains(lost.getLocation().toLowerCase()))) {
            score += 0.3;
        }

        return Math.min(score, 1.0);
    }

    private void notifyUser(User user, String message) {
        System.out.println("🔔 Notify " + user.getUsername() + ": " + message);
    }

    public List<Match> getMatchesForUser(User user) {
        List<Match> userMatches = new ArrayList<>();

        List<LostItem> lostItems = lostItemRepository.findByUser(user);
        for (LostItem lost : lostItems) {
            userMatches.addAll(matchRepository.findByLostItem(lost));
        }

        List<FoundItem> foundItems = foundItemRepository.findByUser(user);
        for (FoundItem found : foundItems) {
            userMatches.addAll(matchRepository.findByFoundItem(found));
        }

        return userMatches;
    }

    public Optional<Match> getMatchById(Long id) {
        return matchRepository.findById(id);
    }
}
