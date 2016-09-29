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
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.api.oauth.RemoteSVNOperation;
import org.eclipse.che.ide.api.oauth.SubversionAuthenticator;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

/**
 * @author Igor Vinokur
 */
public class SubversionAuthenticatorImpl implements SubversionAuthenticator, SubversionAuthenticatorViewImpl.ActionDelegate {

    private final SubversionAuthenticatorView view;
    private final SubversionClientService     clientService;

    private String              projectPath;
    private String              authenticationUrl;
    private AsyncCallback<Void> callback;
    private RemoteSVNOperation  operation;

    @Inject
    public SubversionAuthenticatorImpl(SubversionAuthenticatorView view,
                                       SubversionClientService clientService) {
        this.view = view;
        this.clientService = clientService;
        this.view.setDelegate(this);
    }

    @Override
    public Promise<Void> authenticate(String projectPath, String authenticationUrl, Path path) {
        this.projectPath = projectPath;
        this.authenticationUrl = authenticationUrl;
        view.cleanCredentials();
        view.showDialog();
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                SubversionAuthenticatorImpl.this.callback = callback;
            }
        });
    }

    @Override
    public Promise<Void> authenticate(RemoteSVNOperation operation) {
        this.operation = operation;
        view.cleanCredentials();
        view.showDialog();
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                SubversionAuthenticatorImpl.this.callback = callback;
            }
        });
    }

    @Override
    public void onCancelClicked() {
        callback.onFailure(new Exception("Authorization request rejected by user."));
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
                             public void apply(CLIOutputWithRevisionResponse response) throws OperationException {
                                 callback.onSuccess(null);
                             }
                         }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError error) throws OperationException {
                    callback.onFailure(error.getCause());
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
