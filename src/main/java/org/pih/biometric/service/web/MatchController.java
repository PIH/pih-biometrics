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

import org.pih.biometric.service.api.BiometricMatchingEngine;
import org.pih.biometric.service.model.BiometricsMatch;
import org.pih.biometric.service.model.BiometricsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides web services for biometrics matching
 */
@RestController
@CrossOrigin
public class MatchController {

    @Autowired
    BiometricMatchingEngine engine;

    /**
     * @return matches for the given template.  This is essentially a search for a template, with resulting possible matches
     */
    @RequestMapping(method = RequestMethod.POST, value = "/match")
    @ResponseBody
    public List<BiometricsMatch> match(@RequestBody BiometricsTemplate template) {
        List<BiometricsMatch> matches = new ArrayList<>();
        if (template != null) {
            matches = engine.identify(template);
        }
        return matches;
    }
}
