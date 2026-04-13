package com.fooddeliveryapp.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoMoRequest {
    private String partnerCode;
    private String requestId;
    private Long amount;
    private String orderId;
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String requestType;
    private String extraData;
    private String signature;
    private String lang;
}
