package com.cloud.gfs.service;

import com.cloud.gfs.DAO.FileDAO;
import com.cloud.gfs.Repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
public class FileService {

    // autowire
    FileRepository fileRepository;

    public FileService(FileRepository _fileRepository){
        fileRepository = _fileRepository;
    }

    public FileDAO addFile(FileDAO file){


       // add try catch


        //catch - throw new IllegalStateException("Could not save file")
        FileDAO fileResponse =  fileRepository.save(file);
        return fileResponse;

    }

    public FileDAO getFileById(UUID id){
        FileDAO fileResponse = fileRepository.getReferenceById(id);
        return fileResponse;
    }

}
