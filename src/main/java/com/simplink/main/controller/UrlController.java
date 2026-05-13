package com.simplink.main.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.simplink.main.dto.UrlRequest;
import com.simplink.main.service.UrlService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

import java.net.URI;

@RestController
@AllArgsConstructor
public class UrlController {

    private final UrlService urlService;

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
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}