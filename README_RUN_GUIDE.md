# 🚀 MoMo Payment Integration - Run Guide

## Architecture Overview

```
Android App  →  Spring Boot (8080)  →  MoMo Sandbox API
                      ↓
                  MySQL DB (momo_orders)
```

---

## 1. Prerequisites

| Tool         | Required Version |
|-------------|-----------------|
| JDK         | 17+             |
| Maven       | 3.8+            |
| MySQL       | 8.0+            |
| Android Studio | Latest       |
| Android Emulator | API 26+   |

---

## 2. Database Setup

Run in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS food_delivery;
USE food_delivery;

CREATE TABLE IF NOT EXISTS momo_orders (
    id         VARCHAR(50)  PRIMARY KEY,
    amount     INT          NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
```

> Hibernate `ddl-auto: update` will also auto-create this table on first run.

---

## 3. Backend Configuration

File: `backend-service/src/main/java/com/fooddeliveryapp/api/config/MoMoConfig.java`

```java
PARTNER_CODE  = "MOMOBKUN20180529"
ACCESS_KEY    = "klm05TvNBzhg7h7j"
SECRET_KEY    = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa"
CREATE_URL    = "https://test-payment.momo.vn/v2/gateway/api/create"
REDIRECT_URL  = "http://localhost:8080/api/payment/return"
NOTIFY_URL    = "http://localhost:8080/api/payment/notify"
```

> ⚠️ These are **public sandbox credentials** from MoMo docs. For production, replace with your merchant credentials.

---

## 4. Run Backend

```powershell
cd backend-service
./mvnw spring-boot:run
```

Verify: `http://localhost:8080/api/payment/create?amount=50000`  
Expected: A URL string starting with `https://test-payment.momo.vn/...`

---

## 5. Run Android App

1. Open project in **Android Studio**
2. Start an **Android Emulator** (API 26+)
3. Click **Run ▶**

> The emulator accesses backend via `http://10.0.2.2:8080` (maps to `localhost` on host machine)

---

## 6. Payment Flow

```
1. Add items to cart
2. Go to Cart → tap "Proceed to Checkout"
3. Select "MoMo" payment method
4. Tap "Place Order" → PaymentActivity opens
5. App calls: GET http://10.0.2.2:8080/api/payment/create?amount=XXXXX
6. Backend calls MoMo Sandbox → returns payUrl
7. QR code generated from payUrl
8. Scan QR with phone camera → opens MoMo test page in browser
   OR tap "Pay with MoMo" button → opens payUrl directly
```

---

## 7. API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/payment/create?amount=50000` | Create payment, returns `payUrl` string |
| POST | `/api/payment/notify` | IPN callback from MoMo |
| GET | `/api/payment/return` | Redirect after payment |
| GET | `/api/order/{id}` | Check order status: PENDING/SUCCESS/FAILED |

---

## 8. Signature Verification (rawHash format)

**Create Payment:**
```
accessKey=&amount=&extraData=&ipnUrl=&orderId=&orderInfo=&partnerCode=&redirectUrl=&requestId=&requestType=
```

**IPN Callback:**
```
accessKey=&amount=&extraData=&message=&orderId=&orderInfo=&orderType=&partnerCode=&payType=&requestId=&responseTime=&resultCode=&transId=
```

---

## 9. Debugging

Check backend logs for:
- `[MoMo] rawHash (create): ...` — verify field order
- `[MoMo] signature: ...` — verify HMAC output
- `[MoMo] resultCode=0` — success from MoMo

---

## 10. Testing MoMo Sandbox QR

- Scan QR with phone **camera app** (not MoMo in-app scanner)
- Opens `test-payment.momo.vn` in browser
- Use MoMo test credentials to simulate payment

> ⚠️ Sandbox QR only works on the MoMo test environment. Do not use in production.
