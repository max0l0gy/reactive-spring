package ru.maxology.reactive.demo;

import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;

@Slf4j
@Configuration
@SpringBootApplication
@EnableR2dbcRepositories
@EnableTransactionManagement
public class DemoApplication {

    @Bean
    ReactiveTransactionManager r2dbcTransactionManager(ConnectionFactory cf) {
        return new R2dbcTransactionManager(cf);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Service
@Transactional
@RequiredArgsConstructor
class ReservationService {
    private final ReservationRepository reservationRepository;

    @Transactional
    public Flux<Reservation> saveAll(String... names) {
        return Flux.just(names)
                .map(name -> new Reservation(null, name))
                .flatMap(reservationRepository::save)
                .doOnNext(this::assertValidReservation);
    }

    private void assertValidReservation(Reservation r) {
        Assert.isTrue(r.getName() != null && r.getName().length() > 0
                        && Character.isUpperCase(r.getName().charAt(0)),
                "the name must start with a capital character");
    }

}

@Slf4j
@Component
@RequiredArgsConstructor
class SimpleDataInitializer {
    private final ReservationRepository reservationRepository;
    private final DatabaseClient databaseClient;
    private final ReservationService rs;

    private Mono<String> getSchema() throws URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("schema.sql").toURI());
        return Flux
                .using(() -> Files.lines(path), Flux::fromStream, BaseStream::close)
                .reduce((line1, line2) -> line1 + "\n" + line2);
    }

    private Mono<Integer> executeSql(DatabaseClient client, String sql) {
        return client.execute(sql).fetch().rowsUpdated();
    }


    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void ready() {
        getSchema()
                .flatMap(sql -> executeSql(databaseClient, sql))
                .subscribe(count -> log.info("Schema created"));

        Flux<Reservation> reservations = rs.saveAll("Josh", "Sarah", "Conor", "Max", "Victoria", "Olga");
        //...
        this.reservationRepository.deleteAll()
                .thenMany(reservations)
                .thenMany(this.reservationRepository.findAll())
                .subscribe(reservation -> log.info("saved {}", reservation));
    }

}

@Repository
interface ReservationRepository extends ReactiveCrudRepository<Reservation, Long> {
    Flux<Reservation> findByName(String name);
}

@Data
@Table("reservation")
@NoArgsConstructor
@AllArgsConstructor
class Reservation {
    @Id
    private Long id;
    @Column("name")
    private String name;

}

