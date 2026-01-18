package com.thinhpay.backend.modules.iam.domain.user;

public enum KycLevel {
    /**
     * KYC cơ bản
     * - Hạn mức: < 20,000,000 VND/ngày
     * - Yêu cầu: Email + Phone verification
     */
    BASIC,

    /**
     * KYC nâng cao
     * - Hạn mức: < 100,000,000 VND/ngày
     * - Yêu cầu: BASIC + CMND/CCCD + Selfie
     */
    ADVANCED,

    /**
     * KYC cao cấp
     * - Hạn mức: Không giới hạn
     * - Yêu cầu: ADVANCED + Proof of address + Video verification
     */
    PREMIUM
}

