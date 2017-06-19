/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.pih.biometric.service;

import org.junit.Ignore;
import org.junit.Test;
import org.pih.biometric.service.api.FingerprintScanningEngine;
import org.pih.biometric.service.model.BiometricsMatch;
import org.pih.biometric.service.model.BiometricsTemplate;
import org.pih.biometric.service.model.Fingerprint;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * We ignore this test for now.  Ideally we would get proper mocking of the Neurotechnology
 * devices and scanning set up, but for now we run this manually on demand to sanity check things.
 */
@Ignore
public class BiometricScanningEngineTest extends BaseBiometricTest {

    @Autowired
    FingerprintScanningEngine scanningEngine;

    int numScans = 2;

    @Test
    public void doTest() throws Exception {

        for (int i=0; i<numScans; i++) {
            Fingerprint scan1 = scanningEngine.scanFingerprint();
            BiometricsTemplate savedFp1 = matchingEngine.enroll(scan1);
            System.out.println("Created: " + savedFp1.getSubjectId());
        }

        System.out.println("Number Enrolled: " + matchingEngine.getNumberEnrolled());

        Fingerprint identificationScan = scanningEngine.scanFingerprint();
        List<BiometricsMatch> matches = matchingEngine.identify(identificationScan);
        System.out.println("Matched: " + matches);
    }

    @Test
    public void doCompositeTest() throws Exception {

        Fingerprint scan1 = scanningEngine.scanFingerprint();
        Fingerprint scan2 = scanningEngine.scanFingerprint();
        BiometricsTemplate composite = scanningEngine.generateTemplate(Arrays.asList(scan1, scan2));
        matchingEngine.enroll(composite);

        System.out.println("Number Enrolled: " + matchingEngine.getNumberEnrolled());

        List<BiometricsMatch> matches1 = matchingEngine.identify(scan1);
        System.out.println("Matched scan 1: " + matches1);

        List<BiometricsMatch> matches2 = matchingEngine.identify(scan2);
        System.out.println("Matched scan 2: " + matches2);
    }
}
