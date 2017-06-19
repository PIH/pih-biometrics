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

import org.junit.Test;
import org.pih.biometric.service.BaseBiometricTest;
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
        actions.andExpect(jsonPath("$.enabled", is(true)));
    }
}
