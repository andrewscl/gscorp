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
                String physicalTargetDir,
                String webPrefixPath){
        if( file == null || file.isEmpty()) return null;

        try {
            // Obtener la extensión del archivo en forma segura 
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")){
                fileExtension = originalFilename
                                .substring(originalFilename.lastIndexOf('.'));
            }
            // Generar nombre unico con UUID
            String storedFilename = UUID.randomUUID().toString() + fileExtension;
            // Crear el directorio fisico en el servidor si es que no existe
            File dest = new File(physicalTargetDir);
            if(!dest.exists()) dest.mkdirs();
            // Escribir el archivo fisicamente en el disco
            File storedFile = new File(dest, storedFilename);
            file.transferTo(storedFile);
            // Retornar la ruta web
            return webPrefixPath + storedFilename;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al guardar el archivo" + e.getMessage());
        }
    }

}