package com.danielvm.destiny2bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

@EnableCaching
@SpringBootApplication
public class Destiny2botApplication {

    public static void main(String[] args) {
        SpringApplication.run(Destiny2botApplication.class, args);
    }

    @Bean
    CacheManager inMemoryCacheManager() {
        return new ConcurrentMapCacheManager();
    }

}
