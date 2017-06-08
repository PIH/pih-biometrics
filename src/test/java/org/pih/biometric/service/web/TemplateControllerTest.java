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
import org.pih.biometric.service.model.BiometricsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Tests the template REST controller
 */
public class TemplateControllerTest extends BaseBiometricTest {

    @Override
    protected List<BiometricsTemplate> loadTemplatesToDb() throws Exception {
        List<BiometricsTemplate> l = new ArrayList<>();
        l.add(loadTemplateToDb("101-01-1"));
        l.add(loadTemplateToDb("101-02-1"));
        return l;
    }

    //********** POST ***********

    @Test
    public void testPostNewTemplateWithSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricsTemplate template = loadTemplateFromResource("101-03-1");
        ResultActions actions = postTemplate(template);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", is(template.getSubjectId())));
        actions.andExpect(jsonPath("$.template", is(template.getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    @Test
    public void testPostNewTemplateWithoutSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricsTemplate template = loadTemplateFromResource("101-03-1");
        template.setSubjectId(null);
        ResultActions actions = postTemplate(template);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", notNullValue()));
        actions.andExpect(jsonPath("$.template", is(template.getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    @Test
    public void testPostDuplicateTemplateWithoutSubjectId() throws Exception {
        testPostNewTemplateWithoutSubjectId();
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
        BiometricsTemplate template = loadTemplateFromResource("101-03-1");
        template.setSubjectId(null);
        ResultActions actions = postTemplate(template);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", notNullValue()));
        actions.andExpect(jsonPath("$.template", is(template.getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(4));
    }

    @Test
    public void testPostDuplicateTemplateWithSameSubjectId() throws Exception {
        BiometricsTemplate template = loadTemplateFromResource("101-03-1");
        ResultActions actions = postTemplate(template);
        template = loadTemplateFromResource("101-03-1");
        actions = postTemplate(template);
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CONFLICT.value()));
    }

    protected ResultActions postTemplate(BiometricsTemplate template) throws Exception {
        ResultActions actions = mockMvc.perform(post("/template")
                .content(objectMapper.writeValueAsString(template))
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }

    //********** PUT ***********

    @Test
    public void testPutNewTemplateWithSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricsTemplate template = loadTemplateFromResource("101-03-1");
        ResultActions actions = putTemplate(template);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", is(template.getSubjectId())));
        actions.andExpect(jsonPath("$.template", is(template.getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    @Test
    public void testPutNewTemplateWithoutSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricsTemplate template = loadTemplateFromResource("101-03-1");
        template.setSubjectId(null);
        ResultActions actions = putTemplate(template);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", notNullValue()));
        actions.andExpect(jsonPath("$.template", is(template.getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    @Test
    public void testPutDuplicateTemplateWithoutSubjectId() throws Exception {
        testPostNewTemplateWithoutSubjectId();
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
        BiometricsTemplate template = loadTemplateFromResource("101-03-1");
        template.setSubjectId(null);
        ResultActions actions = putTemplate(template);
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", notNullValue()));
        actions.andExpect(jsonPath("$.template", is(template.getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(4));
    }

    @Test
    public void testPutDuplicateTemplateWithSameSubjectId() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricsTemplate template = loadTemplateFromResource("101-03-1");
        ResultActions actions = putTemplate(template);
        template = loadTemplateFromResource("101-03-1");
        actions = putTemplate(template);
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(3));
    }

    protected ResultActions putTemplate(BiometricsTemplate template) throws Exception {
        ResultActions actions = mockMvc.perform(put("/template")
                .content(objectMapper.writeValueAsString(template))
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }

    //********** GET ***********

    @Test
    public void testGetExistingTemplate() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricsTemplate template = loadTemplateFromResource("101-01-1");
        ResultActions actions = getTemplate(template.getSubjectId());
        actions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        actions.andExpect(jsonPath("$.subjectId", is(template.getSubjectId())));
        actions.andExpect(jsonPath("$.template", is(template.getTemplate())));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
    }

    @Test
    public void testGetMissingTemplate() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        ResultActions actions = getTemplate("missing-subject");
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.NOT_FOUND.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
    }

    protected ResultActions getTemplate(String subjectId) throws Exception {
        ResultActions actions = mockMvc.perform(get("/template/"+subjectId)
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }

    //********** DELETE ***********

    @Test
    public void testDeleteExistingTemplate() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        BiometricsTemplate template = loadTemplateFromResource("101-01-1");
        ResultActions actions = deleteTemplate(template.getSubjectId());
        actions.andExpect(content().string(isEmptyOrNullString()));
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(1));
    }

    @Test
    public void testDeleteMissingTemplate() throws Exception {
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
        ResultActions actions = getTemplate("missing-subject");
        assertThat(actions.andReturn().getResponse().getStatus(), is(HttpStatus.NOT_FOUND.value()));
        assertThat(matchingEngine.getNumberEnrolled(), is(2));
    }

    protected ResultActions deleteTemplate(String subjectId) throws Exception {
        ResultActions actions = mockMvc.perform(delete("/template/"+subjectId)
                .contentType(MediaType.APPLICATION_JSON_UTF8));
        return actions;
    }
}
