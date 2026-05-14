package com.simplink.main.controller;

import com.simplink.main.dto.UrlResponse;
import com.simplink.main.entity.IpLog;
import com.simplink.main.entity.Url;
import com.simplink.main.service.IpLogService;
import com.simplink.main.util.Base62Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            HttpServletRequest httpServletRequest) {

        String shortCode = urlService.shortenUrl(request.getUrl());

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
    public ResponseEntity<List<IpLog>> getIpLogs(@PathVariable String shortCode) {
        List<IpLog> logs = urlService.getLogsByShortCode(shortCode);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/urls/{shortCode}/clicks")
    public ResponseEntity<Long> getClickCount(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getClickCountByShortCode(shortCode));
    }

    @PutMapping("/api/urls/{id}")
    public ResponseEntity<?> updateUrl(@PathVariable Long id, @RequestBody UrlRequest request) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID must be greater than zero!");
            }
            Url updatedUrl = urlService.updateUrl(id, request.getUrl());
            String shortCode = base62Util.encode(updatedUrl.getId());
            UrlResponse response = new UrlResponse(
                    updatedUrl.getId(),
                    updatedUrl.getOriginalUrl(),
                    shortCode,
                    "N/A"
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<?> deleteUrl(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID must be greater than zero!");
            }
            urlService.deleteUrl(id);
            return ResponseEntity.ok("URL with ID " + id + " has been deleted.");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}