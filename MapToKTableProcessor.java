package com.nasdaq.ktable.updater.producer.config;


import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class MapToKTableProcessor {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.application-id}")
    private String applicationId;

    @Value("${kafka.output-topic}")
    private String outputTopic;

    private KafkaStreams kafkaStreams;

    @PostConstruct
    public void initializeKafkaStreams() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("application.id", applicationId);

        StreamsBuilder builder = new StreamsBuilder();

        // Simulate reading values from a Map
        Map<String, String> inputMap = createInputMap();

        // Create a KTable from the input values
        KTable<String, String> kTable = builder.table(
                "my-source-topic",  // Specify a placeholder topic name since it's required by the API
                Materialized.as("my-state-store")
        );

        // Send the KTable to the output topic
        kTable.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        this.kafkaStreams = new KafkaStreams(builder.build(), props);

        // Start the Kafka Streams application
        this.kafkaStreams.start();
    }

    @PreDestroy
    public void closeKafkaStreams() {
        // Shutdown hook to close the stream gracefully
        if (this.kafkaStreams != null) {
            this.kafkaStreams.close();
        }
    }

    private Map<String, String> createInputMap() {
        // Simulate creating a Map of input values
        // In a real scenario, you would read values from your data source
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("key1", "value1");
        inputMap.put("key2", "value2");
        inputMap.put("key3", "value3");
        return inputMap;
    }
}
