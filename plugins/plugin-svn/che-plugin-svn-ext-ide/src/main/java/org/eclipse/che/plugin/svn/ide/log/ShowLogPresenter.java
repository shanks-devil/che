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
package org.eclipse.che.plugin.svn.ide.log;

import com.google.inject.Inject;

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
import org.eclipse.che.plugin.svn.shared.InfoResponse;
import org.eclipse.che.plugin.svn.shared.SubversionItem;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_SVN_OPERATION;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Manages the displaying commit log messages for specified period.
 */
public class ShowLogPresenter extends SubversionActionPresenter {

    private final SubversionCredentialsDialog              subversionCredentialsDialog;
    private final SubversionClientService                  service;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;

    private final ShowLogsView view;

    /**
     * Creates an instance of this presenter.
     */
    @Inject
    protected ShowLogPresenter(AppContext appContext,
                               SubversionOutputConsoleFactory consoleFactory,
                               SubversionCredentialsDialog subversionCredentialsDialog,
                               ProcessesPanelPresenter processesPanelPresenter,
                               SubversionClientService service,
                               NotificationManager notificationManager,
                               SubversionExtensionLocalizationConstants constants,
                               ShowLogsView view,
                               StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors);
        this.subversionCredentialsDialog = subversionCredentialsDialog;

        this.service = service;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.view = view;

        view.setDelegate(new ShowLogsView.Delegate() {
            @Override
            public void logClicked() {
                String range = ShowLogPresenter.this.view.rangeField().getValue();
                if (range != null && !range.trim().isEmpty()) {
                    ShowLogPresenter.this.view.hide();
                    showLogs(range);
                }
            }

            @Override
            public void cancelClicked() {
                ShowLogPresenter.this.view.hide();
            }
        });
    }

    /**
     * Fetches the count of revisions and opens the popup.
     */
    public void showLog() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));
        checkState(resources.length == 1);

        showLog(project, resources, null, null);
    }

    private void showLog(final Project project, final Resource[] resources, final String userName, final String password) {
        service.info(project.getLocation(), toRelative(project, resources[0]), "HEAD", false, userName, password).then(new Operation<InfoResponse>() {
            @Override
            public void apply(InfoResponse response) throws OperationException {
                if (response.getErrorOutput() != null && !response.getErrorOutput().isEmpty()) {
                    printErrors(response.getErrorOutput(), constants.commandInfo());
                    notificationManager.notify("Unable to execute subversion command", FAIL, FLOAT_MODE);
                    return;
                }

                SubversionItem subversionItem = response.getItems().get(0);
                view.setRevisionCount(subversionItem.getRevision());
                view.rangeField().setValue("1:" + subversionItem.getRevision());
                view.show();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                if (getErrorCode(error.getCause()) == UNAUTHORIZED_SVN_OPERATION) {
                    notificationManager.notify(constants.authenticationFailed(), FAIL, FLOAT_MODE);

                    subversionCredentialsDialog.askCredentials().then(new Operation<String[]>() {
                        @Override
                        public void apply(String[] credentials) throws OperationException {
                            showLog(project, resources, credentials[0], credentials[1]);
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
            }
        });
    }

    /**
     * Fetches and displays commit log messages for specified range.
     *
     * @param range
     *         range to be logged
     */
    private void showLogs(String range) {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        service.showLog(project.getLocation(), toRelative(project, resources), range).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                printResponse(response.getCommand(), response.getOutput(), null, constants.commandLog());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
            }
        });
    }

}
