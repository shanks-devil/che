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

import org.eclipse.che.dto.shared.DTO;

/**
 * Describes state of the agent.
 *
 * @author Vitalii Parfonov
 */
@DTO
public interface AgentStateDto {

    void setCode(int code);

    int getCode();

    AgentStateDto withCode(int code);

    void setReason(String reason);

    String getReason();

    AgentStateDto withReason(String reason);
}
