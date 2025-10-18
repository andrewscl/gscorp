package com.gscorp.dv1.employees.application;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;

@Service
public class EmployeeCsvImportService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    public void importCsv(MultipartFile file) throws Exception {
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CSVParser csvParser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Ajusta si tu CSV usa otro formato

            for (CSVRecord record : csvParser) {
                Employee employee = new Employee();
                employee.setName(record.get("name"));
                employee.setFatherSurname(record.get("fatherSurname"));
                employee.setMotherSurname(record.get("motherSurname"));
                employee.setRut(record.get("rut"));
                employee.setMail(record.get("mail"));
                employee.setPhone(record.get("phone"));
                employee.setSecondaryPhone(record.get("secondaryPhone"));
                employee.setGender(record.get("gender"));
                employee.setNationality(record.get("nationality"));
                employee.setMaritalStatus(record.get("maritalStatus"));
                employee.setStudyLevel(record.get("studyLevel"));
                employee.setProfession(record.get("profession"));
                employee.setPrevitionalSystem(record.get("previtionalSystem"));
                employee.setHealthSystem(record.get("healthSystem"));
                employee.setPaymentMethod(record.get("paymentMethod"));
                employee.setBankId(record.get("bankId"));
                employee.setBankName(record.get("bankName"));
                employee.setBankAccountType(record.get("bankAccountType"));
                employee.setBankAccountNumber(record.get("bankAccountNumber"));
                employee.setContractType(record.get("contractType"));
                employee.setWorkSchedule(record.get("workSchedule"));
                employee.setShiftSystem(record.get("shiftSystem"));
                employee.setPosition(record.get("position"));
                employee.setPassword(record.get("password"));
                employee.setPhotoUrl(record.get("photoUrl"));
                employee.setAddress(record.get("address"));

                // Fechas
                String hireDateStr = record.get("hireDate");
                if (hireDateStr != null && !hireDateStr.isEmpty()) {
                    employee.setHireDate(LocalDate.parse(hireDateStr, dateFormatter));
                }
                String birthDateStr = record.get("birthDate");
                if (birthDateStr != null && !birthDateStr.isEmpty()) {
                    employee.setBirthDate(LocalDate.parse(birthDateStr, dateFormatter));
                }
                String exitDateStr = record.get("exitDate");
                if (exitDateStr != null && !exitDateStr.isEmpty()) {
                    employee.setExitDate(LocalDate.parse(exitDateStr, dateFormatter));
                }

                // Boolean
                String activeStr = record.get("active");
                if (activeStr == null || activeStr.isEmpty()) {
                    employee.setActive(true); // Valor por defecto
                } else {
                    // Permite "true"/"false", "1"/"0", "sí"/"no"
                    employee.setActive(
                        activeStr.equalsIgnoreCase("true") ||
                        activeStr.equalsIgnoreCase("1") ||
                        activeStr.equalsIgnoreCase("sí") ||
                        activeStr.equalsIgnoreCase("si")
                    );
                }

                employeeRepository.save(employee);
            }
        }
    }

}
