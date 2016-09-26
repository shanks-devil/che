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
package org.eclipse.che.ide.ui.loaders;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages loaders for loading phases.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class LoaderPresenter {

    public enum Phase {
        STARTING_WORKSPACE_RUNTIME,
        STARTING_WORKSPACE_AGENT,
        CREATING_PROJECT,
        CREATING_WORKSPACE_SNAPSHOT,
        WORKSPACE_STOPPED
    }

    private PopupLoaderFactory      popupLoaderFactory;
    private PopupLoaderMessages     locale;

    private Map<Phase, PopupLoader> popups = new HashMap<>();

    @Inject
    public LoaderPresenter(PopupLoaderFactory popupLoaderFactory,
                           PopupLoaderMessages locale) {
        this.popupLoaderFactory = popupLoaderFactory;
        this.locale = locale;
    }

    /**
     * Displays a loader with a message.
     *
     * @param phase
     *          corresponding phase
     * @return
     *          loader instance
     */
    public PopupLoader show(Phase phase) {
        return show(phase, null);
    }

    /**
     * Displays a loader with a message and a widget.
     *
     * @param phase
     *          corresponding phase
     * @param widget
     *          additional widget to display
     * @return
     *          loader instance
     */
    public PopupLoader show(Phase phase, Widget widget) {
        PopupLoader loader = popups.get(phase);
        if (loader != null) {
            return loader;
        }

        // Create and show a loader
        switch (phase) {
            case STARTING_WORKSPACE_RUNTIME:
                loader = popupLoaderFactory.getPopup(locale.startingWorkspaceRuntime(), locale.startingWorkspaceRuntimeDescription());
                break;
            case STARTING_WORKSPACE_AGENT:
                loader = popupLoaderFactory.getPopup(locale.startingWorkspaceAgent(), locale.startingWorkspaceAgentDescription());
                break;
            case CREATING_PROJECT:
                loader = popupLoaderFactory.getPopup(locale.creatingProject(), locale.creatingProjectDescription());
                break;

            case CREATING_WORKSPACE_SNAPSHOT:
                loader = popupLoaderFactory.getPopup(locale.snapshottingWorkspace(), locale.snapshottingWorkspaceDescription());
                break;

            case WORKSPACE_STOPPED:
                loader = popupLoaderFactory.getPopup(locale.workspaceStopped(), locale.workspaceStoppedDescription(), widget);
                break;
        }

        popups.put(phase, loader);
        return loader;
    }

    /**
     * Sets phase succeeded and hides corresponding loader.
     *
     * @param phase
     *          corresponding phase
     */
    public void setSuccess(Phase phase) {
        PopupLoader popup = popups.get(phase);
        if (popup != null) {
            // Hide the loader if status is SUCCESS
            popups.remove(phase);
            popup.setSuccess();
        }
    }

    /**
     * Sets phase filed.
     *
     * @param phase
     *          corresponding phase
     */
    public void setError(Phase phase) {
        PopupLoader popup = popups.get(phase);
        if (popup != null) {
            // Don't hide the loader with status ERROR
            popups.remove(phase);
            popup.setError();
        }
    }

}
