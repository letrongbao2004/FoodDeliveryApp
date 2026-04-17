package com.fooddeliveryapp.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /** Merchant owner user id */
    @NotNull
    // Keep DB column nullable to avoid breaking existing rows during ddl-auto:update.
    // Validation still requires ownerId for create/update via @Valid.
    @Column
    private Long ownerId;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String description;
    private String imageUrl;
    private String imagePublicId;
    private String address;
    private String phone;
    private String category;
    private double rating;
    private int deliveryTime;
    private double deliveryFee;

    @Version
    private Long version;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("restaurant")
    private List<BusinessHours> businessHours;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImagePublicId() { return imagePublicId; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public List<BusinessHours> getBusinessHours() { return businessHours; }
    public void setBusinessHours(List<BusinessHours> businessHours) { this.businessHours = businessHours; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    public int getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(int deliveryTime) { this.deliveryTime = deliveryTime; }

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
