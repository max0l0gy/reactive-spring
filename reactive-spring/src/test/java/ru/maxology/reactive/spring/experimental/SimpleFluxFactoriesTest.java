package ru.maxology.reactive.spring.experimental;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

class SimpleFluxFactoriesTest {

    @Test
    void simple() {
        //①
        Publisher<Integer> rangeOfIntegers = Flux.range(0, 10);
        StepVerifier.create(rangeOfIntegers).expectNextCount(10).verifyComplete();
        //②
        Flux<String> letters = Flux.just(" A ", " B ", " C ");
        StepVerifier.create(letters).expectNext(" A ", " B ", " C ").verifyComplete();
        //③
        long now = System.currentTimeMillis();
        Mono<Date> greetingMono = Mono.just(new Date(now));
        StepVerifier.create(greetingMono).expectNext(new Date(now)).verifyComplete();
        //④
        Mono<Object> empty = Mono.empty();
        StepVerifier.create(empty).verifyComplete();
        //⑤
        Flux<Integer> fromArray = Flux.fromArray(new Integer[]{1, 2, 3});
        StepVerifier.create(fromArray).expectNext(1, 2, 3).verifyComplete();
        //⑥
        Flux<Integer> fromIterable = Flux.fromIterable(Arrays.asList(1, 2, 3));
        StepVerifier.create(fromIterable).expectNext(1, 2, 3).verifyComplete();
        AtomicInteger integer = new AtomicInteger();
        Supplier<Integer> supplier = integer::incrementAndGet;
        Flux<Integer> integerFlux = Flux.fromStream(Stream.generate(supplier));
        StepVerifier.create(integerFlux.take(3)).expectNext(1).expectNext(2).expectNext(3).verifyComplete();
    }

}
