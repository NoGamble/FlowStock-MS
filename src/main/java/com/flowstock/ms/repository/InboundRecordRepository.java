package com.flowstock.ms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.flowstock.ms.entity.InboundRecord;

@Repository
public interface InboundRecordRepository extends JpaRepository<InboundRecord, Long> {

}