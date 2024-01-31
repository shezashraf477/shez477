package com.nasdaq.ktable.updater.producer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

//@Configuration
public class NasdaqPriceProducerConfig {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Bean
    public CommandLineRunner dataPublisher() {
        return args -> {
            // Simulate Nasdaq prices and symbols
            String[] symbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};
            for (String symbol : symbols) {
                String price = generateRandomPrice();
                String marketData = "{\"symbol\": \"" + symbol + "\", \"price\": \"" + price + "\"}";

                // Publish to "nasdaq-prices" topic
                kafkaTemplate.send("nasdaq-prices", symbol, marketData);
            }
        };
    }

    private String generateRandomPrice() {
        // Simulate a random price for demonstration purposes
        return String.valueOf(Math.round(Math.random() * 1000) / 100.0); // Random price between 0 and 10
    }
}
