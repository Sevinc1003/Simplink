package com.simplink.main.service;

import com.simplink.main.entity.IpLog;
import com.simplink.main.entity.Url;
import com.simplink.main.repository.UrlRepository;
import com.simplink.main.util.Base62Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final Base62Util base62Util;
    private final IpLogService ipLogService;

    public String shortenUrl(String originalUrl) {
        Url url = new Url(originalUrl);
        url = urlRepository.save(url);
        return base62Util.encode(url.getId());
    }

    public Url getUrlEntityByShortCode(String shortCode) {
        long id = base62Util.decode(shortCode);
        return findById(id);
    }
    public List<IpLog> getLogsByShortCode(String shortCode) {
        long urlId = base62Util.decode(shortCode);
        return ipLogService.getAllLogsByUrlId(urlId);
    }
    public long getClickCountByShortCode(String shortCode) {
        long urlId = base62Util.decode(shortCode);
        return ipLogService.getTotalClicksForUrl(urlId);
    }

    public Url findById(Long id) {
        return urlRepository.findById(id).orElseThrow(() ->new RuntimeException("url not found"));
    }
    public Url updateUrl(Long id, String newOriginalUrl) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID cannot be null or less than or equal to zero!");
        }
        if (newOriginalUrl == null || newOriginalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty!");
        }
        Url url = findById(id);
        url.setOriginalUrl(newOriginalUrl);
        return urlRepository.save(url);
    }

    public void deleteUrl(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID cannot be null or less than or equal to zero!");
        }
        Url url = findById(id);
        urlRepository.delete(url);
    }
}
