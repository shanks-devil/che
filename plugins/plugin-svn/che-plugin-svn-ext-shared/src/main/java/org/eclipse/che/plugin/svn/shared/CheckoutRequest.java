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
package org.eclipse.che.plugin.svn.shared;

import org.eclipse.che.dto.shared.DTO;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Representation of a checkout request.
 *
 * @author <a href="mailto:jwhitlock@apache.org">Jeremy Whitlock</a>
 */
@DTO
public interface CheckoutRequest {

    /**
     * @return the project path the request is associated with.
     */
    String getProjectPath();

    /**
     * @param projectPath the project path to set
     */
    void setProjectPath(@NotNull final String projectPath);

    /**
     * @param projectPath the project path to use
     */
    CheckoutRequest withProjectPath(@NotNull final String projectPath);

    /**
     * @return the paths the request is associated with
     */
    List<String> getPaths();

    /**
     * @param paths the paths to set
     */
    void setPaths(@NotNull final List<String> paths);

    /**
     * @param paths the paths to use
     */
    CheckoutRequest withPaths(@NotNull final List<String> paths);

    /**
     * @return the url to checkout
     */
    String getUrl();

    /**
     * @param url the url to set
     */
    void setUrl(@NotNull final String url);

    /**
     * @param url the url
     *
     * @return the request
     */
    CheckoutRequest withUrl(@NotNull final String url);

    /**
     * @return the depth to checkout
     */
    String getDepth();

    /**
     * @param depth the depth to set
     */
    void setDepth(@NotNull final String depth);

    /**
     * @param depth the depth
     *
     * @return the request
     */
    CheckoutRequest withDepth(@NotNull final String depth);

    /**
     * @return whether or not to ignore externals
     */
    boolean isIgnoreExternals();

    /**
     * @param ignoreExternals whether or not to ignore externals
     */
    void setIgnoreExternals(@NotNull final boolean ignoreExternals);

    /**
     * @param ignoreExternals whether or not to ignore externals
     *
     * @return the request
     */
    CheckoutRequest withIgnoreExternals(@NotNull final boolean ignoreExternals);

    /**
     * @return the revision to checkout
     */
    String getRevision();

    /**
     * @param revision the revision to set
     */
    void setRevision(@NotNull final String revision);

    /**
     * @param revision the revision
     *
     * @return the request
     */
    CheckoutRequest withRevision(@NotNull final String revision);


    /**
     * @return the revision to checkout
     */
    String getLogin();

    /**
     * @param revision the revision to set
     */
    void setLogin(@NotNull final String login);

    /**
     * @param revision the revision
     *
     * @return the request
     */
    CheckoutRequest withLogin(@NotNull final String login);

    /**
     * @return the revision to checkout
     */
    String getPassword();

    /**
     * @param revision the revision to set
     */
    void setPassword(@NotNull final String password);

    /**
     * @param revision the revision
     *
     * @return the request
     */
    CheckoutRequest withPassword(@NotNull final String password);

}
