package com.gscorp.dv1.shared;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    

    public String storeFile(
                MultipartFile file,
                String uploadFilesDir,
                String filePath
            ){
        if( file == null || file.isEmpty()) return null;

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")){
                fileExtension = originalFilename
                                .substring(originalFilename.lastIndexOf('.'));
            }
            String storedFilename = UUID.randomUUID().toString() + fileExtension;
            File dest = new File(uploadFilesDir, "photos");
            if(!dest.exists()) dest.mkdirs();
            File storedFile = new File(dest, storedFilename);
            file.transferTo(storedFile);
            String filePathResult = filePath + storedFilename;
            return filePathResult;
        } catch (IOException e) {
            throw new
                RuntimeException(
                    "Error al guardar el archivo" + e.getMessage());
        }

    }

}