package com.dimafeng.examples.transactional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@EnableAutoConfiguration
@EnableJpaRepositories
@ComponentScan
@EnableTransactionManagement
public class Application {

    @Bean(name = "count")
    public Integer count() {
        return 1;
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        UserService service = applicationContext.getBean(UserService.class);

        IntStream.range(0, 5).forEach(i -> System.out.println("\n"));

        service.cleanTable();

        IntStream.range(0, 1000).forEach(i -> {
            service.jdbc();
            service.cleanTable();

            service.entityManager();
            service.cleanTable();

            service.transactionManager();
            service.cleanTable();

            service.transactional();
            service.cleanTable();
        });

        Stream.of(1, 10, 100, 1_000, 10_000, 50_000).forEach(count -> {
            System.out.println("Count = " + count);

            System.out.println("JDBC");
            doIt(service, () -> IntStream.range(0, count).forEach(i -> service.jdbc()));

            System.out.println("Entity Manager");
            doIt(service, () -> IntStream.range(0, count).forEach(i -> service.entityManager()));

            System.out.println("Transaction Manager");
            doIt(service, () -> IntStream.range(0, count).forEach(i -> service.transactionManager()));

            System.out.println("Transactional");
            doIt(service, () -> IntStream.range(0, count).forEach(i -> service.transactional()));

            System.out.println();
        });

        System.exit(0);
    }

    static void doIt(UserService service, Runnable r) {
        try {
            CompletableFuture.runAsync(() -> {
                List<Long> data = IntStream.range(0, 20)
                        .mapToObj(i -> {
                            long val = System.currentTimeMillis();
                            r.run();
                            val = System.currentTimeMillis() - val;
                            service.cleanTable();
                            return val;
                        }).collect(Collectors.toList());

                System.out.println(data);
                System.out.println("Max: " + data.stream().collect(Collectors.maxBy(Long::compare)).get());
                System.out.println("Avg: " + data.stream().collect(Collectors.averagingLong(i -> i)));
                System.out.println("Min: " + data.stream().collect(Collectors.minBy(Long::compare)).get());
                System.out.println();
            }, Executors.newSingleThreadExecutor()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
