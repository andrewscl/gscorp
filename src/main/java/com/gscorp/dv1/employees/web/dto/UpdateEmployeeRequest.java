package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.enums.BankAccountType;
import com.gscorp.dv1.enums.ContractType;
import com.gscorp.dv1.enums.Gender;
import com.gscorp.dv1.enums.HealthEntity;
import com.gscorp.dv1.enums.HealthSystem;
import com.gscorp.dv1.enums.MaritalStatus;
import com.gscorp.dv1.enums.PaymentMethod;
import com.gscorp.dv1.enums.PensionEntity;
import com.gscorp.dv1.enums.PrevitionalSystem;
import com.gscorp.dv1.enums.ShiftSystem;
import com.gscorp.dv1.enums.StudyLevel;
import com.gscorp.dv1.enums.WorkSchedule;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter @Setter
public class UpdateEmployeeRequest {

        @NotNull(message = "El id es obligatorio")
        private Long id;

        @NotBlank(message = "El nombre es obligatorio")
        private String name;

        @NotBlank(message = "El apellido paterno es obligatorio")
        private String fatherSurname;

        @NotBlank(message = "El apellido materno es obligatorio")
        private String motherSurname;

        @NotBlank(message = "El RUT es obligatorio")
        private String rut;

        @Email(message = "El email no es válido")
        private String mail;

        @Pattern(regexp = "^\\+?\\d{0,3}?[- .]?\\d{1,4}[- .]?\\d{3,4}[- .]?\\d{3,4}$", message = "El teléfono no es válido")
        private String phone;

        private String secondaryPhone;

        private Gender gender;

        private Long nationalityId;

        private MaritalStatus maritalStatus;

        private StudyLevel studyLevel;

        private Set<Long> professionIds;

        private PrevitionalSystem previtionalSystem;

        private PensionEntity pensionEntity;

        private HealthSystem healthSystem;

        private HealthEntity healthEntity;

        private PaymentMethod paymentMethod;

        private Long bankId;

        private BankAccountType bankAccountType;

        private String bankAccountNumber;

        private ContractType contractType;

        private WorkSchedule workSchedule;

        private ShiftSystem shiftSystem;

        private Long shiftPatternId;

        private Long positionId;

        private MultipartFile photo;

        @PastOrPresent(message = "La fecha de ingreso debe ser pasada o actual")
        private LocalDate hireDate;

        @Past(message = "La fecha de nacimiento debe ser pasada")
        private LocalDate birthDate;

        private String address;

        private Set<Long> projectIds = new HashSet<>();

}