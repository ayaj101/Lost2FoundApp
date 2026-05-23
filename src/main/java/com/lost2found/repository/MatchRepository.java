package com.lost2found.repository;

import com.lost2found.model.FoundItem;
import com.lost2found.model.LostItem;
import com.lost2found.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // ✅ Fixed: use @Query instead of derived property to match your entity field names
    @Query("SELECT m FROM Match m WHERE m.foundItem.foundId = :foundId ORDER BY m.similarityScore DESC")
    List<Match> findByFoundItem_FoundIdOrderBySimilarityScoreDesc(@Param("foundId") Long foundId);

    @Query("SELECT m FROM Match m WHERE m.lostItem.lostId = :lostId ORDER BY m.similarityScore DESC")
    List<Match> findByLostItem_LostIdOrderBySimilarityScoreDesc(@Param("lostId") Long lostId);

    List<Match> findByLostItem(LostItem lostItem);
    List<Match> findByFoundItem(FoundItem foundItem);


    // ✅ Works fine
    boolean existsByLostItemAndFoundItem(LostItem lost, FoundItem found);
}
