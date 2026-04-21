package com.example.demo.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Partner findByPartnerID(Long partnerID);
    List<Partner> findAllByPartnerName(String partnerName);
}