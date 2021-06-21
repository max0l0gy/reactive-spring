package ru.maxology.reactive.spring.experimental;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

class MapsTest {

    @Test
    void flatMap() {
        Flux<Integer> data = Flux
                .just(new Pair(1, 300), new Pair(2, 200), new Pair(3, 100))
                .flatMap(id -> this.delayReplyFor(id.id, id.delay));
        StepVerifier
                .create(data)
                .expectNext(3, 2, 1)
                .verifyComplete();
    }

    @Test
    void concatMap() {
        Flux<Integer> data = Flux.just(new Pair(1, 300), new Pair(2, 200), new Pair(3, 100))
                .concatMap(id -> this.delayReplyFor(id.id, id.delay));
        StepVerifier.create(data)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    private Flux<Integer> delayReplyFor(Integer i, long delay) {
        return Flux
                .just(i)
                .delayElements(Duration.ofMillis(delay));
    }

    @AllArgsConstructor
    static class Pair {
        private int id;
        private long delay;
    }

    @Test
    void switchMapWithLookaheads() {
        Flux<Integer> data = Flux.just(new Pair(1, 300), new Pair(2, 200), new Pair(3, 100))
                .switchMap(id -> this.delayReplyFor(id.id, id.delay));
        StepVerifier.create(data)
                .expectNext(3)
                .verifyComplete();
    }

}
