package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {
}
