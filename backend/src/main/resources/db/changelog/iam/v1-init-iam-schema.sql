--liquibase formatted sql

--changeset thinhdev:iam-001 splitStatements:false
--comment: Khởi tạo IAM Domain Schema - Identity & Access Management

-- ============================================
-- 1. BẢNG ROLES - Quản lý vai trò người dùng
-- ============================================
CREATE TABLE iam_roles (
                           id VARCHAR(20) PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
                           permissions TEXT,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE iam_roles IS 'Bảng vai trò người dùng (USER, ADMIN, MERCHANT, AGENT)';
COMMENT ON COLUMN iam_roles.permissions IS 'JSON array chứa danh sách quyền';


-- ============================================
-- 2. BẢNG USERS - Bảng người dùng chính
-- ============================================
CREATE TABLE iam_users (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           email VARCHAR(255) UNIQUE NOT NULL,
                           phone_number VARCHAR(20) UNIQUE NOT NULL,
                           password_hash VARCHAR(255) NOT NULL,
                           pin_hash VARCHAR(255),
                           full_name VARCHAR(255) NOT NULL,
                           avatar_url VARCHAR(500),
                           status VARCHAR(20) NOT NULL DEFAULT 'PENDING_VERIFICATION',
                           role_id VARCHAR(20) NOT NULL DEFAULT 'USER',
                           email_verified BOOLEAN DEFAULT FALSE,
                           phone_verified BOOLEAN DEFAULT FALSE,
                           kyc_verified BOOLEAN DEFAULT FALSE,
                           kyc_level VARCHAR(20) DEFAULT 'BASIC',
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           version BIGINT NOT NULL DEFAULT 0,

                           CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES iam_roles(id),
                           CONSTRAINT chk_users_status CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'LOCKED', 'CLOSED')),
                           CONSTRAINT chk_users_kyc_level CHECK (kyc_level IN ('BASIC', 'ADVANCED', 'PREMIUM'))
);

COMMENT ON TABLE iam_users IS 'Bảng người dùng chính - lưu thông tin identity và authentication';
COMMENT ON COLUMN iam_users.password_hash IS 'BCrypt hash với cost factor 12';
COMMENT ON COLUMN iam_users.pin_hash IS 'PIN 6 chữ số cho quick authentication';
COMMENT ON COLUMN iam_users.status IS 'Trạng thái tài khoản: PENDING_VERIFICATION → ACTIVE → SUSPENDED/LOCKED';
COMMENT ON COLUMN iam_users.kyc_level IS 'BASIC: <20M/day, ADVANCED: <100M/day, PREMIUM: unlimited';
COMMENT ON COLUMN iam_users.version IS 'Optimistic locking version';


-- ============================================
-- 3. BẢNG REFRESH TOKENS - Quản lý JWT tokens
-- ============================================
CREATE TABLE iam_refresh_tokens (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    user_id UUID NOT NULL,
                                    token VARCHAR(500) UNIQUE NOT NULL,
                                    device_fingerprint VARCHAR(255),
                                    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    revoked BOOLEAN DEFAULT FALSE,
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    last_used_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES iam_users(id) ON DELETE CASCADE
);

COMMENT ON TABLE iam_refresh_tokens IS 'Refresh tokens cho JWT authentication (TTL: 7 days)';
COMMENT ON COLUMN iam_refresh_tokens.token IS 'UUID v4 refresh token';
COMMENT ON COLUMN iam_refresh_tokens.device_fingerprint IS 'Browser/device fingerprint để detect device switching';
COMMENT ON COLUMN iam_refresh_tokens.revoked IS 'True khi user logout hoặc admin revoke';


-- ============================================
-- 4. BẢNG OTP CODES - Mã xác thực OTP
-- ============================================
CREATE TABLE iam_otp_codes (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL,
                               code VARCHAR(6) NOT NULL,
                               type VARCHAR(20) NOT NULL,
                               purpose VARCHAR(50) NOT NULL,
                               expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               verified BOOLEAN DEFAULT FALSE,
                               attempts INT DEFAULT 0,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               verified_at TIMESTAMP WITH TIME ZONE,

                               CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES iam_users(id) ON DELETE CASCADE,
                               CONSTRAINT chk_otp_type CHECK (type IN ('EMAIL', 'SMS', 'PHONE_CALL')),
                               CONSTRAINT chk_otp_purpose CHECK (purpose IN ('VERIFY_EMAIL', 'VERIFY_PHONE', 'LOGIN', 'TRANSFER', 'CHANGE_PASSWORD', 'RESET_PIN')),
                               CONSTRAINT chk_otp_attempts CHECK (attempts <= 3)
);

COMMENT ON TABLE iam_otp_codes IS 'Mã OTP cho xác thực 2FA (TTL: 5 minutes, max 3 attempts)';
COMMENT ON COLUMN iam_otp_codes.code IS '6 digit OTP code';
COMMENT ON COLUMN iam_otp_codes.type IS 'Kênh gửi: EMAIL, SMS, PHONE_CALL';
COMMENT ON COLUMN iam_otp_codes.purpose IS 'Mục đích: VERIFY_EMAIL, LOGIN, TRANSFER, etc.';
COMMENT ON COLUMN iam_otp_codes.attempts IS 'Số lần verify sai (max 3)';


-- ============================================
-- 5. BẢNG SESSIONS - Quản lý phiên đăng nhập
-- ============================================
CREATE TABLE iam_sessions (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id UUID NOT NULL,
                              session_token VARCHAR(500) UNIQUE NOT NULL,
                              ip_address VARCHAR(45),
                              user_agent TEXT,
                              device_id VARCHAR(255),
                              expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                              active BOOLEAN DEFAULT TRUE,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              last_activity_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES iam_users(id) ON DELETE CASCADE
);

COMMENT ON TABLE iam_sessions IS 'Active sessions - hỗ trợ "logout all devices" (timeout: 30 minutes inactive)';
COMMENT ON COLUMN iam_sessions.session_token IS 'Unique session identifier';
COMMENT ON COLUMN iam_sessions.ip_address IS 'Client IP address (IPv4/IPv6)';
COMMENT ON COLUMN iam_sessions.device_id IS 'Mobile device UUID hoặc browser fingerprint';


-- ============================================
-- 6. BẢNG USER DEVICES - Thiết bị người dùng
-- ============================================
CREATE TABLE iam_user_devices (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id UUID NOT NULL,
                                  device_id VARCHAR(255) UNIQUE NOT NULL,
                                  device_name VARCHAR(255),
                                  device_type VARCHAR(20),
                                  os_name VARCHAR(50),
                                  os_version VARCHAR(20),
                                  app_version VARCHAR(20),
                                  fcm_token VARCHAR(500),
                                  trusted BOOLEAN DEFAULT FALSE,
                                  biometric_enabled BOOLEAN DEFAULT FALSE,
                                  first_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_devices_user FOREIGN KEY (user_id) REFERENCES iam_users(id) ON DELETE CASCADE,
                                  CONSTRAINT chk_device_type CHECK (device_type IN ('MOBILE', 'TABLET', 'WEB', 'DESKTOP'))
);

COMMENT ON TABLE iam_user_devices IS 'Quản lý thiết bị đã đăng nhập - hỗ trợ trusted devices và push notifications';
COMMENT ON COLUMN iam_user_devices.device_id IS 'Unique device identifier (UUID)';
COMMENT ON COLUMN iam_user_devices.device_name IS 'User-friendly name: iPhone 15 Pro, Samsung S23...';
COMMENT ON COLUMN iam_user_devices.fcm_token IS 'Firebase Cloud Messaging token cho push notifications';
COMMENT ON COLUMN iam_user_devices.trusted IS 'Trusted device - skip OTP verification';
COMMENT ON COLUMN iam_user_devices.biometric_enabled IS 'Face ID, Touch ID, Fingerprint enabled';


-- ============================================
-- 7. BẢNG SECURITY LOGS - Nhật ký bảo mật
-- ============================================
CREATE TABLE iam_security_logs (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id UUID,
                                   event_type VARCHAR(50) NOT NULL,
                                   ip_address VARCHAR(45),
                                   user_agent TEXT,
                                   device_id VARCHAR(255),
                                   metadata TEXT,
                                   risk_level VARCHAR(20) DEFAULT 'LOW',
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_security_logs_user FOREIGN KEY (user_id) REFERENCES iam_users(id) ON DELETE SET NULL,
                                   CONSTRAINT chk_security_risk_level CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

COMMENT ON TABLE iam_security_logs IS 'Audit trail - log tất cả security events (login, logout, password change, etc.)';
COMMENT ON COLUMN iam_security_logs.event_type IS 'LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, PASSWORD_CHANGED, OTP_SENT, TRANSFER_APPROVED, etc.';
COMMENT ON COLUMN iam_security_logs.metadata IS 'JSON - thông tin bổ sung về event';
COMMENT ON COLUMN iam_security_logs.risk_level IS 'LOW: normal, MEDIUM: suspicious IP, HIGH: multiple failed attempts, CRITICAL: account hijacking';


-- ============================================
-- 8. INDEXES - Tối ưu performance
-- ============================================

-- Login queries (CRITICAL - mỗi lần login)
CREATE UNIQUE INDEX idx_users_email ON iam_users(email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX idx_users_phone ON iam_users(phone_number) WHERE phone_number IS NOT NULL;
CREATE INDEX idx_users_status ON iam_users(status);
CREATE INDEX idx_users_role ON iam_users(role_id);
CREATE INDEX idx_users_kyc_level ON iam_users(kyc_level);

-- Token validation (CRITICAL - mỗi API request)
CREATE UNIQUE INDEX idx_refresh_tokens_token ON iam_refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON iam_refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON iam_refresh_tokens(expires_at) WHERE NOT revoked;
CREATE INDEX idx_refresh_tokens_user_expires ON iam_refresh_tokens(user_id, expires_at) WHERE NOT revoked;

-- OTP verification (TIME-CRITICAL - 5 minutes TTL)
CREATE INDEX idx_otp_user_purpose ON iam_otp_codes(user_id, purpose, verified);
CREATE INDEX idx_otp_expires ON iam_otp_codes(expires_at) WHERE NOT verified;
CREATE INDEX idx_otp_code_lookup ON iam_otp_codes(user_id, code) WHERE NOT verified;

-- Session management
CREATE UNIQUE INDEX idx_sessions_token ON iam_sessions(session_token);
CREATE INDEX idx_sessions_user_id ON iam_sessions(user_id);
CREATE INDEX idx_sessions_user_active ON iam_sessions(user_id, active);
CREATE INDEX idx_sessions_expires ON iam_sessions(expires_at) WHERE active;

-- Device lookup
CREATE UNIQUE INDEX idx_devices_device_id ON iam_user_devices(device_id);
CREATE INDEX idx_devices_user_id ON iam_user_devices(user_id);
CREATE INDEX idx_devices_user_trusted ON iam_user_devices(user_id, trusted);

-- Security logs (Audit queries)
CREATE INDEX idx_security_logs_user_id ON iam_security_logs(user_id, created_at DESC);
CREATE INDEX idx_security_logs_event_type ON iam_security_logs(event_type, created_at DESC);
CREATE INDEX idx_security_logs_risk ON iam_security_logs(risk_level, created_at DESC);
CREATE INDEX idx_security_logs_created_at ON iam_security_logs(created_at DESC);


-- ============================================
-- 9. SEED DATA - Roles mặc định
-- ============================================

INSERT INTO iam_roles (id, name, description, permissions) VALUES
                                                               (
                                                                   'USER',
                                                                   'User',
                                                                   'Khách hàng cá nhân',
                                                                   '["transfer", "deposit", "withdraw", "view_balance", "view_transactions"]'
                                                               ),
                                                               (
                                                                   'MERCHANT',
                                                                   'Merchant',
                                                                   'Tài khoản doanh nghiệp',
                                                                   '["receive_payment", "qr_generate", "refund", "view_revenue", "export_report"]'
                                                               ),
                                                               (
                                                                   'AGENT',
                                                                   'Agent',
                                                                   'Đại lý nạp/rút tiền',
                                                                   '["cash_in", "cash_out", "kyc_verify", "commission_view"]'
                                                               ),
                                                               (
                                                                   'ADMIN',
                                                                   'Administrator',
                                                                   'Quản trị viên hệ thống',
                                                                   '["all"]'
                                                               );
-- ============================================
-- 10. BẢNG TOKEN BLACKLIST - Chặn JWT đã logout
-- ============================================
CREATE TABLE iam_token_blacklist (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     jti VARCHAR(255) UNIQUE NOT NULL,
                                     user_id UUID NOT NULL,
                                     token_type VARCHAR(20) NOT NULL,
                                     reason VARCHAR(50) NOT NULL,
                                     expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_blacklist_user FOREIGN KEY (user_id) REFERENCES iam_users(id) ON DELETE CASCADE,
                                     CONSTRAINT chk_token_type CHECK (token_type IN ('ACCESS', 'REFRESH')),
                                     CONSTRAINT chk_blacklist_reason CHECK (reason IN ('USER_LOGOUT', 'ADMIN_REVOKE', 'PASSWORD_CHANGED', 'SECURITY_BREACH', 'SUSPICIOUS_ACTIVITY'))
);

COMMENT ON TABLE iam_token_blacklist IS 'Token blacklist - chặn JWT đã logout/revoke (sync với Redis)';
COMMENT ON COLUMN iam_token_blacklist.jti IS 'JWT ID (claim "jti") - unique identifier của JWT token';
COMMENT ON COLUMN iam_token_blacklist.token_type IS 'Loại token: ACCESS (15min) hoặc REFRESH (7 days)';
COMMENT ON COLUMN iam_token_blacklist.reason IS 'Lý do blacklist: USER_LOGOUT, ADMIN_REVOKE, PASSWORD_CHANGED...';
COMMENT ON COLUMN iam_token_blacklist.expires_at IS 'TTL - tự động xóa sau khi token hết hạn';

-- Index cho blacklist lookup (CRITICAL - mỗi API request)
CREATE UNIQUE INDEX idx_blacklist_jti ON iam_token_blacklist(jti);
CREATE INDEX idx_blacklist_user ON iam_token_blacklist(user_id);
CREATE INDEX idx_blacklist_expires ON iam_token_blacklist(expires_at);


-- ============================================
-- 11. ENHANCED SESSIONS - Thêm JTI tracking
-- ============================================
ALTER TABLE iam_sessions
    ADD COLUMN access_token_jti VARCHAR(255),
    ADD COLUMN refresh_token_jti VARCHAR(255);

COMMENT ON COLUMN iam_sessions.access_token_jti IS 'JWT ID của access token hiện tại - dùng để revoke';
COMMENT ON COLUMN iam_sessions.refresh_token_jti IS 'JWT ID của refresh token - dùng để revoke';

-- Index cho JTI lookup
CREATE INDEX idx_sessions_access_jti ON iam_sessions(access_token_jti) WHERE active;
CREATE INDEX idx_sessions_refresh_jti ON iam_sessions(refresh_token_jti) WHERE active;


-- ============================================
-- 12. STORED PROCEDURES - Token Revocation
-- ============================================

-- Procedure: Revoke all user tokens (logout all devices)
CREATE OR REPLACE FUNCTION revoke_all_user_tokens(
    p_user_id UUID,
    p_reason VARCHAR(50) DEFAULT 'USER_LOGOUT'
)
RETURNS TABLE(
    revoked_sessions INT,
    revoked_refresh_tokens INT,
    blacklisted_access_tokens INT
) AS $$
DECLARE
v_revoked_sessions INT;
    v_revoked_refresh INT;
    v_blacklisted INT := 0;
BEGIN
    -- 1. Blacklist tất cả access tokens từ active sessions
INSERT INTO iam_token_blacklist (jti, user_id, token_type, reason, expires_at)
SELECT
    access_token_jti,
    p_user_id,
    'ACCESS',
    p_reason,
    NOW() + INTERVAL '15 minutes'
FROM iam_sessions
WHERE user_id = p_user_id
  AND active = TRUE
  AND access_token_jti IS NOT NULL;

GET DIAGNOSTICS v_blacklisted = ROW_COUNT;

-- 2. Deactivate tất cả sessions
UPDATE iam_sessions
SET active = FALSE,
    last_activity_at = NOW()
WHERE user_id = p_user_id AND active = TRUE;

GET DIAGNOSTICS v_revoked_sessions = ROW_COUNT;

-- 3. Revoke tất cả refresh tokens
UPDATE iam_refresh_tokens
SET revoked = TRUE
WHERE user_id = p_user_id AND revoked = FALSE;

GET DIAGNOSTICS v_revoked_refresh = ROW_COUNT;

-- 4. Log security event
INSERT INTO iam_security_logs (user_id, event_type, metadata, risk_level)
VALUES (
           p_user_id,
           'LOGOUT_ALL_DEVICES',
           json_build_object(
                   'revoked_sessions', v_revoked_sessions,
                   'revoked_refresh_tokens', v_revoked_refresh,
                   'blacklisted_tokens', v_blacklisted,
                   'reason', p_reason
           )::TEXT,
           CASE
               WHEN p_reason = 'SECURITY_BREACH' THEN 'CRITICAL'
               WHEN p_reason = 'SUSPICIOUS_ACTIVITY' THEN 'HIGH'
               ELSE 'MEDIUM'
               END
       );

RETURN QUERY SELECT v_revoked_sessions, v_revoked_refresh, v_blacklisted;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION revoke_all_user_tokens IS 'Revoke tất cả tokens của user - "Logout all devices"';


-- Procedure: Blacklist single token
CREATE OR REPLACE FUNCTION blacklist_token(
    p_jti VARCHAR(255),
    p_user_id UUID,
    p_token_type VARCHAR(20),
    p_reason VARCHAR(50),
    p_ttl_minutes INT DEFAULT 15
)
RETURNS BOOLEAN AS $$
BEGIN
INSERT INTO iam_token_blacklist (jti, user_id, token_type, reason, expires_at)
VALUES (
           p_jti,
           p_user_id,
           p_token_type,
           p_reason,
           NOW() + (p_ttl_minutes || ' minutes')::INTERVAL
       )
    ON CONFLICT (jti) DO NOTHING;

IF p_token_type = 'ACCESS' THEN
UPDATE iam_sessions
SET active = FALSE
WHERE access_token_jti = p_jti;
END IF;

RETURN TRUE;
EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql;


-- ============================================
-- 13. TRIGGERS - Auto-blacklist on password change
-- ============================================

CREATE OR REPLACE FUNCTION trigger_revoke_tokens_on_password_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.password_hash IS DISTINCT FROM NEW.password_hash THEN
        PERFORM revoke_all_user_tokens(NEW.id, 'PASSWORD_CHANGED');
        RAISE NOTICE 'Auto-revoked all tokens for user % due to password change', NEW.email;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_revoke_on_password_change
    AFTER UPDATE ON iam_users
    FOR EACH ROW
    EXECUTE FUNCTION trigger_revoke_tokens_on_password_change();


-- ============================================
-- 14. POSTGRES NOTIFY - Redis Sync Mechanism
-- ============================================

-- Function: Notify Redis khi có token bị blacklist
CREATE OR REPLACE FUNCTION notify_token_blacklist()
RETURNS TRIGGER AS $$
DECLARE
    v_payload JSON;
BEGIN
    -- Build payload để gửi qua NOTIFY
    v_payload := json_build_object(
        'jti', NEW.jti,
        'user_id', NEW.user_id,
        'token_type', NEW.token_type,
        'ttl_seconds', EXTRACT(EPOCH FROM (NEW.expires_at - NOW()))::INT
    );

    -- Send notification qua channel 'token_blacklist'
    PERFORM pg_notify('token_blacklist', v_payload::TEXT);

    RAISE NOTICE 'Notified Redis: Token % blacklisted', NEW.jti;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: Auto-notify khi insert vào blacklist
CREATE TRIGGER trg_notify_blacklist
    AFTER INSERT ON iam_token_blacklist
    FOR EACH ROW
    EXECUTE FUNCTION notify_token_blacklist();

COMMENT ON TRIGGER trg_notify_blacklist ON iam_token_blacklist IS
    'Auto-notify Redis qua Postgres LISTEN/NOTIFY khi có token mới bị blacklist';


-- ============================================
-- 15. REFRESH TOKEN ROTATION - Enhanced Security
-- ============================================

-- Thêm column jti vào iam_refresh_tokens
ALTER TABLE iam_refresh_tokens
    ADD COLUMN jti VARCHAR(255) UNIQUE;

COMMENT ON COLUMN iam_refresh_tokens.jti IS 'JWT ID của refresh token - dùng cho rotation attack prevention';

-- Index cho JTI lookup (CRITICAL - token refresh flow)
CREATE UNIQUE INDEX idx_refresh_tokens_jti ON iam_refresh_tokens(jti) WHERE jti IS NOT NULL;

-- Function: Rotate refresh token (revoke old, create new)
CREATE OR REPLACE FUNCTION rotate_refresh_token(
    p_old_jti VARCHAR(255),
    p_new_jti VARCHAR(255),
    p_user_id UUID,
    p_device_fingerprint VARCHAR(255),
    p_ttl_days INT DEFAULT 7
)
RETURNS TABLE(
    old_token_revoked BOOLEAN,
    new_token_id UUID
) AS $$
DECLARE
    v_old_token_id UUID;
    v_new_token_id UUID;
BEGIN
    -- 1. Revoke old refresh token by JTI
    UPDATE iam_refresh_tokens
    SET revoked = TRUE
    WHERE jti = p_old_jti
      AND user_id = p_user_id
      AND revoked = FALSE
    RETURNING id INTO v_old_token_id;

    -- 2. Blacklist old refresh token
    IF v_old_token_id IS NOT NULL THEN
        INSERT INTO iam_token_blacklist (jti, user_id, token_type, reason, expires_at)
        VALUES (
            p_old_jti,
            p_user_id,
            'REFRESH',
            'TOKEN_ROTATION',
            NOW() + (p_ttl_days || ' days')::INTERVAL
        )
        ON CONFLICT (jti) DO NOTHING;
    END IF;

    -- 3. Create new refresh token entry
    INSERT INTO iam_refresh_tokens (
        jti,
        user_id,
        token, -- Will be set by application layer
        device_fingerprint,
        expires_at
    ) VALUES (
        p_new_jti,
        p_user_id,
        p_new_jti, -- Temporary, app will update with actual token
        p_device_fingerprint,
        NOW() + (p_ttl_days || ' days')::INTERVAL
    )
    RETURNING id INTO v_new_token_id;

    -- 4. Log rotation event
    INSERT INTO iam_security_logs (user_id, event_type, metadata, risk_level)
    VALUES (
        p_user_id,
        'REFRESH_TOKEN_ROTATED',
        json_build_object(
            'old_jti', p_old_jti,
            'new_jti', p_new_jti,
            'revoked_old', (v_old_token_id IS NOT NULL)
        )::TEXT,
        'LOW'
    );

    RETURN QUERY SELECT (v_old_token_id IS NOT NULL), v_new_token_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rotate_refresh_token IS
    'Rotate refresh token - revoke old, blacklist it, create new (prevent Refresh Token Rotation Attack)';


-- Function: Detect refresh token reuse (potential attack)
CREATE OR REPLACE FUNCTION detect_token_reuse(p_jti VARCHAR(255))
RETURNS TABLE(
    is_reused BOOLEAN,
    user_id UUID,
    risk_level VARCHAR(20)
) AS $$
DECLARE
    v_token_record RECORD;
    v_is_blacklisted BOOLEAN;
BEGIN
    -- Check if token đã bị revoke
    SELECT * INTO v_token_record
    FROM iam_refresh_tokens
    WHERE jti = p_jti;

    -- Check if token đã bị blacklist
    SELECT EXISTS(
        SELECT 1 FROM iam_token_blacklist
        WHERE jti = p_jti
    ) INTO v_is_blacklisted;

    -- Nếu token đã revoke/blacklist nhưng vẫn được dùng → ATTACK!
    IF v_token_record.revoked OR v_is_blacklisted THEN
        -- Log critical security event
        INSERT INTO iam_security_logs (user_id, event_type, metadata, risk_level)
        VALUES (
            v_token_record.user_id,
            'REFRESH_TOKEN_REUSE_DETECTED',
            json_build_object(
                'jti', p_jti,
                'revoked', v_token_record.revoked,
                'blacklisted', v_is_blacklisted
            )::TEXT,
            'CRITICAL'
        );

        -- Auto-revoke tất cả tokens của user (compromised)
        PERFORM revoke_all_user_tokens(v_token_record.user_id, 'SECURITY_BREACH');

        RETURN QUERY SELECT TRUE, v_token_record.user_id, 'CRITICAL'::VARCHAR(20);
    ELSE
        RETURN QUERY SELECT FALSE, v_token_record.user_id, 'LOW'::VARCHAR(20);
    END IF;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION detect_token_reuse IS
    'Detect refresh token reuse (Rotation Attack) - auto-revoke all tokens nếu phát hiện';


-- ============================================
-- 16. REDIS SYNC HELPER VIEWS
-- ============================================

-- View: Active blacklist entries (dùng để sync Redis on startup)
CREATE OR REPLACE VIEW v_active_blacklist AS
SELECT
    jti,
    user_id,
    token_type,
    reason,
    EXTRACT(EPOCH FROM (expires_at - NOW()))::INT as ttl_seconds
FROM iam_token_blacklist
WHERE expires_at > NOW()
ORDER BY created_at DESC;

COMMENT ON VIEW v_active_blacklist IS
    'Active blacklist entries - dùng để sync Redis cache on application startup';


-- ============================================
-- 17. CLEANUP FUNCTIONS - Enhanced
-- ============================================

CREATE OR REPLACE FUNCTION cleanup_expired_blacklist()
RETURNS void AS $$
BEGIN
    DELETE FROM iam_token_blacklist
    WHERE expires_at < NOW();

    RAISE NOTICE 'Cleaned up % expired blacklist entries', FOUND;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION cleanup_expired_otps()
RETURNS void AS $$
BEGIN
    DELETE FROM iam_otp_codes
    WHERE expires_at < NOW() - INTERVAL '7 days';

    RAISE NOTICE 'Cleaned up % expired OTP codes', FOUND;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS void AS $$
BEGIN
    DELETE FROM iam_refresh_tokens
    WHERE expires_at < NOW() - INTERVAL '30 days';

    RAISE NOTICE 'Cleaned up % expired refresh tokens', FOUND;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION cleanup_inactive_sessions()
RETURNS void AS $$
BEGIN
    DELETE FROM iam_sessions
    WHERE expires_at < NOW() - INTERVAL '90 days'
       OR (NOT active AND last_activity_at < NOW() - INTERVAL '30 days');

    RAISE NOTICE 'Cleaned up % inactive sessions', FOUND;
END;
$$ LANGUAGE plpgsql;


-- ============================================
-- 18. SCHEDULED MAINTENANCE NOTES
-- ============================================

COMMENT ON SCHEMA public IS 'ThinhPay IAM Domain - Scheduled Jobs Required:
1. Cleanup expired blacklist: SELECT cleanup_expired_blacklist(); -- Hourly
2. Cleanup expired OTP: SELECT cleanup_expired_otps(); -- Daily
3. Cleanup expired tokens: SELECT cleanup_expired_tokens(); -- Daily
4. Cleanup inactive sessions: SELECT cleanup_inactive_sessions(); -- Daily
5. Redis Sync: LISTEN to "token_blacklist" channel for real-time sync';


-- ============================================
-- 19. PERFORMANCE MONITORING VIEWS
-- ============================================

-- View: Blacklist performance stats
CREATE OR REPLACE VIEW v_blacklist_stats AS
SELECT
    DATE(created_at) as date,
    token_type,
    reason,
    COUNT(*) as count,
    AVG(EXTRACT(EPOCH FROM (expires_at - created_at)))::INT as avg_ttl_seconds
FROM iam_token_blacklist
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at), token_type, reason
ORDER BY date DESC, count DESC;

COMMENT ON VIEW v_blacklist_stats IS 'Blacklist statistics - monitor logout patterns và security events';