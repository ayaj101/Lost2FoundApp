package com.lost2found.service;

import com.lost2found.model.LostItem;
import com.lost2found.model.FoundItem;
import com.lost2found.repository.LostItemRepository;
import com.lost2found.repository.FoundItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    public List<LostItem> getAllLostItemsByUser(Long userId) {
        List<LostItem> list = lostItemRepository.findByUserId(userId);
        return list != null ? list : Collections.emptyList();
    }


    public List<FoundItem> getAllFoundItemsByUser(Long userId) {
        List<FoundItem> list = foundItemRepository.findByUserId(userId);
        return list != null ? list : Collections.emptyList();
    }

}