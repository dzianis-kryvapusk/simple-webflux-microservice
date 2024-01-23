package com.example.jokemicroservice.controller;

import java.time.LocalDateTime;

import com.example.jokemicroservice.dto.JokeDto;
import com.example.jokemicroservice.jokeprovider.JokeProvider;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class JokeController {
    private final JokeProvider jokeProvider;

    @GetMapping("/joke")
    public Mono<JokeDto> getJoke() {
        return jokeProvider.getJoke();
    }

    @GetMapping("/jokes")
    public Flux<JokeDto> getJokes(
            @RequestParam(value = "count", defaultValue = "5")
            @Min(value = 0, message = "Count value must be integer between 1 and 100")
            @Max(value = 100, message = "No more than 100 jokes can be retrieved at once")
            int count
    ) {
        return jokeProvider.getJokes(count)
                .onErrorStop()
                .doOnError(ex -> log.error(ex.getMessage(), ex));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleException(ValidationException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                              .time(LocalDateTime.now())
                              .reason(ex.getMessage())
                              .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.builder()
                              .time(LocalDateTime.now())
                              .reason(ex.getMessage())
                              .build());
    }

    @Data
    @Builder
    public static final class ErrorResponse {
        private LocalDateTime time;
        private String reason;
    }

}
