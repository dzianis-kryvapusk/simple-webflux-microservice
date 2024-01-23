package com.example.jokemicroservice.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.example.jokemicroservice.config.ThirdPartyApiJokeProperties;
import com.example.jokemicroservice.dto.JokeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpStatusCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@AutoConfigureWebTestClient
class JokeControllerTest {
    private static MockServerClient mockServer;
    @Autowired
    private WebTestClient client;
    @Autowired
    private ThirdPartyApiJokeProperties jokeApiProperties;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void startServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterEach
    void resetServer() {
        mockServer.reset();
    }

    @AfterAll
    static void stopServer() {
        mockServer.stop();
    }

    @Test
    void test_unaryController() throws Exception {
        JokeDto joke = buildJoke(10);
        mockServer.when(request()
                                .withMethod(HttpMethod.GET.name())
                                .withPath(jokeApiProperties.getUri()),
                        once())
                .respond(response()
                                 .withStatusCode(HttpStatusCode.OK_200.code())
                                 .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                 .withBody(objectMapper.writeValueAsString(joke)));

        JokeDto response =  client.get()
                .uri("/joke")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JokeDto.class).returnResult().getResponseBody();
        assertEquals(joke, response);
    }

    @Test
    void test_multiValueController() throws Exception {
        List<JokeDto> expected = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            JokeDto joke = buildJoke(i);
            expected.add(joke);
            //TODO fix mock server returning only the first joke
            mockServer.when(request()
                                    .withMethod(HttpMethod.GET.name())
                                    .withPath(jokeApiProperties.getUri()),
                            once())
                    .respond(response()
                                     .withStatusCode(HttpStatusCode.OK_200.code())
                                     .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                     .withBody(objectMapper.writeValueAsString(joke)));
        }
        List<JokeDto> response = client.get()
                .uri("/jokes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<JokeDto>>() {}).returnResult().getResponseBody();
        assertNotNull(response);
        assertEquals(expected.size(), response.size());
        response.sort(Comparator.comparing(JokeDto::getId));
        assertEquals(expected, response);
    }

    @Test
    void test_tooManyJokesRequested() {
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/jokes")
                        .queryParam("count", "101")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private JokeDto buildJoke(int id) {
        JokeDto joke = new JokeDto();
        joke.setId(id);
        joke.setSetup("test setup " + id);
        joke.setPunchline("test punchline " + id);
        return joke;
    }

}