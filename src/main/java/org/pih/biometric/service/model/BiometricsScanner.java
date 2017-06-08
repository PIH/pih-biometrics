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

import com.neurotec.devices.NDevice;

import java.io.Serializable;

/**
 * Simple bean to encapsulate a Device capable of extracting a Biometric template
 */
public class BiometricsScanner implements Serializable {

    private String id;
    private String displayName;
    private String make;
    private String model;
    private String serialNumber;

    public BiometricsScanner() { }

    public BiometricsScanner(NDevice device) {
        this.id = device.getId();
        this.displayName = device.getDisplayName();
        this.make = device.getMake();
        this.model = device.getModel();
        this.serialNumber = device.getSerialNumber();
    }

    @Override
    public String toString() {
        if (id != null) {
            return id;
        }
        if (displayName != null) {
            return displayName;
        }
        return super.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
