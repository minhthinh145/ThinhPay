--liquibase formatted sql

--changeset thinhdev:002
--comment: Thêm JPY và GBP vào currencies

INSERT INTO core_currencies (code, symbol, decimal_places)
VALUES ('JPY', '¥', 0)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO core_currencies (code, symbol, decimal_places)
VALUES ('GBP', '£', 2)
ON CONFLICT (code) DO NOTHING;