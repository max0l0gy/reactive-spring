package ru.maxology.reactive.spring.experimental;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class AsyncApiIntegrationTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Test
    void async() {
        //①
        Flux<Integer> integers = Flux.create(emitter -> this.launch(emitter, 5));
        //②
        StepVerifier
                .create(integers.doFinally(signalType -> this.executorService.shutdown()))
                .expectNextCount(5)
                .verifyComplete();
    }

    //③
    private void launch(FluxSink<Integer> integerFluxSink, int count) {
        this.executorService.submit(() -> {
            var integer = new AtomicInteger();
            Assertions.assertNotNull(integerFluxSink);
            while (integer.get() < count) {
                double random = Math.random();
                integerFluxSink.next(integer.incrementAndGet());
                //④
                log.info("integer {}", integer.get());
                log.info("Random sleep milliseconds {}", random);
                this.sleep((long) (random * 1_000));
            }
            integerFluxSink.complete();
            //⑤
        });
    }

    @SneakyThrows
    private void sleep(long milliseconds) {
        TimeUnit.MILLISECONDS.sleep(milliseconds);
    }

}
