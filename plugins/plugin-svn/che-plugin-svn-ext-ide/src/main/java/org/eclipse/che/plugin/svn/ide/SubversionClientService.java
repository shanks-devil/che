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
package org.eclipse.che.plugin.svn.ide;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponseList;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;
import org.eclipse.che.plugin.svn.shared.Depth;
import org.eclipse.che.plugin.svn.shared.GetRevisionsResponse;
import org.eclipse.che.plugin.svn.shared.InfoResponse;

import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Subversion from the client.
 *
 * @author Jeremy Whitlock
 */
public interface SubversionClientService {

    /**
     * Adds the provided paths to version control.
     *
     * @param project
     *         the project path
     * @param paths
     *         the paths to update
     * @param depth
     *         the update depth (--depth)
     * @param addIgnored
     *         whether or not to add ignored files (--no-ignore)
     * @param addParents
     *         whether or not to add parent paths (--parents)
     * @param autoProps
     *         whether to explicitly use automatic properties (--auto-props)
     * @param noAutoProps
     *         whether to explicitly not use automatic properties (--no-auto-props)
     */
    Promise<CLIOutputResponse> add(Path project,
                                   Path[] paths,
                                   String depth,
                                   boolean addIgnored,
                                   boolean addParents,
                                   boolean autoProps,
                                   boolean noAutoProps);

    /**
     * Removes the provided paths from version control.
     *
     * @param project
     *         the project path
     * @param paths
     *         the paths to remove
     */
    Promise<CLIOutputResponse> remove(Path project, Path[] paths);

    /**
     * Reverts any local changes to provided paths and resolves any conflicted states.
     *
     * @param project
     *         the project path
     * @param paths
     *         the paths to remove
     */
    Promise<CLIOutputResponse> revert(Path project, Path[] paths, String depth);

    /**
     * Copy provided path.
     *
     * @param project
     *         the project path
     * @param source
     *         source item path
     * @param destination
     *         destination path
     */
    Promise<CLIOutputResponse> copy(Path project, Path source, Path destination, String comment);

    /**
     * Merge specified URL with target.
     *
     * @param project
     *          project path
     * @param target
     *          target directory
     * @param sourceUrl
     *          source URL to merge
     */
    Promise<CLIOutputResponse> merge(Path project, Path target, Path sourceUrl);

    /**
     * Retrieves the information about repository item.
     *
     * @param project
     *          relative path to the project in local workspace
     * @param target
     *          target to operate
     * @param revision
     *          revision, use HEAD to specify latest revision
     * @param children
     *          whether list children or not
     */
    Promise<InfoResponse> info(Path project, Path target, String revision, boolean children);
    Promise<InfoResponse> info(Path project, String target, String revision, boolean children);

    /**
     * Retrieves the status for the provided paths, or the working copy as a whole.
     *
     * @param project
     *         the project path
     * @param paths
     *         the paths to update
     * @param depth
     *         the update depth (--depth)
     * @param ignoreExternals
     *         whether or not to ignore externals (--ignore-externals)
     * @param showIgnored
     *         whether or not to show ignored paths (--no-ignored)
     * @param showUpdates
     *         whether or not to show repository updates (--show-updates)
     * @param showUnversioned
     *         whether or not to show unversioned paths (--quiet)
     * @param verbose
     *         whether or not to be verbose (--verbose)
     * @param changeLists
     *         which changelists to operation on (--changelist)
     */
    Promise<CLIOutputResponse> status(Path project,
                                      Path[] paths,
                                      String depth,
                                      boolean ignoreExternals,
                                      boolean showIgnored,
                                      boolean showUpdates,
                                      boolean showUnversioned,
                                      boolean verbose,
                                      List<String> changeLists);

    /**
     * Updates the provided paths, or the working copy as a whole, to the latest, or requested, repository version.
     *
     * @param project
     *         the project path
     * @param paths
     *         the paths to update
     * @param revision
     *         the revision (0 indicates HEAD) (--revision)
     * @param depth
     *         the update depth (--depth)
     * @param ignoreExternals
     *         whether or not to ignore externals (--ignore-externals)
     * @param accept
     *         the accept argument (--accept)
     */
    Promise<CLIOutputWithRevisionResponse> update(Path project, Path[] paths, String revision, String depth, boolean ignoreExternals, String accept);

    Promise<CLIOutputWithRevisionResponse> checkout(String projectPath, String url, String login, String password, String revision, String depth, boolean ignoreExternals);

    Promise<CLIOutputResponse> showLog(Path project, Path[] paths, String revision);

    Promise<CLIOutputResponse> showDiff(Path project, Path[] paths, String revision);

    /**
     * Locks the given paths.
     *
     * @param project
     *         the path of the project
     * @param paths
     *         the paths to lock
     * @param force
     *         if false, will warn if another user already has a lock on a target, leave this target unchanged, and continue.<br>
     *         if true, will steal the lock from the previous owner instead
     */
    Promise<CLIOutputResponse> lock(Path project, Path[] paths, boolean force);

    /**
     * Unocks the given paths.
     *
     * @param project
     *         the path of the project
     * @param paths
     *         the paths to lock
     * @param force
     *         if false, will warn if another user already has a lock on a target, leave this target unchanged, and continue.<br>
     *         if true, will unlock anyway
     */
    Promise<CLIOutputResponse> unlock(Path project, Path[] paths, boolean force);

    /**
     * Commits the changes in the repository.
     *
     * @param project
     *         the project path
     * @param paths
     *         the paths to include in the commit
     * @param message
     *         the commit message
     * @param keepChangeLists
     *         if true, doesn't remove the changelist assigments from working copy items after committing
     * @param keepLocks
     *         if true, doesn't unlock files after commiting
     */
    Promise<CLIOutputWithRevisionResponse> commit(Path project, Path[] paths, String message, boolean keepChangeLists, boolean keepLocks);

    /**
     * Cleans up recursively the working copy.
     *
     * @param project
     *         the project path
     * @param paths
     *         the paths to clean up
     */
    Promise<CLIOutputResponse> cleanup(Path project, Path[] paths);

    Promise<CLIOutputResponse> showConflicts(Path project, Path[] paths);

    Promise<CLIOutputResponseList> resolve(Path project, Map<String, String> resolutions, String depth);

    Promise<Void> saveCredentials(String repositoryUrl, String username, String password);

    /**
     * Move provided path.
     *
     * @param project
     *         the project path
     * @param source
     *         source item path
     * @param destination
     *         destination path
     */
    Promise<CLIOutputResponse> move(Path project, Path source, Path destination, String comment);

    /**
     * Set specified property to a path or a target.
     *
     * @param project
     *         the project path
     * @param propertyName
     *         the property name
     * @param propertyValues
     *         the property value
     * @param depth
     *         the depth
     * @param force
     *         forcing or not
     * @param path
     *         path to which property sets
     */
    Promise<CLIOutputResponse> propertySet(Path project, String propertyName, String propertyValues, Depth depth, boolean force, Path path);

    /**
     * Get specified property for a path or a target.
     *
     * @param project the project path
     * @param propertyName the property name
     * @param path path to which property get
     */
    Promise<CLIOutputResponse> propertyGet(Path project, String propertyName, Path path);

    /**
     * Get properties set for a path or a target.
     *
     * @param project the project path
     * @param path path to which property get
     */
    Promise<CLIOutputResponse> propertyList(Path project, Path path);

    /**
     * Delete specified property from a path or a target.
     *
     * @param project
     *         the project path
     * @param propertyName
     *         the property name
     * @param depth
     *         the depth
     * @param force
     *         forcing or not
     * @param path
     *         path from which property should be deleted
     */
    Promise<CLIOutputResponse> propertyDelete(Path project, String propertyName, Depth depth, boolean force, Path path);

    /**
     * Get the list of all revisions where a given path was modified
     *
     * @param project
     *         the project path
     * @param path
     *         path to get the revisions for
     * @param revisionRange
     *         the range of revisions to check
     */
    Promise<GetRevisionsResponse> getRevisions(Path project, Path path, String revisionRange);
}
