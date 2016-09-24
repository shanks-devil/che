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
package org.eclipse.che.api.core.jdbc.jpa.event;

/**
 * TODO
 *
 * @author Anton Korneta.
 */
public class RemovalContext {
    private Exception cause;

    
    // TODO
    public Exception cause() {
        return cause;
    }

    // TODO
    public boolean isFailure() {
        return cause != null;
    }

    // TODO
    public RemovalContext setCause(Exception cause) {
        this.cause = cause;
        return this;
    }
}
