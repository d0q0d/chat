//package org.tpl.chat.service.config;
//
//import com.github.benmanes.caffeine.cache.Caffeine;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.cache.caffeine.CaffeineCacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//@EnableCaching(proxyTargetClass = true)
//@Slf4j
//public class CacheConfigDefault {
//
//    private Integer duration = 60;
//
//    @Primary
//    @Bean
//    public CacheManager cacheManager() {
//        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
//        cacheManager.setCaffeine(caffeineCacheBuilder());
//        return cacheManager;
//    }
//
//    Caffeine<Object, Object> caffeineCacheBuilder() {
//        log.info("Init Cache timeOut={} minutes",duration);
//        return Caffeine.newBuilder()
//                .expireAfterWrite(duration, TimeUnit.MINUTES)
//                .recordStats();
//    }
//
//
//
//}