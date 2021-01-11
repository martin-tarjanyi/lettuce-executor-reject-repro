package org.example;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class App {
    private static final String REDIS_KEY = "some_key";
    private static final Duration COMMAND_TIMEOUT = Duration.ofMillis(8);

    public static void main(String[] args) {
        ReactiveRedisTemplate<String, String> template = createTemplate();

        // prepare a value in cache
        template.opsForValue()
                .set(REDIS_KEY, "some_value")
                .block();

        // stress
        Flux.range(1, 5_000_000)
                .doOnNext(e -> {
                    if (e % 1000 == 0) {
                        System.out.println(e); // diagnostic log
                    }
                })
                .flatMap(a -> getFromRedis(template), 1000)
                .blockLast();
    }

    private static Mono<String> getFromRedis(ReactiveRedisTemplate<String, String> template) {
        return template.opsForValue()
                .get(REDIS_KEY)
                .doOnError(e -> e.getClass() != QueryTimeoutException.class && e.getClass() != RedisSystemException.class,
                        Throwable::printStackTrace) // not interested in regular timeout logs
                .onErrorResume(e -> Mono.empty());
    }

    private static ReactiveRedisTemplate<String, String> createTemplate() {
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(500))
                .build();

        ClientOptions clientOptions =
                ClientOptions.builder()
                        .timeoutOptions(TimeoutOptions.enabled(COMMAND_TIMEOUT))
                        .socketOptions(socketOptions)
                        .publishOnScheduler(true)
                        .build();

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .build();

        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration("localhost",
                6379);

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfiguration,
                clientConfiguration);
        connectionFactory.afterPropertiesSet();

        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext.<String, String>newSerializationContext()
                .key(RedisSerializer.string())
                .hashKey(RedisSerializer.string())
                .value(RedisSerializer.string())
                .hashValue(RedisSerializer.string())
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
