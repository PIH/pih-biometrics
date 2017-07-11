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
 * Represents a sample of biometric data for a person
 * type: represents an implementation-configurable representation of the given sample (eg. "LEFT_MIDDLE_FINGER")
 * format: represents the standard used to represent the template, ISO, ANSI, etc
 * template:  the textual representation of the sample, generally Base64 encoded binary data
 */
public class BiometricSample implements Serializable {

    private String type;
    private BiometricTemplateFormat format;
    private String template;

    public BiometricSample() { }

    public BiometricSample(String type, BiometricTemplateFormat format, String template) {
        this.type = type;
        this.format = format;
        this.template = template;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BiometricTemplateFormat getFormat() {
        return format;
    }

    public void setFormat(BiometricTemplateFormat format) {
        this.format = format;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}