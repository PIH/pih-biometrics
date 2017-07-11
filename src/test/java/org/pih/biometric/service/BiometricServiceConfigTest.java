/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.pih.biometric.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pih.biometric.service.model.BiometricConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BiometricServiceConfigTest extends BaseBiometricTest {

    @Autowired
    BiometricService app;

    @Test
    public void contextLoadsWithDefaults() throws Exception {
        Assert.assertNotNull(app);
        BiometricConfig config = app.getConfig();
        assertThat(config.getMatchingThreshold(), is(72));
        assertThat(config.getMatchingSpeed(), is(BiometricConfig.MatchingSpeed.LOW));
        assertThat(config.getTemplateSize(), is(BiometricConfig.TemplateSize.LARGE));
        assertThat(config.getLicenseFiles(), is(emptyCollectionOf(File.class)));
        assertThat(config.getSqliteDatabasePath(), isEmptyOrNullString());
    }
}
