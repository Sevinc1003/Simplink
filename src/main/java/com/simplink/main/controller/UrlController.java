package com.simplink.main.controller;

import com.simplink.main.dto.UrlResponse;
import com.simplink.main.entity.IpLog;
import com.simplink.main.entity.Url;
import com.simplink.main.service.IpLogService;
import com.simplink.main.util.Base62Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.simplink.main.dto.UrlRequest;
import com.simplink.main.service.UrlService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final IpLogService ipLogService;
    private final Base62Util base62Util;


    @PostMapping("/urls")
    public ResponseEntity<String> createShortUrl(
            @RequestBody UrlRequest request,
            HttpServletRequest httpServletRequest,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();
        String shortCode = urlService.shortenUrl(request.getUrl(), userEmail);

        String baseUrl = ServletUriComponentsBuilder
                .fromRequestUri(httpServletRequest)
                .replacePath(null)
                .build()
                .toUriString();

        String fullShortUrl = baseUrl + "/" + shortCode;

        return ResponseEntity.ok(fullShortUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortCode,HttpServletRequest request) {
        Url originalUrl = urlService.getUrlEntityByShortCode(shortCode);
        ipLogService.logClickContext(originalUrl, request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl.getOriginalUrl()))
                .build();
    }

    @GetMapping("/urls/{shortCode}/ip-logs")
    public ResponseEntity<?> getIpLogs(
            @PathVariable String shortCode,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<IpLog> logs = urlService.getLogsByShortCode(shortCode, userEmail);
            return ResponseEntity.ok(logs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/urls/{shortCode}/clicks")
    public ResponseEntity<?> getClickCount(
            @PathVariable String shortCode,
            Authentication authentication
    ) {
        try {
            String userEmail = authentication.getName();

            long clickCount = urlService.getClickCountByShortCode(shortCode, userEmail);

            return ResponseEntity.ok(clickCount);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/api/urls/{id}")
    public ResponseEntity<?> updateUrl(
            @PathVariable Long id, @RequestBody UrlRequest request, @RequestHeader("User-Id") Long userId) {
        try {
            UrlResponse response = urlService.updateUrl(id, request.getUrl(), userId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            if (e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<?> deleteUrl(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {

            String userEmail = authentication.getName();

            urlService.deleteUrl(id, userEmail);

            return ResponseEntity.ok(
                    "URL with ID " + id + " has been deleted."
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

}