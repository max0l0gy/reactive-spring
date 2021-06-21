package ru.maxology.reactive.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Slf4j
@Component
@RequiredArgsConstructor
class SimpleDataInitializer {
    private final ReservationRepository reservationRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void ready() {
        Flux<Reservation> reservations = Flux.just("Josh", "Sarah", "Conor", "Max", "Victoria", "Olga")
                .map(name -> new Reservation(null, name))
                .flatMap(reservationRepository::save);
        //...
        this.reservationRepository.deleteAll()
                .thenMany(reservations)
                .thenMany(this.reservationRepository.findAll())
                .subscribe(System.out::println);
    }
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {
    Flux<Reservation> findByName(String name);
}


@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
class Reservation {
    @Id
    private String id;
    private String name;

}