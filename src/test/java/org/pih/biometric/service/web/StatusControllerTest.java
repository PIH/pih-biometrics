/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.pih.biometric.service.web;

import com.neurotec.biometrics.NMatchingSpeed;
import com.neurotec.biometrics.NTemplateSize;
import org.junit.Test;
import org.pih.biometric.service.BaseBiometricTest;
import org.pih.biometric.service.model.BiometricsStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Tests the status REST controller
 */
public class StatusControllerTest extends BaseBiometricTest {

    @Test
    public void statusTest() throws Exception {
        ResultActions actions = mockMvc.perform(get("/status").contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.numberEnrolled", is(0)));
        actions.andExpect(jsonPath("$.status", is(BiometricsStatus.AVAILABLE_STATUS)));
        actions.andExpect(jsonPath("$.config.sqliteDatabasePath", is(DB_FILE.getAbsolutePath())));
        actions.andExpect(jsonPath("$.config.matchingThreshold", is(72)));
        actions.andExpect(jsonPath("$.config.matchingSpeed", is(NMatchingSpeed.LOW.toString())));
        actions.andExpect(jsonPath("$.config.templateSize", is(NTemplateSize.LARGE.toString())));
    }
}
