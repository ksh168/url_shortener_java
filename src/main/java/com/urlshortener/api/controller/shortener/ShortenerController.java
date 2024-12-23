package com.urlshortener.api.controller.shortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.api.dto.ShortUrlDto;
import com.urlshortener.api.dto.UrlShortenResponse;
import com.urlshortener.api.entity.UrlMapping;
import com.urlshortener.api.services.UrlMasterService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;

@RestController
public class ShortenerController {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    UrlMasterService UrlMasterService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@Valid @RequestBody ShortUrlDto request) {
        try {
            UrlMapping shortened = UrlMasterService.createShortUrl(request);
            String fullShortUrl = baseUrl + "/" + shortened.getShortUrl();

            UrlShortenResponse response = new UrlShortenResponse(
                    true,
                    null,
                    fullShortUrl);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            UrlShortenResponse response = new UrlShortenResponse(
                    false,
                    e.getMessage(),
                    null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable(name = "shortUrl") String shortUrl) {
        try {
            UrlMapping urlMapping = UrlMasterService.getOriginalUrl(shortUrl);
            if (urlMapping != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", urlMapping.getLongUrl());
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            }
            return new ResponseEntity<>("Short URL not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error processing request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
