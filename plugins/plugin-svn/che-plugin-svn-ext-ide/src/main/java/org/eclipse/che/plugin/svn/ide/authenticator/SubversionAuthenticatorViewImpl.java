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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;

/**
 * @author Igor Vinokur
 */
public class SubversionAuthenticatorViewImpl extends Window implements SubversionAuthenticatorView {

    @UiField(provided = true)
    final SubversionExtensionLocalizationConstants locale;

    interface SubversionAuthenticatorImplUiBinder extends UiBinder<Widget, SubversionAuthenticatorViewImpl> {
    }

    private static SubversionAuthenticatorImplUiBinder uiBinder = GWT.create(SubversionAuthenticatorImplUiBinder.class);

    private ActionDelegate                           delegate;

    @UiField
    TextBox userNameTextBox;
    @UiField
    TextBox passwordTextBox;

    private final Button loginButton;

    @Inject
    public SubversionAuthenticatorViewImpl(SubversionExtensionLocalizationConstants locale) {
        this.locale = locale;
        Widget widget = uiBinder.createAndBindUi(this);
        this.setWidget(widget);
        this.setTitle(locale.authenticatorTitle());

        loginButton = createPrimaryButton(locale.authenticatorLoginButton(), "svn-authentication-username", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onLogInClicked();
            }
        });
        Button cancelButton = createButton(locale.authenticatorCancelButton(), "svn-authentication-password", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });

        addButtonToFooter(loginButton);
        addButtonToFooter(cancelButton);
    }

    @Override
    public void showDialog() {
        super.show();
    }

    @Override
    public void closeDialog() {
        super.hide();
    }

    @Override
    public String getUserName() {
        return userNameTextBox.getText();
    }

    @Override
    public String getPassword() {
        return passwordTextBox.getText();
    }

    @Override
    public void cleanCredentials() {
        userNameTextBox.setText("");
        passwordTextBox.setText("");
        setEnabledLogInButton(false);
    }

    @UiHandler({"userNameTextBox", "passwordTextBox"})
    void credentialChangeHandler(KeyUpEvent event) {
        delegate.onCredentialsChanged();
    }

    @Override
    public void setEnabledLogInButton(boolean enabled) {
        loginButton.setEnabled(enabled);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }
}
