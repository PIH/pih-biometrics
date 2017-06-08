/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.pih.biometric.service.api;

import com.neurotec.lang.NCore;
import com.neurotec.licensing.NLicense;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pih.biometric.service.exception.BiometricServiceException;
import org.pih.biometric.service.model.BiometricsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

/**
 * Component that obtains and releases licenses for particular biometric components
 */
@Component
public class BiometricLicenseManager {
	
	protected final Log log = LogFactory.getLog(this.getClass());

    public static final String FINGER_MATCHING_COMPONENT = "Biometrics.FingerMatching";
    public static final String FINGER_EXTRACTION_COMPONENT = "Biometrics.FingerExtraction,Devices.FingerScanners";

	@Autowired
    BiometricsConfig config;

    /**
     * On startup, we ensure licenses are appropriately added and the fingerprint server is available
     */
    @PostConstruct
    public void startup() {
        log.info("Initializing Biometric Licenses");
        addLicenses();
    }

    @PreDestroy
    public void cleanup() {
        log.info("Neurotechnology Core shutting down.");
        NCore.shutdown();
    }

    /**
     * Neurotechnology requires a license, which may be provided via license files
     * Any configured license files will be attempted to be loaded
     */
    public void addLicenses() {
        if (config.getLicenseFiles() == null || config.getLicenseFiles().isEmpty()) {
            log.debug("No license files configured to load.");
        }
        else {
            log.debug("Number of License Files configured: " + config.getLicenseFiles().size());
            for (File licenseFile : config.getLicenseFiles()) {
                try {
                    String licenseContent = FileUtils.readFileToString(licenseFile, "UTF-8");
                    NLicense.add(licenseContent);
                    log.debug("Added license: " + licenseFile.getName());
                }
                catch (Exception e) {
                    throw new BiometricServiceException("An error occurred while activating license from file: " + licenseFile, e);
                }
            }

        }
    }

    /**
     * Obtain matching license
     */
    protected void obtainMatchingLicense() {
        obtainLicense(FINGER_MATCHING_COMPONENT);
    }

    /**
     * Obtain extraction license
     */
    protected void obtainExtractionLicense() {
        obtainLicense(FINGER_EXTRACTION_COMPONENT);
    }

    /**
     * Release matching license
     */
    protected void releaseMatchingLicense() {
        releaseLicense(FINGER_MATCHING_COMPONENT);
    }

    /**
     * Release extraction license
     */
    protected void releaseExtractionLicense() {
        releaseLicense(FINGER_EXTRACTION_COMPONENT);
    }

    /**
     * Before operations requiring use of the Neurotechnology components, one must obtain a license for the particular component
     */
    protected void obtainLicense(String component) {
        log.debug("Obtaining license for component: " + component);
        try {
            if (!NLicense.obtainComponents("/local", 5000, component)) {
                throw new BiometricServiceException("Unable to obtain a license for " + component);
            }
        }
        catch (IOException e) {
            throw new BiometricServiceException("Unable to obtain a license for " + component, e);
        }
    }

    /**
     * After operations requiring use of the Neurotechnology components, one must release the license for the particular component
     */
    protected void releaseLicense(String component) {
        log.debug("Releasing license for component: " + component);
        try {
            NLicense.releaseComponents(component);
            log.debug("License released...");
        }
        catch (Exception e) {
            throw new BiometricServiceException("An error occurred while releasing " + component + " license", e);
        }
    }
}