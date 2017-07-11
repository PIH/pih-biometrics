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
import org.pih.biometric.service.model.BiometricSubject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Tests the template REST controller
 */
public class MatchControllerTest extends BaseBiometricTest {

    @Override
    protected List<BiometricSubject> loadSubjectsToDb() throws Exception {
        List<BiometricSubject> l = new ArrayList<>();
        l.add(loadSubjectToDb("101-01-1"));
        l.add(loadSubjectToDb("101-02-1"));
        return l;
    }

    //********** POST ***********

    @Test
    public void testExactTemplateMatch() throws Exception {
        String subjectId = "101-01-1";
        ResultActions actions = match(subjectId);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.length()", is(1)));
        actions.andExpect(jsonPath("$[0].subjectId", is(subjectId)));
        actions.andExpect(jsonPath("$[0].matchScore", greaterThan(config.getMatchingThreshold())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
    }

    @Test
    public void testInexactTemplateMatch() throws Exception {
        String subjectId = "101-01-2";
        ResultActions actions = match(subjectId);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.length()", is(1)));
        actions.andExpect(jsonPath("$[0].subjectId", is("101-01-1")));
        actions.andExpect(jsonPath("$[0].matchScore", greaterThan(config.getMatchingThreshold())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
    }

    @Test
    public void testMultipleTemplateMatch() throws Exception {
        loadSubjectToDb("101-01-2");
        String[] subjectIds = {"101-01-1", "101-01-2"};
        for (String subjectId : subjectIds) {
            ResultActions actions = match(subjectId);
            actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
            actions.andExpect(jsonPath("$.length()", is(2)));
            actions.andExpect(jsonPath("$[0].subjectId", is(subjectId)));
            actions.andExpect(jsonPath("$[0].matchScore", greaterThan(config.getMatchingThreshold())));
            assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
        }
    }

    @Test
    public void testNoTemplateMatch() throws Exception {
        String subjectId = "101-03-1";
        ResultActions actions = match(subjectId);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.length()", is(0)));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
    }

    protected ResultActions match(String subjectId) throws Exception {
        BiometricSubject template = loadSubjectFromResource(subjectId);
        ResultActions actions = mockMvc.perform(post("/match")
                .content(objectMapper.writeValueAsString(template))
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }
}
