package com.lost2found.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lost_id", nullable = false)
    private LostItem lostItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "found_id", nullable = false)
    private FoundItem foundItem;

    @Column(name = "score", nullable = false)
    private double similarityScore;

    // ===================== NEW LOGIC FIELDS =====================

    @Column(name = "auto_verified", nullable = false)
    private boolean autoVerified = false;  // score ≥ 0.8

    @Column(name = "needs_user_verification", nullable = false)
    private boolean needsUserVerification = false;  // 0.6 ≤ score < 0.8

    @Column(name = "user_confirmed", nullable = false)
    private boolean userConfirmed = false; // user verifies match

    // ============================================================

    @Column(name = "matched_at", nullable = false, updatable = false)
    private LocalDateTime matchedAt = LocalDateTime.now();

    @Column(name = "system_note", columnDefinition = "TEXT")
    private String systemNote;

    private String status;

    public Match() {}

    public Match(LostItem lostItem, FoundItem foundItem, double similarityScore) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.similarityScore = similarityScore;
        this.matchedAt = LocalDateTime.now();
    }

    // ===================== Getters & Setters =====================

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public LostItem getLostItem() { return lostItem; }
    public void setLostItem(LostItem lostItem) { this.lostItem = lostItem; }

    public FoundItem getFoundItem() { return foundItem; }
    public void setFoundItem(FoundItem foundItem) { this.foundItem = foundItem; }

    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }

    public boolean isAutoVerified() { return autoVerified; }
    public void setAutoVerified(boolean autoVerified) { this.autoVerified = autoVerified; }

    public boolean isNeedsUserVerification() { return needsUserVerification; }
    public void setNeedsUserVerification(boolean needsUserVerification) { this.needsUserVerification = needsUserVerification; }

    public boolean isUserConfirmed() { return userConfirmed; }
    public void setUserConfirmed(boolean userConfirmed) { this.userConfirmed = userConfirmed; }

    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }

    public String getSystemNote() { return systemNote; }
    public void setSystemNote(String systemNote) { this.systemNote = systemNote; }
}
