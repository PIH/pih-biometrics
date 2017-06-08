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
 * Simple bean to encapsulate the status of the system
 */
public class BiometricsStatus implements Serializable {

    public static final String AVAILABLE_STATUS = "AVAILABLE";
    public static final String NOT_AVAILABLE_STATUS = "NOT AVAILABLE";

    private String status;
    private String errorDetails;
    private Integer numberEnrolled;
    private BiometricsConfig config;

    public BiometricsStatus() { }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Integer getNumberEnrolled() {
        return numberEnrolled;
    }

    public void setNumberEnrolled(Integer numberEnrolled) {
        this.numberEnrolled = numberEnrolled;
    }

    public BiometricsConfig getConfig() {
        return config;
    }

    public void setConfig(BiometricsConfig config) {
        this.config = config;
    }
}
