package com.fis.app.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration<T> {
	
	 @Value("${spring.redis.host}")
	    private String host;
	    @Value("${spring.redis.port}")
	    private Integer port;
	    @Value("${spring.redis.password}")
	    private String password;
	    @Bean
	    public LettuceConnectionFactory lettuceConnectionFactory() {
	        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
	        redisStandaloneConfiguration.setHostName(host);
	        redisStandaloneConfiguration.setPort(port);
	        redisStandaloneConfiguration.setPassword(password);

//	        RedisSentinelConfiguration redisSentinelConfig = new RedisSentinelConfiguration();
//	        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();

	        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration
	                .builder()
	                .build();

	        return new LettuceConnectionFactory(
	                redisStandaloneConfiguration,
	                clientConfiguration
	        );
	    }
//	    @Bean
//	    public JedisConnectionFactory jedisConnectionFactory() {
//	        Duration readTimeout = Duration.ofMillis(30 * 1000L);
//	        Duration connectTimeout = Duration.ofMillis(3 * 1000L);
	//
//	        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
//	        redisStandaloneConfiguration.setHostName(host);
//	        redisStandaloneConfiguration.setPort(port);
//	        redisStandaloneConfiguration.setPassword(password);
	//
//	        JedisClientConfiguration clientConfiguration = JedisClientConfiguration
//	                .builder()
//	                .readTimeout(readTimeout)
//	                .connectTimeout(connectTimeout)
//	                .usePooling()
//	                .build();
	//
//	        return new JedisConnectionFactory (
//	                        redisStandaloneConfiguration,
//	                        clientConfiguration
//	        );
//	    }

	    @Bean
	    public RedisTemplate<String, T> redisTemplate() {
	        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
	        redisTemplate.setConnectionFactory(lettuceConnectionFactory());
	        redisTemplate.setKeySerializer(RedisSerializer.string());
	        redisTemplate.setValueSerializer(getJackson2JsonRedisSerializer());
	        redisTemplate.setHashKeySerializer(RedisSerializer.string());
	        redisTemplate.afterPropertiesSet();
	        return redisTemplate;
	    }

	    @Bean
	    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
	        return builder -> builder
	                .withCacheConfiguration("24HExp",
	                        RedisCacheConfiguration
	                                .defaultCacheConfig()
	                                .entryTtl(Duration.ofHours(24))
	                                .disableCachingNullValues()
	                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
	                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(getJackson2JsonRedisSerializer()))
	                )

	                .withCacheConfiguration("60sExp",
	                        RedisCacheConfiguration
	                                .defaultCacheConfig()
	                                .entryTtl(Duration.ofSeconds(60))
	                                .disableCachingNullValues()
	                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
	                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(getJackson2JsonRedisSerializer()))
	                )

	                .withCacheConfiguration("120sExp",
	                        RedisCacheConfiguration
	                                .defaultCacheConfig()
	                                .entryTtl(Duration.ofSeconds(120))
	                                .disableCachingNullValues()
	                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
	                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
	                );
	    }

	    @Bean
	    public Jackson2JsonRedisSerializer getJackson2JsonRedisSerializer() {
	        ObjectMapper objectMapper = new ObjectMapper();
	        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
	        objectMapper.registerModule(new JavaTimeModule());
	        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
	        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
	        return jackson2JsonRedisSerializer;
	    }
	
}
