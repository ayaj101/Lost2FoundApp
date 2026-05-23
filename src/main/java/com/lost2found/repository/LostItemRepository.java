package com.lost2found.repository;

import com.lost2found.model.LostItem;
import com.lost2found.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    // 1️⃣ Find by location (case-insensitive)
    List<LostItem> findByLocationContainingIgnoreCaseAndStatus(String location, LostItem.Status status);

    // 2️⃣ Find by status
    List<LostItem> findByStatus(LostItem.Status status);

    // 3️⃣ Candidate selection for AI matching
    @Query("SELECT l FROM LostItem l " +
            "WHERE l.status = 'OPEN' " +
            "AND (LOWER(l.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (l.eventDate IS NULL OR l.eventDate BETWEEN :fromDate AND :toDate)")
    List<LostItem> findCandidates(@Param("location") String location,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate);

    // 4️⃣ Fetch items by user for dashboard/profile (optional)
    @Query("SELECT l FROM LostItem l WHERE l.user.id = :userId ORDER BY l.createdAt DESC")
    List<LostItem> findByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM LostItem l WHERE l.user.id = :userId AND LOWER(l.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<LostItem> findByUserIdAndLocationContainingIgnoreCase(@Param("userId") Long userId,
                                                               @Param("location") String location);
    List<LostItem> findByUser(User user);

}
