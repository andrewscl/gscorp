package com.gscorp.dv1.employees.infrastructure;

import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private String surname;
    private String mail;
    private String phone;
    private String password;
    private String photoUrl;

    private String nationalId;
    private String position;
    private String department;

    private String hireDate;
    private String birthDate;

    private Boolean active = true;

    private String address;

    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY)
    private User user;

}