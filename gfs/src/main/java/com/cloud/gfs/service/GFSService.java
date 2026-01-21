package com.cloud.gfs.service;

import com.cloud.gfs.util.KafkaConfig;
import com.cloud.gfs.DAO.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.io.InputStream;

@Service
@Slf4j
public class GFSService {

    private static final String CREATE_TOPIC = "create-chunk";
    private static final String READ_TOPIC = "read-chunk";
    private static final String RESPONSE_TOPIC = "response-topic";

    private final ObjectMapper objectMapper;

    private KafkaProducer<String, String> producer;

    @Autowired
    public GFSService(KafkaConfig kafkaConfig) {
        this.producer = kafkaConfig.createProducer();
        
        // Log the Kafka configuration for debugging
        log.info("GFS Service initialized with Kafka configuration:");
        log.info("Bootstrap servers: {}", kafkaConfig.getClass().getSimpleName());
        log.info("Partition strategy: {}", partitionStrategy);
        log.info("Number of partitions: {}", numberOfPartitions);
    }

    @Value("${app.gfs.temp.directory}")
    private String TEMP_DIRECTORY;

    @Autowired
    private MetaService metaService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ChunkService chunkService;

    @Value("${app.gfs.servers}")
    String[] servers;

    @Value("${app.gfs.ports}")
    Integer[] ports;

    @Value("${app.gfs.chunk-size}")
    private int maxChunkSize; // the maximum chunk size is 4Kb in size

    @Value("${app.gfs.partition.strategy:ROUND_ROBIN}")
    private String partitionStrategy;

    @Value("${app.gfs.kafka.partitions:3}")
    private int numberOfPartitions;

    /**
     * Generate partition key based on the configured strategy
     */
    private String generatePartitionKey(String fileName, int chunkCount, int fileSize) {
        switch (partitionStrategy.toUpperCase()) {
            case "ROUND_ROBIN":
                return String.valueOf(chunkCount % numberOfPartitions);
            case "FILE_NAME_HASH":
                return String.valueOf(Math.abs(fileName.hashCode() % numberOfPartitions));
            case "CHUNK_COUNT":
                return String.valueOf(chunkCount);
            case "FILE_SIZE_BASED":
                return String.valueOf((fileSize / maxChunkSize) % numberOfPartitions);
            case "COMBINED_HASH":
                String combined = fileName + "_" + chunkCount + "_" + fileSize;
                return String.valueOf(Math.abs(combined.hashCode() % numberOfPartitions));
            default:
                return String.valueOf(chunkCount % numberOfPartitions);
        }
    }

    /**
     * Send chunk to Kafka topic with partition key
     */
    private void sendChunkToKafka(ChunkMessage chunkMessage, String partitionKey) {
        try {
            
            String jsonMessage = mapper.writeValueAsString(chunkMessage);
            
            // Create Kafka producer record with partition key
            ProducerRecord<String, String> record = new ProducerRecord<>(
                CREATE_TOPIC, 
                partitionKey, 
                jsonMessage
            );
            
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to send chunk {} for file {} to partition {}", 
                        chunkMessage.getChunkCount(), 
                        chunkMessage.getFileName(), 
                        metadata != null ? metadata.partition() : "unknown", 
                        exception);
                } else {
                    log.info("Successfully sent chunk {} for file {} to partition {} at offset {}", 
                        chunkMessage.getChunkCount(), 
                        chunkMessage.getFileName(), 
                        metadata.partition(), 
                        metadata.offset());
                }
            });
            
            log.debug("Sent chunk {} for file {} with partition key: {}", 
                chunkMessage.getChunkCount(), 
                chunkMessage.getFileName(), 
                partitionKey);
                
        } catch (Exception e) {
            log.error("Error serializing or sending chunk message", e);
            throw new RuntimeException("Failed to send chunk to Kafka", e);
        }
    }

    /**
     * Send chunk to Kafka topic with custom partition key
     * This method allows external control over partition key generation
     */
    public void sendChunkWithCustomPartitionKey(ChunkMessage chunkMessage, String customPartitionKey) {
        sendChunkToKafka(chunkMessage, customPartitionKey);
    }

    /**
     * Send chunk to Kafka topic with specific partition number
     * This method allows direct partition assignment
     */
    public void sendChunkToSpecificPartition(ChunkMessage chunkMessage, Integer partitionNumber) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(chunkMessage);
            
            // Create Kafka producer record with specific partition
            ProducerRecord<String, String> record = new ProducerRecord<>(
                CREATE_TOPIC, 
                partitionNumber, 
                null, 
                jsonMessage
            );
            
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to send chunk {} for file {} to partition {}", 
                        chunkMessage.getChunkCount(), 
                        chunkMessage.getFileName(), 
                        partitionNumber, 
                        exception);
                } else {
                    log.info("Successfully sent chunk {} for file {} to partition {} at offset {}", 
                        chunkMessage.getChunkCount(), 
                        chunkMessage.getFileName(), 
                        metadata.partition(), 
                        metadata.offset());
                }
            });
            
            log.debug("Sent chunk {} for file {} to specific partition: {}", 
                chunkMessage.getChunkCount(), 
                chunkMessage.getFileName(), 
                partitionNumber);
                
        } catch (Exception e) {
            log.error("Error serializing or sending chunk message", e);
            throw new RuntimeException("Failed to send chunk to Kafka", e);
        }
    }

    /**
     * Send chunk using custom partitioner
     * This method uses a custom partitioner for advanced partition key strategies
     */
    public void sendChunkWithCustomPartitioner(ChunkMessage chunkMessage, String partitionKey, KafkaConfig kafkaConfig) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(chunkMessage);
            
            // Create producer with custom partitioner
            KafkaProducer<String, String> customProducer = kafkaConfig.createProducerWithCustomPartitioner(
                "Util.CustomPartitioner"
            );
            
            // Create Kafka producer record with partition key
            ProducerRecord<String, String> record = new ProducerRecord<>(
                CREATE_TOPIC, 
                partitionKey, 
                jsonMessage
            );
            
            customProducer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to send chunk {} for file {} with custom partitioner", 
                        chunkMessage.getChunkCount(), 
                        chunkMessage.getFileName(), 
                        exception);
                } else {
                    log.info("Successfully sent chunk {} for file {} to partition {} at offset {} using custom partitioner", 
                        chunkMessage.getChunkCount(), 
                        chunkMessage.getFileName(), 
                        metadata.partition(), 
                        metadata.offset());
                }
            });
            
            log.debug("Sent chunk {} for file {} with custom partitioner using key: {}", 
                chunkMessage.getChunkCount(), 
                chunkMessage.getFileName(), 
                partitionKey);
                
            // Close the custom producer
            customProducer.close();
                
        } catch (Exception e) {
            log.error("Error serializing or sending chunk message with custom partitioner", e);
            throw new RuntimeException("Failed to send chunk to Kafka with custom partitioner", e);
        }
    }

    public String uploadFile(org.springframework.web.multipart.MultipartFile file, String fileName, String fileExtension, Integer fileSize) throws IOException {
    String generatedFileId = null;
        Socket socket0;

        List<File> list = new ArrayList<>(); // check

        try (InputStream in = file.getInputStream()) {

            int serverNumber= 0;
            int chunkCount = 1;
            byte[] buffer = new byte[maxChunkSize];
            int dataRead;
            String status = "UPLOADING";

            FileDAO fileDAO = new FileDAO(fileName, fileSize, status); // check

            // save file info in file table
            FileDAO fileResponse = saveFileDetails(fileDAO);
            if (fileResponse != null) {
                generatedFileId = String.valueOf(fileResponse.getId());
            }

            while ((dataRead=in.read(buffer)) != -1 ) {
                ChunkMessage chunkMessage = new ChunkMessage();
                chunkMessage.setCommand("create");
                chunkMessage.setFileName(fileName);
                chunkMessage.setFileExtension(fileExtension);
                chunkMessage.setChunkCount(chunkCount);
                chunkMessage.setFileSize(fileSize);
                chunkMessage.setData(Arrays.copyOf(buffer, dataRead));

                // Generate partition key based on strategy
                String partitionKey = generatePartitionKey(fileName, chunkCount, fileSize);
                
                // Send chunk to Kafka with partition key
                sendChunkToKafka(chunkMessage, partitionKey);
                
                System.out.println("Chunk count "+chunkCount + " sent with partition key: " + partitionKey);
                String chunkName = fileName + Integer.toString(chunkCount);
                int chunkSize = buffer.length;

                // Creating chunk dto object
//                ChunkDAO chunk = new ChunkDAO(chunkName, chunkSize, chunkCount);
//                ChunkDAO chunkResponse =  chunkService.addChunk(chunk);
//
//                FileMetaDataDAO fileMetaData = new FileMetaDataDAO(fileResponse.getId(), chunkResponse.getId(), chunkCount, servers[serverNumber], ports[serverNumber]);
//                metaService.addFileMetaData(fileMetaData);
//                // Method updates file metadata
//                saveFileMetaDataDetails(fileMetaData);

                //increment the chunkCount after the chunk gets successfuly stored in the server
                chunkCount++;

                //new FileMetaDataDAO(fileResponse.getId(), chunkResponse.getId(), chunkCount, servers[serverNumber], ports[serverNumber]);

            }
            fileService.updateFileStatus(fileResponse.getId(), "UPLOADED");
        }
        catch (Exception e){
            log.error("there is an exception", e.getStackTrace());
            e.printStackTrace();
        }
        return "FileId"+generatedFileId;
    }







    public ChunkDAO saveChunkInfo(ChunkDAO chunk){
    // save chunk info on chunk table
    ChunkDAO chunkResponse =  chunkService.addChunk(chunk);

    return chunkResponse;

}
public FileDAO saveFileDetails(FileDAO file){
        // save file info
        FileDAO fileResponse =  fileService.addFile(file);
         return fileResponse;

}

public void saveFileMetaDataDetails(FileMetaDataDAO fileMetaData) {
    // save fileMetaData info
    // updating the fileMetaData table
    metaService.addFileMetaData(fileMetaData);
}








public void getFile(UUID fileId, String fileName, String fileExtension) throws IOException
    {
        Socket socket0;

        List<FileMetaDataDAO> fileMetaDataList = metaService.fileMetaDataRepository.findByFileId(fileId);

        int[] chunkIndexes = fileMetaDataList.stream().mapToInt(FileMetaDataDAO :: getChunkIndex).sorted().toArray();

        OutputStream outToServer;
        InputStream inFromServer;

        DataOutputStream writeToServer;
        DataInputStream readFromServer;

        String path = "./media";

        File outputFile = File.createTempFile(fileName,fileExtension, new File(path));
        FileOutputStream fos = new FileOutputStream(outputFile);

        for(int chunkIndex : chunkIndexes){

            try {
                String serverIp = fileMetaDataList.stream().filter(data -> data.getChunkIndex() == chunkIndex).findFirst().map(FileMetaDataDAO :: getServerId).orElse("Default Property");
                Integer port = fileMetaDataList.stream().filter(data -> data.getChunkIndex() == chunkIndex).findFirst().map(FileMetaDataDAO :: getPort).orElse(0);
                socket0 = new Socket(serverIp, port);
            }

            catch (IOException e) {
                throw new RuntimeException(e);
            }

            outToServer = socket0.getOutputStream();
            inFromServer = socket0.getInputStream();

            writeToServer =  new DataOutputStream(outToServer);
            readFromServer = new DataInputStream(inFromServer);

            String chunkName = fileName + Integer.toString(chunkIndex) + fileExtension;

            String message = "";
            message+= "read";
            message+=",";
            message+= chunkName;

            int bytes;

            byte[] buffer = new byte[8192];
            int totalBytesRead = 0;

            writeToServer.writeUTF(message);

            while( totalBytesRead<maxChunkSize &&(bytes = readFromServer.read(buffer,totalBytesRead,maxChunkSize-totalBytesRead))!=-1) {

                log.info("bytes=", bytes);
                fos.write(buffer, totalBytesRead, bytes);
                totalBytesRead += bytes;

            }
        }
        log.info("successfully uploaded");
    }
}
