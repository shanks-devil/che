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

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link SubversionAuthenticatorImpl}.
 *
 * @author Igor Vinokur
 */
@ImplementedBy(SubversionAuthenticatorViewImpl.class)
interface SubversionAuthenticatorView extends View<SubversionAuthenticatorView.ActionDelegate> {

    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user clicks cancel button. */
        void onCancelClicked();

        /** Performs any actions appropriate in response to the user clicks Log In button. */
        void onLogInClicked();

        /** Performs any actions appropriate in response to the user having changed the user name or password */
        void onCredentialsChanged();
    }

    /** @return username */
    String getUserName();

    /** @return password */
    String getPassword();

    /** Clean username and password fields. */
    void cleanCredentials();

    /** Enable or disable Log In button. */
    void setEnabledLogInButton(boolean enabled);

    /** Show dialog. */
    void showDialog();

    /** Close dialog. */
    void closeDialog();
}
