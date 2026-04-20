# Hướng Dẫn Sử Dụng Distributed Lock với Redisson

Tài liệu này hướng dẫn cách sử dụng `DistributedLockService` đã được tích hợp trong dự án FoodDeliveryApp Backend.

## 1. Yêu Cầu Hệ Thống

> [!IMPORTANT]
> Backend của bạn hiện đã tích hợp thư viện **Redisson**. Để chạy được server, **BẮT BUỘC** bạn phải cài đặt và chạy **Redis Server** ở dưới background (mặc định tại `localhost:6379`).

Nếu bạn chưa có Redis:
- **Windows**: Tải qua Redis github releases cho Windows, hoặc chạy thông qua Docker/WSL2.
- Lệnh Docker nhanh: `docker run --name redis-server -p 6379:6379 -d redis`

## 2. Distributed Lock là gì và Tại sao cần?

Trong hệ thống đặt đồ ăn, giả sử nhà hàng chỉ còn **1 phần Phở** cuối cùng. Nếu 2 người dùng cùng lúc bấm "Đặt hàng" trong cùng một phần nghìn giây, có thể xảy ra tình trạng cả 2 request đều đọc thấy số lượng còn 1, và đều cho qua. Kết quả: Kho bị âm, và nhà hàng phải từ chối 1 khách.

Để tránh điều này, ta dùng **Distributed Lock**. Nó đảm bảo ở một thời điểm, chỉ có **1 luồng (thread) hoặc 1 server** được phép xử lý đơn đặt hàng cho một món/user cụ thể.

## 3. Cách sử dụng `DistributedLockService`

Service `DistributedLockService` đã bọc sẵn các logic khóa an toàn, bạn chỉ cần gọi phương thức `executeWithLock`.

### Code Mẫu (Ví dụ đặt hàng)

```java
package com.fooddeliveryapp.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final DistributedLockService distributedLockService;
    private final RestaurantRepository restaurantRepository;

    public String placeOrder(Long restaurantId, Long userId) {
        
        // Tạo một khoá duy nhất theo restaurantId để không khoá toàn bộ hệ thống
        String lockKey = "restaurant:" + restaurantId;

        // Gọi hàm executeWithLock
        // - Thời gian chờ lấy khóa: 10 giây
        // - Thời gian khóa tự động nhả nếu xử lý quá lâu (Lease Time): 30 giây
        return distributedLockService.executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, () -> {
            
            // --- LOGIC XỬ LÝ ĐẶT HÀNG ---
            // Lúc này, chắc chắn 100% không có thread nào khác đang xử lý cho nhà hàng này
            
            Restaurant res = restaurantRepository.findById(restaurantId).orElseThrow();
            if (res.getAvailableItems() > 0) {
                res.setAvailableItems(res.getAvailableItems() - 1);
                restaurantRepository.save(res);
                return "Đặt hàng thành công!";
            } else {
                return "Hết món ăn!";
            }
            // --- KẾT THÚC LOGIC ---
            
        });
    }
}
```

## 4. Xử lý Lỗi

> [!WARNING]
> Nếu hệ thống đang quá tải, một request khác có thể không lấy được khoá trong thời gian quy định (vd: 10s). Lúc này hàm sẽ throw `RuntimeException("Resource is currently busy...")`.
> Bạn nên có `GlobalExceptionHandler` để hứng lỗi này và trả về HTTP Status 409 (Conflict) hoặc 429 (Too Many Requests) cho Client Android hiển thị popup: "Hệ thống đang xử lý, vui lòng thử lại".
