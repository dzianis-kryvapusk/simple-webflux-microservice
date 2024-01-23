package com.example.jokemicroservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JokeDto {
    private long id;
    private String type;
    private String setup;
    private String punchline;
}
