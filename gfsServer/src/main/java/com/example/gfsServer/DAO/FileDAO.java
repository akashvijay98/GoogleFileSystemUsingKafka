package com.cloud.gfs.DAO;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.UUID;

@Entity
@Table(name = "File")

@Getter
@Setter
public class FileDAO {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id")
    private UUID id;
    private String name;
    private Integer size;
    String status;

    public FileDAO(){}
    public FileDAO(UUID id, String name, Integer size, String status){
        this.id = id;
        this.name = name;
        this.size = size;
        this.status = status;
    }

    public FileDAO(String fileName, int fileSize, String status) {
        this.name = fileName;
        this.size = fileSize;
        this.status = status;
    }
}