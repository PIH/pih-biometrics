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

import org.pih.biometric.service.api.FingerprintScanningEngine;
import org.pih.biometric.service.model.BiometricsScanner;
import org.pih.biometric.service.model.BiometricsTemplate;
import org.pih.biometric.service.model.Fingerprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides web services for biometrics scanning
 */
@RestController
@CrossOrigin
@RequestMapping("/fingerprint")
public class FingerprintScanController {

    @Autowired
    FingerprintScanningEngine engine;

    /**
     * @return Fingerprint that is the result of a scan
     */
    @RequestMapping(method = RequestMethod.GET, value = "/devices")
    @ResponseBody
    public List<BiometricsScanner> getScanners() {
        return engine.getFingerprintScanners();
    }

    /**
     * @return Fingerprint that is the result of a scan
     */
    @RequestMapping(method = RequestMethod.GET, value = "/scan")
    @ResponseBody
    public Fingerprint scan(@RequestParam(value="deviceId", required=false) String deviceId,
                            @RequestParam(value="type", required=false) String type) {
        return engine.scanFingerprint(deviceId, type);
    }

    /**
     * @return Template that is the result of scanning one or more fingerprints
     */
    @RequestMapping(method = RequestMethod.POST, value = "/template")
    @ResponseBody
    public BiometricsTemplate generateTemplate(@RequestBody List<Fingerprint> fingerprints) {
        return engine.generateTemplate(fingerprints);
    }
}
