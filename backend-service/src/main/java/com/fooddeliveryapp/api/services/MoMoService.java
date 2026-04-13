package com.fooddeliveryapp.api.services;

import com.fooddeliveryapp.api.config.MoMoConfig;
import com.fooddeliveryapp.api.dto.MoMoRequest;
import com.fooddeliveryapp.api.dto.MoMoResponse;
import com.fooddeliveryapp.api.models.PaymentOrder;
import com.fooddeliveryapp.api.repositories.PaymentOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MoMoService {

    private static final Logger log = LoggerFactory.getLogger(MoMoService.class);

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Create MoMo payment — returns payUrl string only.
     */
    public String createPayment(Integer amount) throws Exception {
        String orderId   = UUID.randomUUID().toString();
        String requestId = orderId; // requestId == orderId per requirement
        String orderInfo = "Payment for order " + orderId;
        String extraData = "";
        String requestType = "captureWallet";

        // ===== rawHash EXACT order (per MoMo v2 spec) =====
        String rawHash =
                "accessKey="   + MoMoConfig.ACCESS_KEY  +
                "&amount="     + amount                 +
                "&extraData="  + extraData              +
                "&ipnUrl="     + MoMoConfig.NOTIFY_URL  +
                "&orderId="    + orderId                +
                "&orderInfo="  + orderInfo              +
                "&partnerCode="+ MoMoConfig.PARTNER_CODE+
                "&redirectUrl="+ MoMoConfig.REDIRECT_URL+
                "&requestId="  + requestId              +
                "&requestType="+ requestType;

        log.info("[MoMo] rawHash (create): {}", rawHash);

        String signature = hmacSha256(rawHash, MoMoConfig.SECRET_KEY);
        log.info("[MoMo] signature: {}", signature);

        MoMoRequest request = MoMoRequest.builder()
                .partnerCode(MoMoConfig.PARTNER_CODE)
                .requestId(requestId)
                .amount(Long.valueOf(amount))
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(MoMoConfig.REDIRECT_URL)
                .ipnUrl(MoMoConfig.NOTIFY_URL)
                .requestType(requestType)
                .extraData(extraData)
                .signature(signature)
                .lang("vi")
                .build();

        // Save PENDING order to DB
        PaymentOrder paymentOrder = new PaymentOrder(orderId, amount, "PENDING", new Date());
        paymentOrderRepository.save(paymentOrder);
        log.info("[MoMo] Order saved with status PENDING, orderId={}", orderId);

        // Call MoMo Sandbox API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MoMoRequest> entity = new HttpEntity<>(request, headers);

        try {
            MoMoResponse response = restTemplate.postForObject(MoMoConfig.CREATE_URL, entity, MoMoResponse.class);
            if (response == null) throw new RuntimeException("Empty response from MoMo");

            log.info("[MoMo] resultCode={}, message={}, payUrl={}", response.getResultCode(), response.getMessage(), response.getPayUrl());

            if (response.getResultCode() != 0) {
                throw new RuntimeException("MoMo error " + response.getResultCode() + ": " + response.getMessage());
            }

            return response.getPayUrl();

        } catch (HttpClientErrorException e) {
            log.error("[MoMo] HTTP {} from MoMo: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("MoMo returned HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        }
    }

    /**
     * Verify IPN signature from MoMo callback.
     */
    public boolean verifyIpnSignature(Map<String, Object> params) throws Exception {
        String accessKey   = MoMoConfig.ACCESS_KEY;
        String amount      = String.valueOf(params.get("amount"));
        String extraData   = String.valueOf(params.getOrDefault("extraData", ""));
        String message     = String.valueOf(params.get("message"));
        String orderId     = String.valueOf(params.get("orderId"));
        String orderInfo   = String.valueOf(params.get("orderInfo"));
        String orderType   = String.valueOf(params.get("orderType"));
        String partnerCode = MoMoConfig.PARTNER_CODE;
        String payType     = String.valueOf(params.get("payType"));
        String requestId   = String.valueOf(params.get("requestId"));
        String responseTime= String.valueOf(params.get("responseTime"));
        String resultCode  = String.valueOf(params.get("resultCode"));
        String transId     = String.valueOf(params.get("transId"));
        String signature   = String.valueOf(params.get("signature"));

        // ===== rawHash EXACT alphabetical order (IPN) =====
        String rawHash =
                "accessKey="   + accessKey   +
                "&amount="     + amount      +
                "&extraData="  + extraData   +
                "&message="    + message     +
                "&orderId="    + orderId     +
                "&orderInfo="  + orderInfo   +
                "&orderType="  + orderType   +
                "&partnerCode="+ partnerCode +
                "&payType="    + payType     +
                "&requestId="  + requestId   +
                "&responseTime="+ responseTime +
                "&resultCode=" + resultCode  +
                "&transId="    + transId;

        log.info("[MoMo] rawHash (IPN): {}", rawHash);

        String computed = hmacSha256(rawHash, MoMoConfig.SECRET_KEY);
        log.info("[MoMo] computed={}, received={}", computed, signature);

        return computed.equalsIgnoreCase(signature);
    }

    /**
     * Update order status in DB.
     */
    public void updateOrderStatus(String orderId, String status) {
        paymentOrderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            paymentOrderRepository.save(order);
            log.info("[MoMo] Order {} updated to {}", orderId, status);
        });
    }

    /**
     * Get order status from DB.
     */
    public Optional<PaymentOrder> getOrder(String orderId) {
        return paymentOrderRepository.findById(orderId);
    }

    // ===== HMAC SHA256 =====
    private String hmacSha256(String data, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return toHexString(rawHmac);
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) formatter.format("%02x", b);
        return sb.toString();
    }
}
