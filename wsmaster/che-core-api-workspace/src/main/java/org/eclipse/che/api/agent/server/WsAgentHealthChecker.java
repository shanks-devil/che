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
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.AgentHealthStateDto;
import org.eclipse.che.api.workspace.shared.dto.AgentStateDto;
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
public class WsAgentHealthChecker implements AgentHealthChecker {
    private static final String WS_AGENT_SERVER_NOT_FOUND_ERROR = "Workspace agent server not found in dev machine.";

    protected static final Logger LOG = LoggerFactory.getLogger(WsAgentHealthChecker.class);

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final int                    wsAgentPingConnectionTimeoutMs;

    @Inject
    public WsAgentHealthChecker(HttpJsonRequestFactory httpJsonRequestFactory,
                                @Named("machine.ws_agent.ping_conn_timeout_ms") int wsAgentPingConnectionTimeoutMs) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.wsAgentPingConnectionTimeoutMs = wsAgentPingConnectionTimeoutMs;
    }

    @Override
    public String agentId() {
        return WSAGENT_REFERENCE;
    }

    @Override
    public AgentHealthStateDto check(Machine devMachine) throws NotFoundException,
                                                                ServerException,
                                                                ForbiddenException,
                                                                BadRequestException,
                                                                UnauthorizedException,
                                                                IOException,
                                                                ConflictException {
        final Map<String, ? extends Server> servers = devMachine.getRuntime().getServers();
        Server wsAgent = null;
        for (Server ser : servers.values()) {
            if (WSAGENT_REFERENCE.equals(ser.getRef())) {
                wsAgent = ser;
            }
        }
        final AgentHealthStateDto agentHealthStateDto = newDto(AgentHealthStateDto.class).withAgentId(WSAGENT_REFERENCE);
        if (wsAgent == null) {
            return agentHealthStateDto.withAgentState(newDto(AgentStateDto.class)
                                                              .withCode(NOT_FOUND.getStatusCode())
                                                              .withReason("Workspace Agent not available if Dev machine are not RUNNING"));
        }
        try {
            final HttpJsonRequest pingRequest = createPingRequest(devMachine);
            final HttpJsonResponse response = pingRequest.request();
            agentHealthStateDto.setAgentState(newDto(AgentStateDto.class)
                                                      .withCode(response.getResponseCode())
                                                      .withReason(response.asString()));
        } catch (IOException e) {
            agentHealthStateDto.setAgentState(newDto(AgentStateDto.class)
                                                      .withCode(SERVICE_UNAVAILABLE.getStatusCode())
                                                      .withReason(e.getMessage()));
        }
        return agentHealthStateDto;
    }

    // forms the ping request based on information about the machine.
    protected HttpJsonRequest createPingRequest(Machine machine) throws ServerException {
        Map<String, ? extends Server> servers = machine.getRuntime().getServers();
        Server wsAgentServer = servers.get(Constants.WS_AGENT_PORT);
        if (wsAgentServer == null) {
            LOG.error("{} WorkspaceId: {}, DevMachine Id: {}, found servers: {}",
                      WS_AGENT_SERVER_NOT_FOUND_ERROR, machine.getWorkspaceId(), machine.getId(), servers);
            throw new ServerException(WS_AGENT_SERVER_NOT_FOUND_ERROR);
        }
        String wsAgentPingUrl = wsAgentServer.getUrl();
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
