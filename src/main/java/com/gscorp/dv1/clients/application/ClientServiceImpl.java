package com.gscorp.dv1.clients.application;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientRepo;
import com.gscorp.dv1.clients.web.dto.ClientDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService{

    private final ClientRepo clientRepo;

    public Client saveClient (Client client){
        clientRepo.save(client);
        return client;
    }

    @Override
    public Client findById (Long id){
        return clientRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Client no encontrado"));
    }

    @Transactional
    public List<ClientDto> getAllClients (){
        return clientRepo.findAll(Sort.by("client").ascending())
                    .stream()
                    .map(c -> new ClientDto(c.getId(), c.getName(), c.getLegalName(), c.getTaxId(), c.getActive()))
                    .toList();
    }

    @Override
    public Client findByIdWithUsers(Long id){
        return clientRepo.findByIdWithUsers(id)
            .orElseThrow(() -> new IllegalArgumentException("Client no encontrado"));
    }

}
