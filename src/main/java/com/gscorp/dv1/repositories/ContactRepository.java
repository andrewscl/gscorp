package com.gscorp.dv1.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.entities.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>{

    Optional <Contact> findByEmail( String email);

}
