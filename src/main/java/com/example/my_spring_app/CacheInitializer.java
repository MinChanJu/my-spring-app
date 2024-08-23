package com.example.my_spring_app;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CacheInitializer {

    private final CacheManager cacheManager;

    public CacheInitializer(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings("null")
    @PostConstruct
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
        });
    }
}
