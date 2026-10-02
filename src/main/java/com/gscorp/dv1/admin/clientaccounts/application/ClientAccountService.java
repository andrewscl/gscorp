package com.gscorp.dv1.admin.clientaccounts.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.admin.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.admin.clientaccounts.web.dto.CreateClientAccountRequest;

public interface ClientAccountService {

    List<ClientAccountDto> getDtosByUserScope(
                                boolean ignoreProjectFilter,
                                List<Long> projectIds);

    ClientAccountDto createClientAccount(
                                boolean ignoreProjectFilter,
                                List<Long> projectIds,
                                CreateClientAccountRequest req, 
                                UUID userExternalId);

    ClientAccountDto getAccountDtoByExternalId(
                                boolean ignoreProjectFilter,
                                List<Long> projectIds,
                                UUID externalId);

    List<ClientAccountDto> getClientAccountsBySite(
                                boolean ignoreProjectFilter,
                                List<Long> projectIds,
                                UUID siteExternalId);

}
