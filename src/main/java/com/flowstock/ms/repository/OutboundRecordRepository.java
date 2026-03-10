package com.flowstock.ms.repository;

import com.flowstock.ms.entity.OutboundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboundRecordRepository extends JpaRepository<OutboundRecord, Long> {
}