package com.fooddeliveryapp.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "restaurants")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String imageUrl;
    private double rating;
    private int deliveryTime;
    private double deliveryFee;

    @Version
    private Long version;

    /** Lịch mở cửa theo từng ngày trong tuần */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("restaurant")
    private List<BusinessHours> businessHours;

    /**
     * Tính toán trạng thái mở cửa dựa theo múi giờ Việt Nam.
     * Trả về true nếu nhà hàng đang mở cửa vào thời điểm gọi API.
     */
    @Transient
    @JsonProperty("isOpen")
    public boolean isOpen() {
        if (businessHours == null || businessHours.isEmpty()) return false;

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DayOfWeek today = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        return businessHours.stream()
                .filter(h -> h.getDayOfWeek() == today && !h.isClosed())
                .anyMatch(h -> !currentTime.isBefore(h.getOpenTime())
                            && !currentTime.isAfter(h.getCloseTime()));
    }
}
