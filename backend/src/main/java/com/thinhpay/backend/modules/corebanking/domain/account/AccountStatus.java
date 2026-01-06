package com.thinhpay.backend.modules.corebanking.domain.account;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatus {
    ACTIVE("Tài khoản đang hoạt động"),
    FROZEN("Tài khoản bị đóng băng (tạm khóa)"),
    CLOSED("Tài khoản đã đóng vĩnh viễn"),
    LOCKED("Tài khoản bị khóa do vi phạm hoặc bảo mật");

    private final String description;
}
