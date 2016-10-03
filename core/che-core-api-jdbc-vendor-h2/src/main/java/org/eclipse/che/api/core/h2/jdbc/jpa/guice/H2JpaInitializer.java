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
package org.eclipse.che.api.core.h2.jdbc.jpa.guice;

import com.google.inject.persist.PersistService;

import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Paths;

/**
 * Starts persistence engine with specific H2 db configuration.
 *
 * @author Anton Korneta
 */
public class H2JpaInitializer implements JpaInitializer {

    @Inject
    public H2JpaInitializer(PersistService persistService, @Named("che.conf.storage") String storageRoot) {
        System.getProperties().put("h2.baseDir", Paths.get(storageRoot).resolve("db").toString());
        persistService.start();
    }
}
