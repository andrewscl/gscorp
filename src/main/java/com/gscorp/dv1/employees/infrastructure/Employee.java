package com.gscorp.dv1.employees.infrastructure;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name="employee")
@AllArgsConstructor
@NoArgsConstructor
@Data
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

    private String gender;
    private String nationality;
    private String maritalStatus;
    private String studyLevel;
    private String profession;

    private String previtionalSystem;
    private String healthSystem;

    private String paymentMethod;
    private String bankId;
    private String bankName;
    private String bankAccountType;
    private String bankAccountNumber;

    private String contractType;
    private String workSchedule;
    private String shiftSystem;
    private String position;

    private String password;
    private String photoUrl;

    private LocalDate hireDate;
    private LocalDate birthDate;
    private LocalDate exitDate;

    private Boolean active = true;

    private String address;

    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY)
    private User user;

    @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();

}