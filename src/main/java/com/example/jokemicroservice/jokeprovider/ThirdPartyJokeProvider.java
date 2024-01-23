package com.example.jokemicroservice.jokeprovider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.jokemicroservice.config.ThirdPartyApiJokeProperties;
import com.example.jokemicroservice.dto.JokeDto;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ThirdPartyJokeProvider implements JokeProvider {
    private final ThirdPartyApiJokeProperties jokeApiProperties;
    private WebClient webClient;

    @PostConstruct
    private void initWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * jokeApiProperties.getTimeout())
                .responseTimeout(Duration.ofSeconds(jokeApiProperties.getTimeout()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(jokeApiProperties.getTimeout(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(jokeApiProperties.getTimeout(), TimeUnit.SECONDS)));

        webClient = WebClient.builder()
                .baseUrl(jokeApiProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public Flux<JokeDto> getJokes(int number) {
        List<Mono<JokeDto>> receivers = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            receivers.add(getJoke());
        }
        return Flux.merge(receivers);
    }

    @Override
    public Mono<JokeDto> getJoke() {
        return webClient.get().uri(jokeApiProperties.getUri())
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(JokeDto.class);
                    }
                    return response.createException().flatMap(Mono::error);
                });
    }
}
