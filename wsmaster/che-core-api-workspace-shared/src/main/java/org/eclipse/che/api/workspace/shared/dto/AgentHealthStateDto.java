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

/**
 * Describes status of the agent
 *
 * @author Vitalii Parfonov
 * @author Valeriy Svydenko
 */
@DTO
public interface AgentHealthStateDto {

    void setWorkspaceStatus(WorkspaceStatus status);

    AgentHealthStateDto withWorkspaceStatus(WorkspaceStatus status);

    /**
     * Returns the status of the current workspace instance.
     * <p>
     * <p>All the workspaces which are stopped have runtime
     * are considered {@link WorkspaceStatus#STOPPED}.
     */
    WorkspaceStatus getWorkspaceStatus();

    void setAgentState(AgentStateDto agentState);

    AgentHealthStateDto withAgentState(AgentStateDto agentState);

    /** Returns state of the agent */
    AgentStateDto getAgentState();

    void setAgentId(String agentId);

    AgentHealthStateDto withAgentId(String agentId);

    /** Returns id of the agent */
    String getAgentId();
}
