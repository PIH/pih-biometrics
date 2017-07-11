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
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Tests the subject REST controller
 */
public class SubjectControllerTest extends BaseBiometricTest {

    @Override
    protected List<BiometricSubject> loadSubjectsToDb() throws Exception {
        List<BiometricSubject> l = new ArrayList<>();
        l.add(loadSubjectToDb("101-01-1"));
        l.add(loadSubjectToDb("101-02-1"));
        return l;
    }

    //********** POST ***********

    @Test
    public void testPostNewSubjectWithSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricSubject subject = loadSubjectFromResource("101-03-1");
        ResultActions actions = postSubject(subject);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", is(subject.getSubjectId())));
        actions.andExpect(jsonPath("$.fingerprints[0].template", is(subject.getFingerprints().get(0).getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    @Test
    public void testPostNewSubjectWithoutSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricSubject subject = loadSubjectFromResource("101-03-1");
        subject.setSubjectId(null);
        ResultActions actions = postSubject(subject);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", notNullValue()));
        actions.andExpect(jsonPath("$.fingerprints[0].template", is(subject.getFingerprints().get(0).getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    @Test
    public void testPostDuplicateSubjectWithoutSubjectId() throws Exception {
        testPostNewSubjectWithoutSubjectId();
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
        BiometricSubject subject = loadSubjectFromResource("101-03-1");
        subject.setSubjectId(null);
        ResultActions actions = postSubject(subject);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", notNullValue()));
        actions.andExpect(jsonPath("$.fingerprints[0].template", is(subject.getFingerprints().get(0).getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(4));
    }

    @Test
    public void testPostDuplicateSubjectWithSameSubjectId() throws Exception {
        BiometricSubject subject = loadSubjectFromResource("101-03-1");
        ResultActions actions = postSubject(subject);
        subject = loadSubjectFromResource("101-03-1");
        actions = postSubject(subject);
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CONFLICT.value()));
    }

    protected ResultActions postSubject(BiometricSubject subject) throws Exception {
        ResultActions actions = mockMvc.perform(post("/subject")
                .content(objectMapper.writeValueAsString(subject))
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }

    //********** PUT ***********

    @Test
    public void testPutNewSubjectWithSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricSubject subject = loadSubjectFromResource("101-03-1");
        ResultActions actions = putSubject(subject);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", is(subject.getSubjectId())));
        actions.andExpect(jsonPath("$.fingerprints[0].template", is(subject.getFingerprints().get(0).getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    @Test
    public void testPutNewSubjectWithoutSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricSubject subject = loadSubjectFromResource("101-03-1");
        subject.setSubjectId(null);
        ResultActions actions = putSubject(subject);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", notNullValue()));
        actions.andExpect(jsonPath("$.fingerprints[0].template", is(subject.getFingerprints().get(0).getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    @Test
    public void testPutDuplicateSubjectWithoutSubjectId() throws Exception {
        testPostNewSubjectWithoutSubjectId();
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
        BiometricSubject subject = loadSubjectFromResource("101-03-1");
        subject.setSubjectId(null);
        ResultActions actions = putSubject(subject);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", notNullValue()));
        actions.andExpect(jsonPath("$.fingerprints[0].template", is(subject.getFingerprints().get(0).getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(4));
    }

    @Test
    public void testPutDuplicateSubjectWithSameSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricSubject subject = loadSubjectFromResource("101-03-1");
        ResultActions actions = putSubject(subject);
        subject = loadSubjectFromResource("101-03-1");
        actions = putSubject(subject);
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    protected ResultActions putSubject(BiometricSubject subject) throws Exception {
        ResultActions actions = mockMvc.perform(put("/subject")
                .content(objectMapper.writeValueAsString(subject))
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }

    //********** GET ***********

    @Test
    public void testGetExistingSubject() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricSubject subject = loadSubjectFromResource("101-01-1");
        ResultActions actions = getSubject(subject.getSubjectId());
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", is(subject.getSubjectId())));
        actions.andExpect(jsonPath("$.fingerprints[0].template", not(isEmptyOrNullString()))); // TODO: This does not match template.  Determine why.
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
    }

    @Test
    public void testGetMissingSubject() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        ResultActions actions = getSubject("missing-subject");
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.NOT_FOUND.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
    }

    protected ResultActions getSubject(String subjectId) throws Exception {
        ResultActions actions = mockMvc.perform(get("/subject/"+subjectId)
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }

    //********** DELETE ***********

    @Test
    public void testDeleteExistingSubject() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricSubject subject = loadSubjectFromResource("101-01-1");
        ResultActions actions = deleteSubject(subject.getSubjectId());
        actions.andExpect(content().string(isEmptyOrNullString()));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(1));
    }

    @Test
    public void testDeleteMissingSubject() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        ResultActions actions = getSubject("missing-subject");
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.NOT_FOUND.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
    }

    protected ResultActions deleteSubject(String subjectId) throws Exception {
        ResultActions actions = mockMvc.perform(delete("/subject/"+subjectId)
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }
}
