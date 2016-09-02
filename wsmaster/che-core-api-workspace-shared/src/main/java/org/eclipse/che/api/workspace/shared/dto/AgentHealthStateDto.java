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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * //
 *
 * @author Vitalii Parfonov
 */
@DTO
public interface AgentHealthStateDto {

    void setWorkspaceStatus(WorkspaceStatus status);

    AgentHealthStateDto withWorkspaceStatus(WorkspaceStatus status);

    /**
     * Returns the status of the current workspace instance.
     *
     * <p>All the workspaces which are stopped have runtime
     * are considered {@link WorkspaceStatus#STOPPED}.
     */
    WorkspaceStatus getWorkspaceStatus();

    void setAgentStates(Map<String, AgentState> agentStates);

    AgentHealthStateDto withAgentStates(Map<String, AgentState> agentStates);

    /**
     *
     */
    Map<String, AgentState> getAgentStates();




}
