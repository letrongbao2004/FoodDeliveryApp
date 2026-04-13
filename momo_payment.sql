-- ============================================
-- MoMo Payment Orders Table
-- Food Delivery App - Sandbox Environment
-- ============================================

CREATE TABLE IF NOT EXISTS momo_orders (
    id          VARCHAR(50)     PRIMARY KEY,
    amount      INT             NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_momo_orders_status ON momo_orders(status);

-- Sample: view all orders
-- SELECT * FROM momo_orders ORDER BY created_at DESC;

-- Sample: check a specific order
-- SELECT * FROM momo_orders WHERE id = 'your-order-id';
