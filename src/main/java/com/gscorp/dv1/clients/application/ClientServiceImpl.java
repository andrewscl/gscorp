package com.gscorp.dv1.clients.application;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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

    @Override
    @Transactional
    public Client saveClient (Client client){
        return clientRepo.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Client findById (Long id){
        return clientRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Client no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> getAllClients (){
        return clientRepo.findAll(Sort.by("name").ascending())
                    .stream()
                    .map(c -> new ClientDto(c.getId(), c.getName(), c.getLegalName(), c.getTaxId(), c.getActive()))
                    .toList();
    }

    @Override
    public Client findByIdWithUsers (Long id){
        return clientRepo.findById(id)
            .orElseThrow(()->
                new IllegalArgumentException("Usuario no encontrado" + id));
    }

    //Eliminar cliente
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!clientRepo.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }
        try {
            clientRepo.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("No se puede eliminar: el cliente tiene referencias");
        }
    }

}
