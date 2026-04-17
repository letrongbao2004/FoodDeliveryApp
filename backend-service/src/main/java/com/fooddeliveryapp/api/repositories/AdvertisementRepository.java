package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.Advertisement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    @Query("""
            SELECT a FROM Advertisement a
            JOIN FETCH a.menuItem mi
            JOIN FETCH mi.restaurant r
            WHERE a.isActive = true
              AND :now BETWEEN a.startDate AND a.endDate
            ORDER BY a.createdAt DESC
            """)
    List<Advertisement> findActiveAds(LocalDateTime now, Pageable pageable);

    @Query("""
            SELECT a FROM Advertisement a
            JOIN FETCH a.menuItem mi
            JOIN FETCH mi.restaurant r
            WHERE a.merchantId = :merchantId
            ORDER BY a.createdAt DESC
            """)
    List<Advertisement> findByMerchantIdWithMenuItem(Long merchantId);

    @Query("""
            SELECT a FROM Advertisement a
            JOIN FETCH a.menuItem mi
            JOIN FETCH mi.restaurant r
            WHERE a.id = :id
            """)
    Optional<Advertisement> findByIdWithMenuItem(Long id);
}
