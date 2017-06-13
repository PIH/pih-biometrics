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

import org.junit.Test;
import org.pih.biometric.service.api.BiometricMatchingEngine;
import org.pih.biometric.service.model.BiometricsTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Tests for the matching engine
 */
public class BiometricMatchingEngineTest extends BaseBiometricTest {

    @Autowired
    BiometricMatchingEngine matchingEngine;

    @Test
    public void shouldGetTemplatesInVariousFormats() throws Exception {
        String subjectId = "101-01-1";
        BiometricsTemplate template = loadTemplateFromResource(subjectId);
        matchingEngine.enroll(template);

        // Get template in default format
        String defaultFormat = matchingEngine.getTemplate(subjectId).getTemplate();
        String neuroFormat = matchingEngine.getTemplate(subjectId, BiometricsTemplate.Format.NEUROTECHNOLOGY).getTemplate();

        // For now just verify that these extractions all work successfully, and produce different template results
        assertThat(neuroFormat, is(defaultFormat));

        String isoFormat = matchingEngine.getTemplate(subjectId, BiometricsTemplate.Format.ISO).getTemplate();
        assertThat(isoFormat, not(defaultFormat));

        String ansiFormat = matchingEngine.getTemplate(subjectId, BiometricsTemplate.Format.ANSI).getTemplate();
        assertThat(ansiFormat, not(defaultFormat));
        assertThat(ansiFormat, not(isoFormat));
    }
}
