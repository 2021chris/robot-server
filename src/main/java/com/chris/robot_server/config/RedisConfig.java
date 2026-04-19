package com.chris.robot_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis配置<br>
 * 集群下启动session共享，需打开@EnableRedisHttpSession<br>
 * 单机下不需要
 *
 * @author chris
 *
 */
//@EnableRedisHttpSession
@Configuration
public class RedisConfig {

	@Bean("redisTemplate")
	public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String,String>  redis = new RedisTemplate<>();

		// 使用StringRedisSerializer来序列化和反序列化键
		redis.setKeySerializer(new StringRedisSerializer());
		redis.setHashKeySerializer(new StringRedisSerializer());

		// 使用GenericJackson2JsonRedisSerializer来序列化和反序列化值
		redis.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redis.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		// 设置连接工厂
		redis.setConnectionFactory(connectionFactory);

		return redis;
	}


	@Bean("redisObjTemplate")
    public RedisTemplate<String, Object> redisObjTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // key 也用字符串序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(factory);
        return template;
    }
}
