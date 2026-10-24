package com.employeeintelligence.api.config;

import com.employeeintelligence.security.TenantContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;
import java.time.Duration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "cache.redis.enabled", havingValue = "true")
public class RedisCacheConfig {
    @Bean
    public KeyGenerator tenantCacheKeyGenerator(TenantContext tenantContext) {
        return new TenantCacheKeyGenerator(tenantContext);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        var keySerializer = new StringRedisSerializer();
        var valueSerializer = new GenericJackson2JsonRedisSerializer();
        var configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(configuration)
                .transactionAware()
                .build();
    }

    private static final class TenantCacheKeyGenerator implements KeyGenerator {
        private final TenantContext tenantContext;

        private TenantCacheKeyGenerator(TenantContext tenantContext) {
            this.tenantContext = tenantContext;
        }

        @Override
        public Object generate(Object target, Method method, Object... params) {
            // Reads and all employee mutations must address the same versioned tenant snapshot.
            return tenantContext.currentTenantId() + ":workforce-summary:v2";
        }
    }
}
