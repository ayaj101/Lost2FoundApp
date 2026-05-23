package com.lost2found.model;

import com.lost2found.config.JsonConverter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "lost_items")
public class LostItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lost_id")
    private Long lostId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 120)
    private String location;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Convert(converter = JsonConverter.class)
    @Column(name = "description_emb", columnDefinition = "TEXT")
    private Map<String, Object> descriptionEmb;

    @Convert(converter = JsonConverter.class)
    @Column(name = "auto_desc_emb", columnDefinition = "TEXT")
    private Map<String, Object> autoDescEmb;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "lostItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Match> matches = new ArrayList<>();

    public enum Status { OPEN, MATCHED, CLOSED }

    public LostItem() {}

    // ===== Getters & Setters =====
    public Long getLostId() { return lostId; }
    public void setLostId(Long lostId) { this.lostId = lostId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Map<String, Object> getDescriptionEmb() { return descriptionEmb; }
    public void setDescriptionEmb(Map<String, Object> descriptionEmb) { this.descriptionEmb = descriptionEmb; }

    public Map<String, Object> getAutoDescEmb() { return autoDescEmb; }
    public void setAutoDescEmb(Map<String, Object> autoDescEmb) { this.autoDescEmb = autoDescEmb; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }

    // ===== Helper Methods =====
    public void addMatch(Match match) {
        matches.add(match);
        match.setLostItem(this);
    }

    public void removeMatch(Match match) {
        matches.remove(match);
        match.setLostItem(null);
    }
}
