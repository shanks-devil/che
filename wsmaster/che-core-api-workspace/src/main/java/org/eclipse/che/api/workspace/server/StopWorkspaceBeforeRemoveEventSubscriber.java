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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

/**
 * Stops the workspace before remove.
 *
 * @author Anton Korneta.
 */
@Singleton
public class StopWorkspaceBeforeRemoveEventSubscriber implements EventSubscriber<BeforeWorkspaceRemovedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(StopWorkspaceBeforeRemoveEventSubscriber.class);

    @Inject
    private EventService     eventService;
    @Inject
    private WorkspaceManager workspaceManager;

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(BeforeWorkspaceRemovedEvent event) {
        try {
            final String wsId = event.getWorkspace().getId();
            if (workspaceManager.getWorkspace(wsId).getStatus() == RUNNING) {
                workspaceManager.stopWorkspace(wsId);
            }
        } catch (ConflictException | NotFoundException | ServerException x) {
            LOG.error(format("Failed to stop the workspace '%s' prior to its removal", event.getWorkspace().getId()), x);
        }
    }
}
