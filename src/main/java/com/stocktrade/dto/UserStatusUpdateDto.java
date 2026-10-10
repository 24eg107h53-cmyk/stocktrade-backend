package com.stocktrade.dto;

import com.stocktrade.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateDto {
    @NotNull(message = "Status cannot be null")
    private UserStatus status;
}
