package com.deahtstroke.rivenbot.config;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Data
@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "cache")
public class RedisConfiguration {

  private Map<String, Long> expirations;

  @Bean
  RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory();
  }

  @Bean
  RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
    Map<String, RedisCacheConfiguration> initialCaches = this.expirations.entrySet().stream()
        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(),
            createConfiguration(entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
        .withInitialCacheConfigurations(initialCaches)
        .transactionAware()
        .build();
  }

  private RedisCacheConfiguration createConfiguration(Long timeToLive) {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(timeToLive))
        .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer()));
  }
}
