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
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;

/**
 * Describes a mechanism for checking ws agent's state.
 *
 * @author Vitalii Parfonov
 */
public interface WsAgentHealthChecker {

    /**
     * Verifies if ws agent is alive.
     * The request to ws agent will be sent periodically,
     * the request period is defined by a property named 'machine.ws_agent.ping_conn_timeout_ms'.
     *
     * @param machine
     *         machine instance
     * @return state of the ws agent, if the state of ws agent is 200 it means that agent is working well otherwise agent is down -
     * it may happen when OOM.
     * @throws NotFoundException
     *         if the agent with specified id does not exist
     * @throws ServerException
     *         if internal server error occurred
     * @throws ForbiddenException
     *         if the user is not workspace owner
     * @throws BadRequestException
     *         if has invalid parameters
     * @throws UnauthorizedException
     *         if the user is not authorized
     * @throws ConflictException
     *         if has a conflict with the current state of the target resource
     */
    WsAgentHealthStateDto check(Machine machine) throws NotFoundException,
                                                        ServerException,
                                                        ForbiddenException,
                                                        BadRequestException,
                                                        UnauthorizedException,
                                                        ConflictException;
}
