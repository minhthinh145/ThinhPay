--liquibase formatted sql

--changeset thinhdev:001
--comment: Khoi tao Core Banking Schema (Currencies, Accounts, Transactions, Ledger)

-- 1. Bảng Tiền tệ
CREATE TABLE core_currencies (
                                 code VARCHAR(3) PRIMARY KEY,
                                 symbol VARCHAR(5) NOT NULL,
                                 decimal_places INT DEFAULT 0
);

-- 2. Bảng Tài khoản
CREATE TABLE core_accounts (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               currency_code VARCHAR(3) REFERENCES core_currencies(code),
                               balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
                               held_balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
                               status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                               version BIGINT NOT NULL DEFAULT 0,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Bảng Giao dịch
CREATE TABLE core_transactions (
                                   id UUID PRIMARY KEY,
                                   account_id UUID NOT NULL REFERENCES core_accounts(id),
                                   request_id VARCHAR(100) NOT NULL,
                                   amount NUMERIC(19, 4) NOT NULL,
                                   type VARCHAR(20) NOT NULL,
                                   status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                   metadata TEXT,
                                   description TEXT,
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- INDEX CHO TRANSACTION
CREATE UNIQUE INDEX idx_trx_request_id ON core_transactions(request_id);
CREATE INDEX idx_trx_account_id ON core_transactions(account_id);


-- 4. Sổ cái Bút toán kép (Ledger)
CREATE TABLE core_ledger_entries (
                                     id UUID PRIMARY KEY,
                                     transaction_id UUID REFERENCES core_transactions(id),
                                     account_id UUID REFERENCES core_accounts(id),
                                     amount NUMERIC(19, 4) NOT NULL,
                                     balance_snapshot NUMERIC(19, 4) NOT NULL,
                                     entry_type VARCHAR(10) NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- INDEX CHO LEDGER (BỔ SUNG MỚI)
-- Giúp tìm nhanh các bút toán thuộc về 1 giao dịch (quan trọng để check Double Entry)
CREATE INDEX idx_ledger_trx_id ON core_ledger_entries(transaction_id);
-- Giúp tìm nhanh lịch sử biến động số dư của 1 tài khoản
CREATE INDEX idx_ledger_account_id ON core_ledger_entries(account_id);


-- 5. Seed data
INSERT INTO core_currencies (code, symbol, decimal_places) VALUES ('VND', '₫', 0);
INSERT INTO core_currencies (code, symbol, decimal_places) VALUES ('USD', '$', 2);