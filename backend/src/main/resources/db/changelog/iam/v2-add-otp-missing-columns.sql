--liquibase formatted sql

--changeset thinhdev:iam-002-add-missing-columns splitStatements:true
--comment: Add missing updated_at and version columns to iam_otp_codes table

-- Add updated_at column
ALTER TABLE iam_otp_codes
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN iam_otp_codes.updated_at IS 'Last updated timestamp';

-- Add version column for optimistic locking
ALTER TABLE iam_otp_codes
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN iam_otp_codes.version IS 'Optimistic locking version number';

-- Create index for updated_at
CREATE INDEX IF NOT EXISTS idx_otp_updated_at ON iam_otp_codes(updated_at);
