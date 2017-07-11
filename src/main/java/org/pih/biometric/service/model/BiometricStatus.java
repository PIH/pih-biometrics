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
public class BiometricStatus implements Serializable {

    private boolean enabled;
    private String statusMessage;
    private Integer numberEnrolled;

    public BiometricStatus() { }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Integer getNumberEnrolled() {
        return numberEnrolled;
    }

    public void setNumberEnrolled(Integer numberEnrolled) {
        this.numberEnrolled = numberEnrolled;
    }
}
