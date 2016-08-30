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
package org.eclipse.che.api.workspace.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.AgentState;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceAgentHealthStateDto;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;

/**
 * //
 *
 * @author Vitalii Parfonov
 */
@Api(value = "/workspace-agent-health", description = "Workspace Agent Health Checker")
@Path("/workspace-agent-health")
public class WorkspaceAgentHealthChecker extends Service {


    private final WorkspaceManager              workspaceManager;
    private final HttpJsonRequestFactory        httpJsonRequestFactory;
    private final int wsAgentPingConnectionTimeoutMs;

    @Inject
    public WorkspaceAgentHealthChecker(WorkspaceManager workspaceManager,
                                       HttpJsonRequestFactory httpJsonRequestFactory,
                                       @Named("machine.ws_agent.ping_conn_timeout_ms") int wsAgentPingConnectionTimeoutMs) {
        this.workspaceManager = workspaceManager;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.wsAgentPingConnectionTimeoutMs = wsAgentPingConnectionTimeoutMs;
    }


    @GET
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get the workspace by the composite key",
                  notes = "Composite key can be just workspace ID or in the " +
                          "namespace:workspace_name form, where namespace is optional (e.g :workspace_name is valid key too.")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested workspace entity"),
                   @ApiResponse(code = 404, message = "The workspace with specified id does not exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceAgentHealthStateDto getByKey(@ApiParam(value = "Composite key",
                                           examples = @Example({@ExampleProperty("workspace12345678"),
                                                                @ExampleProperty("namespace:workspace_name"),
                                                                @ExampleProperty(":workspace_name")}))
                                 @PathParam("key") String key) throws NotFoundException,
                                                                      ServerException,
                                                                      ForbiddenException,
                                                                      BadRequestException,
                                                                      UnauthorizedException,
                                                                      IOException,
                                                                      ConflictException {
        validateKey(key);
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
        if (WorkspaceStatus.RUNNING != workspace.getStatus()) {
            return DtoFactory.newDto(WorkspaceAgentHealthStateDto.class).withWorkspaceStatus(workspace.getStatus());
        }
        Map<String, AgentState> agentStates = new HashMap<>(1);
        final MachineImpl devMachine = workspace.getRuntime().getDevMachine();
        if (devMachine == null) {
            agentStates.put(WSAGENT_REFERENCE, DtoFactory.newDto(AgentState.class).withCode(NOT_FOUND.getStatusCode())
                                                            .withReason("Workspace Agent not available if Dev machine are not RUNNING"));
            return DtoFactory.newDto(WorkspaceAgentHealthStateDto.class).withWorkspaceStatus(workspace.getStatus())
                                                                        .withAgentStates(agentStates);
        }
        final Map<String, ServerImpl> servers = devMachine.getRuntime().getServers();
        Server wsAgent = null;
        for (ServerImpl ser : servers.values()) {
            if (WSAGENT_REFERENCE.equals(ser.getRef())) {
                wsAgent = ser;
            }
        }
        if (wsAgent == null) {
            agentStates.put(WSAGENT_REFERENCE, DtoFactory.newDto(AgentState.class).withCode(NOT_FOUND.getStatusCode())
                                                         .withReason("Workspace Agent not available if Dev machine are not RUNNING"));
            return DtoFactory.newDto(WorkspaceAgentHealthStateDto.class).withWorkspaceStatus(workspace.getStatus())
                             .withAgentStates(agentStates);

        }

        String wsAgentPingUrl = wsAgent.getUrl();
        // since everrest mapped on the slash in case of it absence
        // we will always obtain not found response
        if (!wsAgentPingUrl.endsWith("/")) {
            wsAgentPingUrl = wsAgentPingUrl.concat("/");
        }
        try {

            final HttpJsonResponse response = httpJsonRequestFactory.fromUrl(wsAgentPingUrl)
                                                                    .setMethod(HttpMethod.GET)
                                                                    .setTimeout(wsAgentPingConnectionTimeoutMs).request();
            agentStates.put(WSAGENT_REFERENCE, DtoFactory.newDto(AgentState.class).withCode(response.getResponseCode())
                                                         .withReason(response.asString()));

            final WorkspaceAgentHealthStateDto workspaceAgentHealthStateDto = DtoFactory.newDto(WorkspaceAgentHealthStateDto.class)
                                                                                        .withWorkspaceStatus(workspace.getStatus())
                                                                                        .withAgentStates(agentStates);
            return workspaceAgentHealthStateDto;
        } catch (IOException e) {
            agentStates.put(WSAGENT_REFERENCE, DtoFactory.newDto(AgentState.class).withCode(SERVICE_UNAVAILABLE.getStatusCode())
                                                         .withReason(e.getMessage()));

            final WorkspaceAgentHealthStateDto workspaceAgentHealthStateDto = DtoFactory.newDto(WorkspaceAgentHealthStateDto.class)
                                                                                        .withWorkspaceStatus(workspace.getStatus())
                                                                                        .withAgentStates(agentStates);
            return workspaceAgentHealthStateDto;

        }
    }



    /*
    * Validate composite key.
    *
    */
    private void validateKey(String key) throws BadRequestException {
        String[] parts = key.split(":", -1); // -1 is to prevent skipping trailing part
        switch (parts.length) {
            case 1: {
                return; // consider it's id
            }
            case 2: {
                if (parts[1].isEmpty()) {
                    throw new BadRequestException("Wrong composite key format - workspace name required to be set.");
                }
                break;
            }
            default: {
                throw new BadRequestException(format("Wrong composite key %s. Format should be 'username:workspace_name'. ", key));
            }
        }
    }
}
