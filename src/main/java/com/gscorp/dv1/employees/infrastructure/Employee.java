package com.gscorp.dv1.employees.infrastructure;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.gscorp.dv1.bank.infrastructure.Bank;
import com.gscorp.dv1.enums.BankAccountType;
import com.gscorp.dv1.enums.ContractType;
import com.gscorp.dv1.enums.Gender;
import com.gscorp.dv1.enums.HealthSystem;
import com.gscorp.dv1.enums.MaritalStatus;
import com.gscorp.dv1.enums.PaymentMethod;
import com.gscorp.dv1.enums.PrevitionalSystem;
import com.gscorp.dv1.enums.ShiftSystem;
import com.gscorp.dv1.enums.StudyLevel;
import com.gscorp.dv1.enums.WorkSchedule;
import com.gscorp.dv1.nationalities.infrastructure.Nationality;
import com.gscorp.dv1.positions.infrastructure.Position;
import com.gscorp.dv1.professions.infrastructure.Profession;
import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name="employee")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String motherSurname;
    private String fatherSurname;
    private String rut;
    private String mail;
    private String phone;
    private String secondaryPhone;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    private StudyLevel studyLevel;

    @ManyToMany
    @JoinTable(
        name = "employee_profession",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "profession_id")
    )
    @Builder.Default
    private Set<Profession> professions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private PrevitionalSystem previtionalSystem;

    @Enumerated(EnumType.STRING)
    private HealthSystem healthSystem;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Enumerated(EnumType.STRING)
    private BankAccountType bankAccountType;

    private String bankAccountNumber;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    private WorkSchedule workSchedule;

    @Enumerated(EnumType.STRING)
    private ShiftSystem shiftSystem;

    @ManyToOne
    @JoinColumn(name = "shift_pattern_id")
    private ShiftPattern shiftPattern;

    @OneToOne(mappedBy = "position", fetch = FetchType.LAZY)
    private Position position;

    private String password;
    private String photoUrl;

    private LocalDate hireDate;
    private LocalDate birthDate;
    private LocalDate exitDate;

    @Builder.Default
    private Boolean active = true;

    private String address;

    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY)
    private User user;

    @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

}