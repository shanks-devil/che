/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.agent.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Mechanism for checking workspace agent's state.
 *
 * @author Vitalii Parfonov
 * @author Valeriy Svydenko
 */
@Singleton
public class WsAgentHealthCheckerImpl implements WsAgentHealthChecker {
    protected static final Logger LOG = LoggerFactory.getLogger(WsAgentHealthCheckerImpl.class);

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final int                    wsAgentPingConnectionTimeoutMs;

    @Inject
    public WsAgentHealthCheckerImpl(HttpJsonRequestFactory httpJsonRequestFactory,
                                    @Named("machine.ws_agent.ping_conn_timeout_ms") int wsAgentPingConnectionTimeoutMs) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.wsAgentPingConnectionTimeoutMs = wsAgentPingConnectionTimeoutMs;
    }

    @Override
    public WsAgentHealthStateDto check(Machine machine) throws NotFoundException, ServerException {
        final Map<String, ? extends Server> servers = machine.getRuntime().getServers();
        Server wsAgent = getWsAgent(servers);
        final WsAgentHealthStateDto agentHealthStateDto = newDto(WsAgentHealthStateDto.class);
        if (wsAgent == null) {
            return agentHealthStateDto.withCode(NOT_FOUND.getStatusCode())
                                      .withReason("Workspace Agent not available");
        }
        try {
            final HttpJsonRequest pingRequest = createPingRequest(machine, wsAgent);
            final HttpJsonResponse response = pingRequest.request();
            agentHealthStateDto.withCode(response.getResponseCode());
        } catch (IOException e) {
            agentHealthStateDto.withCode(SERVICE_UNAVAILABLE.getStatusCode())
                               .withReason(e.getMessage());
        } catch (ForbiddenException | BadRequestException | ConflictException | UnauthorizedException e) {
            throw new ServerException(e);
        }
        return agentHealthStateDto;
    }

    private Server getWsAgent(Map<String, ? extends Server> servers) {
        for (Server server : servers.values()) {
            if (WSAGENT_REFERENCE.equals(server.getRef())) {
                return server;
            }
        }
        return null;
    }

    // forms the ping request based on information about the machine.
    protected HttpJsonRequest createPingRequest(Machine machine, Server wsAgent) throws ServerException {
        String wsAgentPingUrl = wsAgent.getUrl();
        // since everrest mapped on the slash in case of it absence
        // we will always obtain not found response
        if (!wsAgentPingUrl.endsWith("/")) {
            wsAgentPingUrl = wsAgentPingUrl.concat("/");
        }
        return httpJsonRequestFactory.fromUrl(wsAgentPingUrl)
                                     .setMethod(HttpMethod.GET)
                                     .setTimeout(wsAgentPingConnectionTimeoutMs);
    }

}
