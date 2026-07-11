package com.gscorp.dv1.communications.contacts.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>{

    Optional <Contact> findByEmail( String email);

}
