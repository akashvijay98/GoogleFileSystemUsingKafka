package com.example.gfsServer2.Service;

import com.example.gfsServer2.DAO.ChunkMessage;
import com.example.gfsServer2.Util.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
@Setter
class KafkaMessage {
    private ChunkMessage value;
    // Add other fields as needed

    // Getters and setters
}

@Service
public class Server2 {

    private static final String CREATE_TOPIC = "create-chunk";
    private static final String RESPONSE_TOPIC = "response-topic";


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @Autowired
    private ObjectMapper objectMapper;



    @KafkaListener(topicPartitions = @org.springframework.kafka.annotation.TopicPartition(topic = CREATE_TOPIC, partitions = {"1"}), groupId = "gfs-consumer")
    public void listen(ConsumerRecord<String, String> record,Acknowledgment ack) {
        ack.acknowledge();

        if (record != null) {
                processMessages(record.value());
                System.out.println("New message added to queue");
            }
    }


    public void processMessages(String jsonMessage) {

            //System.out.println("A message has been posted"+record.value());

            try {
                ChunkMessage chunkMessage  = objectMapper.readValue(jsonMessage, ChunkMessage.class);

                    System.out.println("chunk count: " + chunkMessage.getChunkCount());

                    if ("create".equals(chunkMessage.getCommand())) {
                        System.out.println("Create command");
                        handleCreateCommand(chunkMessage);
                    } else if ("read".equals(chunkMessage.getCommand())) {
                        handleReadCommand(chunkMessage);
                    } else {
                        System.out.println("Unknown command");
                    }


            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception (e.g., log it) based on your application's requirements
            }

    }

    private void handleCreateCommand(ChunkMessage chunkMessage) {
        // Implement the create logic
        String fileName = chunkMessage.getFileName();
        String extension = chunkMessage.getFileExtension();
        int chunkNo = chunkMessage.getChunkCount();
        byte[] data = chunkMessage.getData();

        System.out.println("Successfully received chunk " + chunkNo);

        String path = "/temp2/" + fileName + Integer.toString(chunkNo) + extension;
        File file = new File(path);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) {
            // Append data to file
            fileOutputStream.write(data);

            sendMessage("response-topic","success");
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage("response-topic","failed");
        }
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    private void handleReadCommand(ChunkMessage message) {
        // Implement the read logic if needed
        System.out.println("Read command not implemented for server1");
    }
}
