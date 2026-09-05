-- ==========================================================
-- SUPER FRIST VPN - PRODUCTION DATABASE SCHEMA (PostgreSQL)
-- ==========================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Admin Users
CREATE TABLE IF NOT EXISTS admin_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'SUPER_ADMIN',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP WITH TIME ZONE
);

-- 2. Activation Codes
CREATE TABLE IF NOT EXISTS activation_codes (
    code VARCHAR(64) PRIMARY KEY,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'USED', 'EXPIRED', 'DISABLED', 'REVOKED'
    validity_days INT NOT NULL DEFAULT 30,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    activation_date TIMESTAMP WITH TIME ZONE,
    expiry_date TIMESTAMP WITH TIME ZONE,
    device_id VARCHAR(128),
    user_id VARCHAR(64),
    last_connection TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    batch_id VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_activation_codes_status ON activation_codes(status);
CREATE INDEX IF NOT EXISTS idx_activation_codes_device ON activation_codes(device_id);
CREATE INDEX IF NOT EXISTS idx_activation_codes_expiry ON activation_codes(expiry_date);

-- 3. VPN Servers (WireGuard / OpenVPN nodes)
CREATE TABLE IF NOT EXISTS vpn_servers (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    country VARCHAR(64) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    flag_emoji VARCHAR(16) NOT NULL,
    city VARCHAR(64) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL DEFAULT 51820,
    protocol VARCHAR(32) NOT NULL DEFAULT 'WIREGUARD',
    public_key TEXT NOT NULL,
    ping_ms INT DEFAULT 35,
    load_percentage INT DEFAULT 40,
    max_users INT DEFAULT 1000,
    current_users INT DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ONLINE',
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Active Connections (Telemetry & Session Monitoring)
CREATE TABLE IF NOT EXISTS active_connections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL REFERENCES activation_codes(code) ON DELETE CASCADE,
    device_id VARCHAR(128) NOT NULL,
    server_id VARCHAR(64) NOT NULL REFERENCES vpn_servers(id) ON DELETE CASCADE,
    client_ip VARCHAR(64),
    virtual_ip VARCHAR(64) DEFAULT '10.8.0.2',
    protocol VARCHAR(32) DEFAULT 'WIREGUARD',
    connected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    bytes_sent BIGINT DEFAULT 0,
    bytes_received BIGINT DEFAULT 0,
    status VARCHAR(32) DEFAULT 'CONNECTED'
);

CREATE INDEX IF NOT EXISTS idx_active_conn_user ON active_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_active_conn_server ON active_connections(server_id);

-- 5. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    admin_user VARCHAR(255) DEFAULT 'system',
    action VARCHAR(128) NOT NULL,
    target VARCHAR(255),
    details TEXT,
    ip_address VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp DESC);

-- ==========================================================
-- SEED INITIAL PRODUCTION DATA
-- ==========================================================

-- Default admin: admin@superfrist.vpn (bcrypt hash for 'Admin@2026!')
INSERT INTO admin_users (email, password_hash, role)
VALUES ('admin@superfrist.vpn', '$2b$12$eJ3k0eH1w/gGj8xR1N7.h.78sL5tQ9O6VjY2k8bE9q3mP5j2Y1Z2.', 'SUPER_ADMIN')
ON CONFLICT (email) DO NOTHING;

-- Seed default VPN Servers
INSERT INTO vpn_servers (id, name, country, country_code, flag_emoji, city, host, port, protocol, public_key, ping_ms, load_percentage, status, is_visible)
VALUES 
('srv_uae_dubai', 'UAE - Dubai', 'UAE', 'AE', '🇦🇪', 'Dubai', 'ae-dxb.vpn.superfrist.net', 51820, 'WIREGUARD', '0a5k9N2Z8wXz1mK+SuperFristUAEDubaiNode=', 32, 45, 'ONLINE', true),
('srv_singapore', 'Singapore', 'Singapore', 'SG', '🇸🇬', 'Singapore Central', 'sg-sin.vpn.superfrist.net', 51820, 'WIREGUARD', '9uY71B+SuperFristSingaporeGatewayPubKey=', 48, 58, 'ONLINE', true),
('srv_germany', 'Germany - Frankfurt', 'Germany', 'DE', '🇩🇪', 'Frankfurt', 'de-fra.vpn.superfrist.net', 51820, 'WIREGUARD', '7jK4xL+SuperFristGermanyNodePublicKy=', 65, 38, 'ONLINE', true),
('srv_netherlands', 'Netherlands - Amsterdam', 'Netherlands', 'NL', '🇳🇱', 'Amsterdam', 'nl-ams.vpn.superfrist.net', 51820, 'WIREGUARD', '5mN8qW+SuperFristNetherlandsHighSpeed=', 68, 42, 'ONLINE', true),
('srv_usa', 'United States - New York', 'United States', 'US', '🇺🇸', 'New York', 'us-nyc.vpn.superfrist.net', 51820, 'WIREGUARD', '3pL2oK+SuperFristUSANewYorkSecureKey=', 110, 64, 'ONLINE', true),
('srv_uk', 'United Kingdom - London', 'United Kingdom', 'GB', '🇬🇧', 'London', 'uk-lon.vpn.superfrist.net', 51820, 'WIREGUARD', '1wE9rT+SuperFristLondonDirectAccessKey=', 72, 51, 'ONLINE', true)
ON CONFLICT (id) DO NOTHING;

-- Seed initial test codes
INSERT INTO activation_codes (code, status, validity_days, notes)
VALUES 
('VPN-8F7K-29MX-QP4A', 'ACTIVE', 30, 'Production Seed Code (30 Days)'),
('VPN-FAST-7777-UAE1', 'ACTIVE', 60, 'VIP Ultra Fast (60 Days)'),
('VPN-PRO1-9999-YEAR', 'ACTIVE', 365, 'Annual Pass (365 Days)')
ON CONFLICT (code) DO NOTHING;
