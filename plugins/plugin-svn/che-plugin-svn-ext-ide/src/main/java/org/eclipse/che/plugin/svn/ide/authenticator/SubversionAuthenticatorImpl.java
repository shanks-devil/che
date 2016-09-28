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
package org.eclipse.che.plugin.svn.ide.authenticator;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.oauth.SVNoperation;
import org.eclipse.che.ide.api.oauth.SubversionAuthenticator;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;

import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

/**
 * @author Igor Vinokur
 */
public class SubversionAuthenticatorImpl implements SubversionAuthenticator, SubversionAuthenticatorViewImpl.ActionDelegate {

    private final AppContext appContext;
    private final SubversionAuthenticatorView view;
    private final SubversionClientService     clientService;

    private String projectPath;
    private String authenticationUrl;
    private AsyncCallback<Void> callback;
    private SVNoperation        operation;

    @Inject
    public SubversionAuthenticatorImpl(AppContext appContext,
                                       SubversionAuthenticatorView view,
                                       SubversionClientService clientService) {
        this.appContext = appContext;
        this.view = view;
        this.clientService = clientService;
        this.view.setDelegate(this);
    }

    @Override
    public void authenticate(String projectPath, String authenticationUrl, Path path, AsyncCallback<Void> callback) {
        this.projectPath = projectPath;
        this.authenticationUrl = authenticationUrl;
        this.callback = callback;
        view.cleanCredentials();
        view.showDialog();
    }

    @Override
    public void authenticate(SVNoperation operation) {
        this.operation = operation;
        view.cleanCredentials();
        view.showDialog();
    }

    @Override
    public void onCancelClicked() {
        //callback.onFailure(new Exception("Authorization request rejected by user."));
        view.closeDialog();
    }

    @Override
    public void onLogInClicked() {
        if (operation != null) {
            operation.perform(view.getUserName(), view.getPassword());
        } else {
            clientService.checkout(projectPath, authenticationUrl, view.getUserName(), view.getPassword(), null, null, false)
                         .then(new Operation<CLIOutputWithRevisionResponse>() {
                             @Override
                             public void apply(CLIOutputWithRevisionResponse arg) throws OperationException {
                                 callback.onSuccess(null);
                                 //onAuthenticated(OAuthStatus.fromValue(3));
                             }
                         });
        }
        view.closeDialog();
    }

    @Override
    public void onCredentialsChanged() {
        view.setEnabledLogInButton(!isNullOrEmpty(view.getUserName()) && !isNullOrEmpty(view.getPassword()));
    }
}
