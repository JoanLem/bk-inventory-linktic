package com.example.demo.repository;

import com.example.demo.model.InventoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepo extends JpaRepository<InventoryModel, Long> {
    
    Optional<InventoryModel> findByProductId(Long productId);
}

