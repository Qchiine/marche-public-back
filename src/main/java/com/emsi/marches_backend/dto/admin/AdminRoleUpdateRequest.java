package com.emsi.marches_backend.dto.admin;

import com.emsi.marches_backend.model.enums.RoleEnum;
import jakarta.validation.constraints.NotNull;

public record AdminRoleUpdateRequest(
        @NotNull RoleEnum role
) {
}
