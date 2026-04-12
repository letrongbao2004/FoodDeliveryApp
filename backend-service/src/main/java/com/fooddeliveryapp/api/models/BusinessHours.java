package com.fooddeliveryapp.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "business_hours",
        uniqueConstraints = @UniqueConstraint(columnNames = {"restaurant_id", "day_of_week"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnoreProperties({"businessHours", "hibernateLazyInitializer", "handler"})
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek; // MONDAY, TUESDAY, ... SUNDAY

    @Column(name = "open_time")
    private LocalTime openTime;  // e.g. 08:00

    @Column(name = "close_time")
    private LocalTime closeTime; // e.g. 22:00

    /** true = nghỉ cả ngày hôm đó */
    private boolean closed;
}
