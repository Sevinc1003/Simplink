package com.simplink.main.service;

import com.simplink.main.dto.UrlResponse;
import com.simplink.main.entity.IpLog;
import com.simplink.main.entity.Url;
import com.simplink.main.entity.User;
import com.simplink.main.repository.UrlRepository;
import com.simplink.main.repository.UserRepository;
import com.simplink.main.util.Base62Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final Base62Util base62Util;
    private final IpLogService ipLogService;

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public String shortenUrl(String originalUrl, String userEmail) {

        User user = getCurrentUser(userEmail);
        Url url = new Url(originalUrl);
        url.setUser(user);
        url = urlRepository.save(url);
        return base62Util.encode(url.getId());
    }

    public Url getUrlEntityByShortCode(String shortCode) {
        long id = base62Util.decode(shortCode);
        return findById(id);
    }

    public List<IpLog> getLogsByShortCode(String shortCode, String userEmail) {
        User currentUser = getCurrentUser(userEmail);
        Url url = getUrlEntityByShortCode(shortCode);
        if (!url.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to view logs for this URL");
        }
        return ipLogService.getAllLogsByUrlId(url.getId());
    }

    public long getClickCountByShortCode(String shortCode, String userEmail) {
        User currentUser = getCurrentUser(userEmail);
        Url url = getUrlEntityByShortCode(shortCode);
        if (!url.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to view click count for this URL");
        }
        return ipLogService.getTotalClicksForUrl(url.getId());
    }

    public Url findById(Long id) {
        return urlRepository.findById(id).orElseThrow(() ->new RuntimeException("url not found"));
    }

    public UrlResponse updateUrl(Long id, String newOriginalUrl) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID cannot be null or less than or equal to zero!");
        }
        if (newOriginalUrl == null || newOriginalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty!");
        }

        Url url = findById(id);
        url.setOriginalUrl(newOriginalUrl);
        Url updatedUrl = urlRepository.save(url);

        String shortCode = base62Util.encode(updatedUrl.getId());

        return new UrlResponse(
                updatedUrl.getId(),
                updatedUrl.getOriginalUrl(),
                shortCode,
                "N/A"
        );
    }

    public void deleteUrl(Long id, String userEmail) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID cannot be null or less than or equal to zero!");
        }
        User currentUser = getCurrentUser(userEmail);
        Url url = findById(id);
        if (!url.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to delete this URL");
        }
        urlRepository.delete(url);
    }
}
