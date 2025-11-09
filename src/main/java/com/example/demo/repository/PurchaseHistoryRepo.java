package com.example.demo.repository;

import com.example.demo.model.PurchaseHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseHistoryRepo extends JpaRepository<PurchaseHistoryModel, Long> {
    
    List<PurchaseHistoryModel> findByProductIdOrderByOperationDateDesc(Long productId);
}

