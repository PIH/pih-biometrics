/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.pih.biometric.service.model;

import java.io.Serializable;

/**
 * Represents a template extraction of biometric data for a person,
 * which may include one or more fingerprints or other biometric modalities
 * The template is a String, which may be an encoded binary value
 */
public class BiometricsTemplate implements Serializable {

    private String subjectId;
    private String template;

    public BiometricsTemplate() { }

    public BiometricsTemplate(String subjectId, String template) {
        this.subjectId = subjectId;
        this.template = template;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
