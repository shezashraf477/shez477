package com.nasdaq.ktable.updater.producer.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Component
public class JsonToKTableProcessor {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.application-id}")
    private String applicationId;

    @Value("${kafka.input-topic}")
    private String inputTopic;

    @Value("${kafka.output-topic}")
    private String outputTopic;

    private KafkaStreams kafkaStreams;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Your custom hash map for key lookup
    private final Map<String, String> keyLookupMap = new HashMap<>();

    @PostConstruct
    public void initializeKafkaStreams() {
        // Populate the key lookup map with sample data
        keyLookupMap.put("key1", "value1");
        keyLookupMap.put("key2", "value2");
        keyLookupMap.put("key3", "value3");

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("application.id", applicationId);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        StreamsBuilder builder = new StreamsBuilder();
        Serde<String> stringSerde = Serdes.String();
        Serde<String> jsonSerde = Serdes.String();

        // Create a KTable from the input topic, extracting a specific field as the key
        KTable<String, String> kTable = builder.table(
                inputTopic,
                Consumed.with(stringSerde, jsonSerde),
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("my-state-store")
                        .withKeySerde(stringSerde)  // Explicitly set key serde
                        .withValueSerde(jsonSerde)
        );

        // Convert KTable to KStream
        KStream<String, String> kStream = kTable.toStream();

        // Transform the KStream using the extracted key
        KStream<String, String> transformedStream = kStream.map(
                (key, value) -> {
                    String extractedKey = extractKeyFromJson(value);
                    String lookupValue = keyLookupMap.getOrDefault(extractedKey, "defaultLookupValue");
                    return KeyValue.pair(extractedKey, lookupValue);
                }
        );

        // Convert KStream back to KTable
        KTable<String, String> transformedTable = transformedStream.groupByKey()
                .reduce((value1, value2) -> value2);

        // Send the transformed KTable to the output topic
        transformedTable.toStream().to(outputTopic, Produced.with(stringSerde, jsonSerde));

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

    // Extract a specific field from JSON using Jackson with Optional for conciseness
    private String extractKeyFromJson(String jsonString) {
        try {
            return Optional.ofNullable(objectMapper.readTree(jsonString))
                    .map(jsonNode -> jsonNode.get("fieldName"))
                    .map(JsonNode::asText)
                    .orElse("");
        } catch (IOException e) {
            e.printStackTrace();
            // Handle JSON parsing exception
            return "";
        }
    }
}
