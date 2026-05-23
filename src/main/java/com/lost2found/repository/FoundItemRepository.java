package com.lost2found.repository;

import com.lost2found.model.FoundItem;
import com.lost2found.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {

    // 1️⃣ Find by location (case-insensitive)
    List<FoundItem> findByLocationContainingIgnoreCaseAndStatus(String location, FoundItem.Status status);

    // 2️⃣ Find by status
    List<FoundItem> findByStatus(FoundItem.Status status);

    // 3️⃣ Search by location + date range
    @Query("SELECT f FROM FoundItem f " +
            "WHERE (:location IS NULL OR LOWER(f.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:fromDate IS NULL OR :toDate IS NULL OR f.eventDate BETWEEN :fromDate AND :toDate)")
    List<FoundItem> searchFoundItems(@Param("location") String location,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);

    // 4️⃣ Fetch items by user for dashboard/profile
    @Query("SELECT f FROM FoundItem f WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<FoundItem> findByUserId(@Param("userId") Long userId);

    // 5️⃣ Fetch all open items for AI matching
    @Query("SELECT f FROM FoundItem f WHERE f.status = 'OPEN'")
    List<FoundItem> findAllOpenFoundItems();

    @Query("SELECT f FROM FoundItem f WHERE f.user.id = :userId AND LOWER(f.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<FoundItem> findByUserIdAndLocationContainingIgnoreCase(@Param("userId") Long userId,
                                                                @Param("location") String location);
    List<FoundItem> findByUser(User user);

}
