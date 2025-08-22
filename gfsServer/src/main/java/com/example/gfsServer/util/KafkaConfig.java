package com.example.gfsServer.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:kafka:9092}")
    private String bootstrapServers;

    @Value("${app.gfs.kafka.producer.acks:all}")
    private String acks;

    @Value("${app.gfs.kafka.producer.retries:3}")
    private int retries;

    @Value("${app.gfs.kafka.producer.batch.size:16384}")
    private int batchSize;

    @Value("${app.gfs.kafka.producer.linger.ms:1}")
    private int lingerMs;

    @Value("${app.gfs.kafka.producer.buffer.memory:33554432}")
    private long bufferMemory;

    @Bean
    public KafkaProducer<String, String> createProducer() {
        Properties producerProps = new Properties();
        
        // Basic configuration
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Enhanced configuration for better partition key handling
        producerProps.put(ProducerConfig.ACKS_CONFIG, acks);
        producerProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        
        // Partition key specific configurations
        producerProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.internals.DefaultPartitioner");
        
        // Compression for better performance
        producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Idempotence for exactly-once semantics
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        
        return new KafkaProducer<>(producerProps);
    }

    /**
     * Create a producer with custom partitioner for specific partition key strategies
     */
    public KafkaProducer<String, String> createProducerWithCustomPartitioner(String partitionerClass) {
        Properties producerProps = new Properties();
        
        // Basic configuration
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Enhanced configuration
        producerProps.put(ProducerConfig.ACKS_CONFIG, acks);
        producerProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        
        // Custom partitioner
        if (partitionerClass != null && !partitionerClass.isEmpty()) {
            producerProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, partitionerClass);
        }
        
        // Compression and idempotence
        producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        
        return new KafkaProducer<>(producerProps);
    }
}