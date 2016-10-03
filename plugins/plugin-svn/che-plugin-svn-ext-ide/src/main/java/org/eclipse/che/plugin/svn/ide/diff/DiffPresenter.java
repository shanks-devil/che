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
package org.eclipse.che.plugin.svn.ide.diff;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_SVN_OPERATION;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.DiffAction} action.
 */
@Singleton
public class DiffPresenter extends SubversionActionPresenter {

    private final NotificationManager                      notificationManager;
    private final SubversionCredentialsDialog              subversionCredentialsDialog;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;

    @Inject
    protected DiffPresenter(final AppContext appContext,
                            final NotificationManager notificationManager,
                            final SubversionOutputConsoleFactory consoleFactory,
                            final SubversionCredentialsDialog subversionCredentialsDialog,
                            final ProcessesPanelPresenter processesPanelPresenter,
                            final SubversionClientService service,
                            final SubversionExtensionLocalizationConstants constants,
                            final StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors);
        this.subversionCredentialsDialog = subversionCredentialsDialog;

        this.service = service;
        this.notificationManager = notificationManager;
        this.constants = constants;
    }

    public void showDiff() {
        showDiff(null, null);
    }

    private void showDiff(String userName, String password) {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        service.showDiff(project.getLocation(), toRelative(project, resources), "HEAD", userName, password).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(), constants.commandDiff());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                if (getErrorCode(error.getCause()) == UNAUTHORIZED_SVN_OPERATION) {
                    notificationManager.notify(constants.authenticationFailed(), FAIL, FLOAT_MODE);

                    subversionCredentialsDialog.askCredentials().then(new Operation<String[]>() {
                        @Override
                        public void apply(String[] credentials) throws OperationException {
                            showDiff(credentials[0], credentials[1]);
                        }
                    }).catchError(new Operation<PromiseError>() {
                        @Override
                        public void apply(PromiseError error) throws OperationException {
                            notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                        }
                    });
                } else {
                    notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                }
                notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
            }
        });
    }

}
