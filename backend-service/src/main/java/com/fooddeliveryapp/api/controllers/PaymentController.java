package com.fooddeliveryapp.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @GetMapping("/create")
    public ResponseEntity<String> createPayment(@RequestParam int amount) {
        // Trả về một link Mock thanh toán MoMo Sandbox
        // Android App sẽ lấy URL này và gen ra mã QR CODE + Nút Bấm
        String payUrl = "https://test-payment.momo.vn/v2/gateway/pay?amount=" + amount + "&orderInfo=Test";
        return ResponseEntity.ok(payUrl);
    }
}
