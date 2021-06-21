package ru.maxology.reactive.spring.experimental;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class MapTest {
    @Test
    void maps() {
        var data = Flux.just(" a ", " b ", " c ")
                .map(String::toUpperCase);
        StepVerifier.create(data)
                .expectNext(" A ", " B ", " C ")
                .verifyComplete();
    }
}
