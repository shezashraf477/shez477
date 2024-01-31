package com.nasdaq.ktable.updater.producer.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Component
public class NasdaqPriceProcessor {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.application-id}")
    private String applicationId;

    private KafkaStreams kafkaStreams;

    @PostConstruct
    public void initializeKafkaStreams() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("application.id", applicationId);

        StreamsBuilder builder = new StreamsBuilder();

        // Specify key and value serdes
        Serde<String> stringSerde = Serdes.String();
        Serde<String> jsonSerde = Serdes.String();

        // Create a KTable with a state store named "nasdaq-store"
        KTable<String, String> nasdaqPrices = builder.table("mkt-data-topic", Consumed.with(stringSerde, jsonSerde));

        // Simulate Nasdaq prices and symbols
        String[] symbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};
        for (String symbol : symbols) {
            String price = generateRandomPrice();
            String marketData = "{\"symbol\": \"" + symbol + "\", \"price\": \"" + price + "\"}";

            // Store in the KTable
            nasdaqPrices.toStream().to("mkt-data-topic", Produced.with(stringSerde, jsonSerde));

            // Print the contents of the KTable (for demonstration purposes)
            nasdaqPrices.toStream().foreach((key, value) ->
                    System.out.println("KTable - Key: " + key + ", Value: " + value)
            );
        }

        this.kafkaStreams = new KafkaStreams(builder.build(), props);

        // Set up a latch to wait for the Kafka Streams application to become in a running state
        CountDownLatch latch = new CountDownLatch(1);
        this.kafkaStreams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING) {
                latch.countDown();
            }
        });

        // Start the Kafka Streams application
        this.kafkaStreams.start();

        try {
            // Wait for the Kafka Streams application to become in a running state
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void closeKafkaStreams() {
        // Shutdown hook to close the stream gracefully
        if (this.kafkaStreams != null) {
            this.kafkaStreams.close();
        }
    }

    private String generateRandomPrice() {
        // Simulate a random price for demonstration purposes
        return String.valueOf(Math.round(Math.random() * 1000) / 100.0); // Random price between 0 and 10
    }
}

