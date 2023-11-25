package com.danielvm.destiny2bot;

import com.danielvm.destiny2bot.filter.CachingRequestBodyFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.reactive.function.client.WebClient;

@EnableCaching
@SpringBootApplication
@EnableAspectJAutoProxy
public class Destiny2botApplication {

  public static void main(String[] args) {
    SpringApplication.run(Destiny2botApplication.class, args);
  }

  @Bean
  CacheManager inMemoryCacheManager() {
    return new ConcurrentMapCacheManager();
  }

  @Bean
  public FilterRegistrationBean<CachingRequestBodyFilter> signatureValidationFilterBean() {
    FilterRegistrationBean<CachingRequestBodyFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new CachingRequestBodyFilter());
    registrationBean.addUrlPatterns("/interactions");
    return registrationBean;
  }

  @Bean
  public WebClient.Builder webClient() {
    return WebClient.builder();
  }

}
