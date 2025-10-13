package com.gscorp.dv1.clients.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.web.dto.ClientDto;

@Service
public interface ClientService {

    Client saveClient (Client client);
    Client findById (Long id);
    List<ClientDto> getAllClients();
    Client findByIdWithUsers (Long id);
    void deleteById(Long id);

}
