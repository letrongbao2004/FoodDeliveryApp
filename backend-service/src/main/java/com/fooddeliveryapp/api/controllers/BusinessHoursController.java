package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.models.BusinessHours;
import com.fooddeliveryapp.api.repositories.BusinessHoursRepository;
import com.fooddeliveryapp.api.repositories.RestaurantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/hours")
public class BusinessHoursController {

    private final BusinessHoursRepository hoursRepo;
    private final RestaurantRepository restaurantRepo;

    public BusinessHoursController(BusinessHoursRepository hoursRepo,
                                   RestaurantRepository restaurantRepo) {
        this.hoursRepo = hoursRepo;
        this.restaurantRepo = restaurantRepo;
    }

    /** Lấy toàn bộ lịch mở cửa của một nhà hàng */
    @GetMapping
    public ResponseEntity<List<BusinessHours>> getHours(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(hoursRepo.findByRestaurantId(restaurantId));
    }

    /** Kiểm tra nhà hàng có đang mở cửa không */
    @GetMapping("/is-open")
    public ResponseEntity<Map<String, Object>> isOpen(@PathVariable Long restaurantId) {
        return restaurantRepo.findById(restaurantId).map(restaurant -> {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            DayOfWeek today = now.getDayOfWeek();
            LocalTime currentTime = now.toLocalTime();

            return hoursRepo.findByRestaurantIdAndDayOfWeek(restaurantId, today)
                    .map(hours -> {
                        boolean open = !hours.isClosed()
                                && !currentTime.isBefore(hours.getOpenTime())
                                && !currentTime.isAfter(hours.getCloseTime());
                        return ResponseEntity.ok(Map.<String, Object>of(
                                "isOpen", open,
                                "day", today.name(),
                                "openTime", hours.isClosed() ? "Closed" : hours.getOpenTime().toString(),
                                "closeTime", hours.isClosed() ? "Closed" : hours.getCloseTime().toString()
                        ));
                    })
                    .orElse(ResponseEntity.ok(Map.of(
                            "isOpen", false,
                            "day", today.name(),
                            "message", "No schedule set for today"
                    )));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo hoặc cập nhật lịch cho một ngày.
     * Body JSON:
     * {
     *   "dayOfWeek": "MONDAY",
     *   "openTime":  "08:00:00",
     *   "closeTime": "22:00:00",
     *   "closed": false
     * }
     */
    @PutMapping("/{day}")
    public ResponseEntity<?> upsertDay(@PathVariable Long restaurantId,
                                       @PathVariable String day,
                                       @RequestBody BusinessHours body) {
        return restaurantRepo.findById(restaurantId).map(restaurant -> {
            DayOfWeek dow = DayOfWeek.valueOf(day.toUpperCase());
            BusinessHours hours = hoursRepo
                    .findByRestaurantIdAndDayOfWeek(restaurantId, dow)
                    .orElse(new BusinessHours());
            hours.setRestaurant(restaurantRepo.getReferenceById(restaurantId));
            hours.setDayOfWeek(dow);
            hours.setOpenTime(body.getOpenTime());
            hours.setCloseTime(body.getCloseTime());
            hours.setClosed(body.isClosed());
            return ResponseEntity.ok(hoursRepo.save(hours));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Đánh dấu nghỉ một ngày cụ thể */
    @PutMapping("/{day}/close")
    public ResponseEntity<?> closeDay(@PathVariable Long restaurantId,
                                      @PathVariable String day) {
        return restaurantRepo.findById(restaurantId).map(restaurant -> {
            DayOfWeek dow = DayOfWeek.valueOf(day.toUpperCase());
            BusinessHours hours = hoursRepo
                    .findByRestaurantIdAndDayOfWeek(restaurantId, dow)
                    .orElse(new BusinessHours());
            hours.setRestaurant(restaurantRepo.getReferenceById(restaurantId));
            hours.setDayOfWeek(dow);
            hours.setClosed(true);
            return ResponseEntity.ok(hoursRepo.save(hours));
        }).orElse(ResponseEntity.notFound().build());
    }
}
