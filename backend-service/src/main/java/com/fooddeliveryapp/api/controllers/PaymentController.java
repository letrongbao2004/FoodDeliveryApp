package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.models.PaymentOrder;
import com.fooddeliveryapp.api.services.MoMoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private MoMoService moMoService;

    /**
     * GET /api/payment/create?amount=50000
     * Returns payUrl as plain string
     */
    @GetMapping("/payment/create")
    public ResponseEntity<String> createPayment(@RequestParam Integer amount) {
        try {
            log.info("[PaymentController] Creating payment for amount={}", amount);
            String payUrl = moMoService.createPayment(amount);
            return ResponseEntity.ok(payUrl);
        } catch (Exception e) {
            log.error("[PaymentController] Error creating payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * POST /api/payment/notify
     * IPN callback from MoMo
     */
    @PostMapping("/payment/notify")
    public ResponseEntity<String> receiveMoMoNotify(@RequestBody Map<String, Object> body) {
        try {
            log.info("[PaymentController] IPN received: {}", body);

            String orderId    = String.valueOf(body.get("orderId"));
            String resultCode = String.valueOf(body.get("resultCode"));

            // Verify signature
            if (!moMoService.verifyIpnSignature(body)) {
                log.warn("[PaymentController] Invalid IPN signature for orderId={}", orderId);
                return ResponseEntity.status(401).body("Invalid signature");
            }

            // Update status
            String status = "0".equals(resultCode) ? "SUCCESS" : "FAILED";
            moMoService.updateOrderStatus(orderId, status);

            return ResponseEntity.ok("ok");

        } catch (Exception e) {
            log.error("[PaymentController] IPN error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/payment/return
     * Redirect URL after user completes payment on MoMo
     */
    @GetMapping("/payment/return")
    public ResponseEntity<String> handleMoMoReturn(@RequestParam Map<String, String> params) {
        String orderId    = params.get("orderId");
        String resultCode = params.get("resultCode");
        log.info("[PaymentController] Return: orderId={}, resultCode={}", orderId, resultCode);

        String message = "0".equals(resultCode) ? "Payment successful!" : "Payment failed or cancelled.";
        return ResponseEntity.ok(message + " Order: " + orderId);
    }

    /**
     * GET /api/order/{id}
     * Check payment status: PENDING | SUCCESS | FAILED
     */
    @GetMapping("/order/{id}")
    public ResponseEntity<String> getOrderStatus(@PathVariable String id) {
        Optional<PaymentOrder> order = moMoService.getOrder(id);
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get().getStatus());
        } else {
            return ResponseEntity.status(404).body("Order not found");
        }
    }
}
