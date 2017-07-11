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
import java.util.ArrayList;
import java.util.List;

/**
 * A subject represents a Person for whom one or more biometric samples are being collected,
 * and for whom we want to associate all of these biometrics together with a single subjectId identifier
 */
public class BiometricSubject implements Serializable {

    private String subjectId;
    private List<Fingerprint> fingerprints;

    public BiometricSubject() { }

    public BiometricSubject(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public List<Fingerprint> getFingerprints() {
        if (fingerprints == null) {
            fingerprints = new ArrayList<Fingerprint>();
        }
        return fingerprints;
    }

    public void setFingerprints(List<Fingerprint> fingerprints) {
        this.fingerprints = fingerprints;
    }

    public void addFingerprint(Fingerprint fingerprint) {
        getFingerprints().add(fingerprint);
    }
}
