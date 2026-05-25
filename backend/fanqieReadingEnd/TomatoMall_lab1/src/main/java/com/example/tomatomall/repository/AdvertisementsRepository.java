package com.example.tomatomall.repository;

import com.example.tomatomall.po.Advertisements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdvertisementsRepository extends JpaRepository<Advertisements, Integer> {
    @Query("SELECT a FROM Advertisements a WHERE a.id = :id")
    Optional<Advertisements> findById(@Param("id") String id);
}
