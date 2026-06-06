package com.gscorp.dv1.users.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSpecRepository
            extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>{
    
}
