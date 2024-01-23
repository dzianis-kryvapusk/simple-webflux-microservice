package com.example.jokemicroservice.jokeprovider;

import com.example.jokemicroservice.dto.JokeDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JokeProvider {
    Mono<JokeDto> getJoke();
    Flux<JokeDto> getJokes(int number);
}
