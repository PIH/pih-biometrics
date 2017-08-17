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
import org.pih.biometric.service.model.BiometricMatch;
import org.pih.biometric.service.model.BiometricSubject;
import org.pih.biometric.service.model.Fingerprint;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * We ignore this test for now.  Ideally we would get proper mocking of the Neurotechnology
 * devices and scanning set up, but for now we run this manually on demand to sanity check things.
 * Will need to set the mock config to enable fingerprint scanning (enabled this currently causes a core dump involving a usb library, assumedly, because it doesn't have any USB ports)
 **/
@Ignore
public class BiometricScanningEngineTest extends BaseBiometricTest {

    @Autowired
    FingerprintScanningEngine scanningEngine;

    int numScans = 2;

    @Test
    public void doTest() throws Exception {

        for (int i=0; i<numScans; i++) {
            BiometricSubject subject = new BiometricSubject();
            subject.addFingerprint(scanningEngine.scanFingerprint());
            BiometricSubject savedFp1 = matchingEngine.enroll(subject);
            System.out.println("Created: " + savedFp1.getSubjectId());
        }

        System.out.println("Number Enrolled: " + matchingEngine.getNumberEnrolled());

        BiometricSubject subject = new BiometricSubject();
        subject.addFingerprint(scanningEngine.scanFingerprint());
        List<BiometricMatch> matches = matchingEngine.identify(subject);
        System.out.println("Matched: " + matches);
    }

    @Test
    public void doCompositeTest() throws Exception {

        Fingerprint scan1 = scanningEngine.scanFingerprint();
        Fingerprint scan2 = scanningEngine.scanFingerprint();
        BiometricSubject composite = new BiometricSubject();
        composite.addFingerprint(scan1);
        composite.addFingerprint(scan2);
        matchingEngine.enroll(composite);

        System.out.println("Number Enrolled: " + matchingEngine.getNumberEnrolled());

        BiometricSubject id1 = new BiometricSubject();
        id1.addFingerprint(scan1);
        List<BiometricMatch> matches1 = matchingEngine.identify(id1);
        System.out.println("Matched scan 1: " + matches1);

        BiometricSubject id2 = new BiometricSubject();
        id2.addFingerprint(scan2);
        List<BiometricMatch> matches2 = matchingEngine.identify(id2);
        System.out.println("Matched scan 2: " + matches2);
    }
}
