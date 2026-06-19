package com.example.fintech_demo.repository;

import com.example.fintech_demo.model.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisbursementRepository extends JpaRepository<Disbursement, String> {}
