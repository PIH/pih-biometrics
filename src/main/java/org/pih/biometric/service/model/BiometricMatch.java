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
 * Represents a possible biometric match as a result of searching
 * a biometric database for matches of an input template
 * The matchScore is a numeric that indicates the strength of the match
 */
public class BiometricMatch implements Serializable {

    private String subjectId;
    private Integer matchScore;

    public BiometricMatch() { }

    public BiometricMatch(String subjectId, Integer matchScore) {
        this.subjectId = subjectId;
        this.matchScore = matchScore;
    }

    @Override
    public String toString() {
        return "Biometric Match on" + subjectId + " with score " + matchScore;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }
}
