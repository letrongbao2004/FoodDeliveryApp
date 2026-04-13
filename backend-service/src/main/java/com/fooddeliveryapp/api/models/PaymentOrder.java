package com.fooddeliveryapp.api.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "momo_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrder {
    @Id
    private String id; // orderId (UUID)
    
    private Integer amount;
    private String status; // PENDING, SUCCESS, FAILED
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
