package com.example.gfsServer2.DAO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class ChunkMessage implements Serializable {
    private String command;
    private String fileName;
    private int chunkCount;
    private String fileExtension;

    private int fileSize;
    private byte[] data;

    // Full constructor
    public ChunkMessage(String command, String fileName, int chunkCount, String fileExtension, int fileSize, byte[] data) {
        this.command = command;
        this.fileName = fileName;
        this.chunkCount = chunkCount;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.data = data;
    }

    // Constructor with required fields
    public ChunkMessage(String command, String fileName, int chunkCount) {
        this(command, fileName, 0, null, 0, null); // Using null or default values for optional fields
    }

}


