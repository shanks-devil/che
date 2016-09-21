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
import org.eclipse.che.api.workspace.shared.dto.AgentHealthStateDto;

import java.io.IOException;

/**
 * Interface which describes mechanism for checking agent's state.
 *
 * @author Vitalii Parfonov
 */
public interface AgentHealthChecker {
    /** Returns agent id */
    String agentId();

    /**
     * Verify if the agent is alive.
     *
     * @param devMachine
     *         development machine instance
     * @return state of the workspace agent
     * @throws NotFoundException
     *         if the agent with specified id does not exist
     * @throws ServerException
     *         if internal server error occurred
     * @throws ForbiddenException
     *         if the user is not workspace owner
     * @throws BadRequestException
     * @throws UnauthorizedException
     *         if the user is not authorized
     * @throws IOException
     * @throws ConflictException
     */
    AgentHealthStateDto check(Machine devMachine) throws NotFoundException,
                                                         ServerException,
                                                         ForbiddenException,
                                                         BadRequestException,
                                                         UnauthorizedException,
                                                         IOException,
                                                         ConflictException;
}
