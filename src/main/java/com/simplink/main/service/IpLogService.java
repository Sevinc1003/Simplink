package com.simplink.main.service;

import java.util.List;

import com.simplink.main.repository.IpLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.simplink.main.entity.IpLog;
import com.simplink.main.entity.Url;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IpLogService {

    private final IpLogRepository ipLogRepository;

    @Async
    public void logClickContext(Url url, HttpServletRequest request) {
        // IP, Brauzer cihazı və gəliş mənbəyini (Referer) tuturuq
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        IpLog ipLog = new IpLog(url, ipAddress, userAgent, referrer);

        ipLogRepository.save(ipLog);
    }


    public List<IpLog> getAllLogsByUrlId(Long urlId) {
        return ipLogRepository.findByUrlId(urlId);
    }


    public long getTotalClicksForUrl(Long urlId) {
        return ipLogRepository.countByUrlId(urlId);
    }


    public String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
            // X-Forwarded-For içində vergüllə ayrılmış bir neçə IP ola bilər, əsl istifadəçi birincidir
            return ipAddress.split(",")[0].trim();
        }

        ipAddress = request.getHeader("X-Real-IP");
        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
            return ipAddress;
        }

        // Əgər heç bir proxy yoxdursa, birbaşa IP-ni al
        return request.getRemoteAddr();
    }
}
