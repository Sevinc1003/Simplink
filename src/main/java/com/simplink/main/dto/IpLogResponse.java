package com.simplink.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpLogResponse {
    private Long id;
    private Long urlId;
    private String ipAddress;
    private String userAgent;
    private String referrer;
    private String country;
    private LocalDateTime createdAt;
}
