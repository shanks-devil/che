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

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.api.oauth.SubversionAuthenticator;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

/**
 * @author Igor Vinokur
 */
public class SubversionAuthenticatorImpl implements SubversionAuthenticator, SubversionAuthenticatorViewImpl.ActionDelegate {

    private final SubversionAuthenticatorView view;

    private AsyncCallback<String[]> callback;

    @Inject
    public SubversionAuthenticatorImpl(SubversionAuthenticatorView view) {
        this.view = view;
        this.view.setDelegate(this);
    }

    @Override
    public Promise<String[]> authenticate() {
        view.cleanCredentials();
        view.showDialog();
        return createFromAsyncRequest(new RequestCall<String[]>() {
            @Override
            public void makeCall(final AsyncCallback<String[]> callback) {
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
        callback.onSuccess(new String[]{view.getUserName(), view.getPassword()});
        view.closeDialog();
    }

    @Override
    public void onCredentialsChanged() {
        view.setEnabledLogInButton(!isNullOrEmpty(view.getUserName()) && !isNullOrEmpty(view.getPassword()));
    }
}
