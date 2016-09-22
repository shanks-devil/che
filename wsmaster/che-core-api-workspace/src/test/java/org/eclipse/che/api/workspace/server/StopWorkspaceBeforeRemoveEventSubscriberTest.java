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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class StopWorkspaceBeforeRemoveEventSubscriberTest {

    @Mock
    private WorkspaceManager wsManager;

    @InjectMocks
    private StopWorkspaceBeforeRemoveEventSubscriber stopper;

    @Test
    public void shouldStopWorkspaceOnBeforeWorkspaceRemoveEvent() throws Exception {
        final String wsId = "wsId123";
        final WorkspaceImpl rmWs = new WorkspaceImpl();
        rmWs.setId(wsId);
        rmWs.setStatus(RUNNING);
        doNothing().when(wsManager).stopWorkspace(wsId);
        when(wsManager.getWorkspace(wsId)).thenReturn(rmWs);
        stopper.onEvent(new BeforeWorkspaceRemovedEvent(rmWs));
    }

    @Test
    public void shouldIgnoreExceptionWhen() throws Exception {
        final String wsId = "wsId123";
        final WorkspaceImpl workspace = new WorkspaceImpl();
        workspace.setId(wsId);
        workspace.setStatus(RUNNING);
        doThrow(ServerException.class).when(wsManager).stopWorkspace(wsId);
        when(wsManager.getWorkspace(wsId)).thenReturn(workspace);
        stopper.onEvent(new BeforeWorkspaceRemovedEvent(workspace));
    }
}
