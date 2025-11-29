-- V3: Create subscription and payment tables

-- Create subscription_plans table
CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL CHECK (billing_cycle IN ('MONTHLY', 'YEARLY')),
    max_projects INTEGER NOT NULL,
    max_tasks_per_project INTEGER NOT NULL,
    max_team_members INTEGER NOT NULL,
    file_attachments BOOLEAN NOT NULL DEFAULT FALSE,
    advanced_reporting BOOLEAN NOT NULL DEFAULT FALSE,
    priority_support BOOLEAN NOT NULL DEFAULT FALSE,
    api_access BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create subscriptions table
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES subscription_plans(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('TRIAL', 'ACTIVE', 'CANCELLED', 'EXPIRED', 'SUSPENDED')),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    razorpay_subscription_id VARCHAR(100),
    razorpay_customer_id VARCHAR(100),
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    razorpay_payment_id VARCHAR(100),
    razorpay_order_id VARCHAR(100),
    razorpay_signature VARCHAR(500),
    payment_method VARCHAR(20),
    failure_reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP
);

-- Create indexes for subscriptions
CREATE INDEX idx_subscription_user ON subscriptions(user_id);
CREATE INDEX idx_subscription_status ON subscriptions(status);
CREATE INDEX idx_subscription_end_date ON subscriptions(end_date);

-- Create indexes for payments
CREATE INDEX idx_payment_user ON payments(user_id);
CREATE INDEX idx_payment_subscription ON payments(subscription_id);
CREATE INDEX idx_payment_status ON payments(status);

-- Insert default subscription plans
INSERT INTO subscription_plans (name, description, price, billing_cycle, max_projects, max_tasks_per_project, max_team_members, file_attachments, advanced_reporting, priority_support, api_access, active)
VALUES
    ('Free', 'Perfect for individuals getting started', 0.00, 'MONTHLY', 3, 20, 1, false, false, false, false, true),
    ('Basic', 'For small teams and freelancers', 499.00, 'MONTHLY', 10, 100, 5, true, false, false, false, true),
    ('Premium', 'For growing teams with advanced needs', 999.00, 'MONTHLY', 50, 500, 20, true, true, true, false, true),
    ('Enterprise', 'For large organizations', 2499.00, 'MONTHLY', -1, -1, -1, true, true, true, true, true),
    ('Basic Annual', 'For small teams and freelancers - Annual billing', 4990.00, 'YEARLY', 10, 100, 5, true, false, false, false, true),
    ('Premium Annual', 'For growing teams - Annual billing (2 months free)', 9990.00, 'YEARLY', 50, 500, 20, true, true, true, false, true);
