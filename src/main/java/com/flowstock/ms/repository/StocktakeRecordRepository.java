package com.flowstock.ms.repository;

import com.flowstock.ms.entity.StocktakeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StocktakeRecordRepository extends JpaRepository<StocktakeRecord, Long> {
}