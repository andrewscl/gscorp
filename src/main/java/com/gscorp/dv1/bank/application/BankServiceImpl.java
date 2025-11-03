package com.gscorp.dv1.bank.application;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.bank.infrastructure.Bank;
import com.gscorp.dv1.bank.infrastructure.BankRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService{

    private final BankRepository bankRepository;

    @Value("${file.administration_files-dir}")
    private String administrationFilesDir;

    @Override
    @Transactional(readOnly = true)
    public Bank findById(Long id) {
        return bankRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Bank> findAll() {
        return new HashSet<>(bankRepository.findAll());
    }

    @Override
    @Transactional
    public Bank saveBank(Bank bank) {
        return bankRepository.save(bank);
    }

    @Override
    @Transactional
    public String storeBankLogo(MultipartFile logoFile) {
        if(logoFile == null || logoFile.isEmpty()) {
            return null;
        }

        try {
            //Generar el nombre del archivo
            String originalFilename = logoFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String storedFilename = UUID.randomUUID().toString() + fileExtension;
            //Directorio fisico donde guardar el archivo
            File dest = new File(administrationFilesDir , "bank_logos"); 
            if (!dest.exists()) dest.mkdirs();
            File storedFile = new File(dest, storedFilename);
            logoFile.transferTo(storedFile);

            return "/files/administration_files/bank_logos/" + storedFilename;

        } catch (Exception e) {
            throw new RuntimeException("Error al almacenar el logo del banco", e);
        }

    }

}
