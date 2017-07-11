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

import org.pih.biometric.service.api.BiometricMatchingEngine;
import org.pih.biometric.service.exception.SubjectNotFoundException;
import org.pih.biometric.service.model.BiometricSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;

/**
 * Provides web services for biometric subjects
 */
@RestController
@CrossOrigin
public class SubjectController {

    @Autowired
    BiometricMatchingEngine engine;

    /**
     * A POST operation is only meant to create, not update.  Duplicate subjects result in a conflict status
     * @return saved subject with subjectId populated
     */
    @RequestMapping(method = RequestMethod.POST, value = "/subject")
    @ResponseBody
    public BiometricSubject create(@RequestBody BiometricSubject subject, HttpServletResponse response, UriComponentsBuilder ucBuilder) {
        subject = engine.enroll(subject);
        response.addHeader(HttpHeaders.LOCATION, ucBuilder.path("/subject/{subjectId}").buildAndExpand(subject.getSubjectId()).toUriString());
        response.setStatus(HttpStatus.CREATED.value());
        return subject;
    }

    /**
     * A PUT operation is meant to either create or update
     * @return saved subject with subjectId populated
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/subject")
    @ResponseBody
    public BiometricSubject createOrUpdate(@RequestBody BiometricSubject subject, HttpServletResponse response, UriComponentsBuilder ucBuilder) {
        if (subject.getSubjectId() != null) {
            BiometricSubject existingTemplate = engine.getSubject(subject.getSubjectId());
            if (existingTemplate != null) {
                return engine.update(subject);
            }
        }
        return create(subject, response, ucBuilder);
    }

    /**
     * A GET operation is meant to return an existing resource
     * @return saved subject for the given subjectId
     */
    @RequestMapping(method = RequestMethod.GET, value = "/subject/{subjectId}")
    @ResponseBody
    public BiometricSubject createOrUpdate(@PathVariable String subjectId) {
        BiometricSubject subject = engine.getSubject(subjectId);
        if (subject == null) {
            throw new SubjectNotFoundException(subjectId);
        }
        return subject;
    }

    /**
     * A DELETE operation is meant to delete an existing resource
     * @return 204 No Content on success, 404 if subject cannot be found
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/subject/{subjectId}")
    @ResponseBody
    public void delete(@PathVariable String subjectId) {
        BiometricSubject subject = engine.getSubject(subjectId);
        if (subject == null) {
            throw new SubjectNotFoundException(subjectId);
        }
        engine.deleteSubject(subjectId);
    }
}
