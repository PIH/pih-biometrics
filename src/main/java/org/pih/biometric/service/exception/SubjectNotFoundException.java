/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.pih.biometric.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Represents an exception that occurs if an attempt is made to save
 * a new template for a subject that already exists, rather than updating the existing template
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubjectNotFoundException extends BiometricServiceException {

    public SubjectNotFoundException(String subjectId) {
        super("Subject " + subjectId + " not found.");
    }
}
