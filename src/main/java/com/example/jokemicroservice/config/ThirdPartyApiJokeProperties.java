package com.example.jokemicroservice.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "third-party-api.joke")
public class ThirdPartyApiJokeProperties {
    private String baseUrl;
    private String uri;
    /**
     * Timeout in seconds.
     */
    private int timeout;
}
