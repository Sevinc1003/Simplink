package com.simplink.main.service;

import com.simplink.main.entity.Url;
import com.simplink.main.repository.UrlRepository;
import com.simplink.main.util.Base62Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final Base62Util base62Util;

    public String shortenUrl(String originalUrl) {
        Url url = new Url(originalUrl);
        url = urlRepository.save(url);
        return base62Util.encode(url.getId());
    }

    public String getOriginalUrl(String shortCode) {
        long id = base62Util.decode(shortCode);
        Url url = findById(id);
        return url.getOriginalUrl();
    }

    public Url findById(Long id) {
        return urlRepository.findById(id).orElseThrow(() ->new RuntimeException("url not found"));
    }

}
