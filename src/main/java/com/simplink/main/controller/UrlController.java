package com.simplink.main.controller;

import com.simplink.main.entity.IpLog;
import com.simplink.main.entity.Url;
import com.simplink.main.service.IpLogService;
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

}