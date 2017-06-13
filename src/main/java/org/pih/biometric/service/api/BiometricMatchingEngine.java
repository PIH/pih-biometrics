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

import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NMatchingSpeed;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.standards.CBEFFBDBFormatIdentifiers;
import com.neurotec.biometrics.standards.CBEFFBiometricOrganizations;
import com.neurotec.biometrics.standards.FMRecord;
import com.neurotec.io.NBuffer;
import com.neurotec.lang.NObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pih.biometric.service.exception.BiometricServiceException;
import org.pih.biometric.service.exception.DuplicateSubjectException;
import org.pih.biometric.service.exception.ServiceNotEnabledException;
import org.pih.biometric.service.model.BiometricsConfig;
import org.pih.biometric.service.model.BiometricsMatch;
import org.pih.biometric.service.model.BiometricsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Component that enables interaction with the biometric matching service, including enrollment, matching, and retrieval of templates
 */
@Component
public class BiometricMatchingEngine {
	
	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
    BiometricsConfig config;

    @Autowired
    BiometricLicenseManager licenseManager;

    /**
     * On startup, we ensure licenses are appropriately added and the server is available
     */
    @PostConstruct
    public void startup() {
        initializeDatabase();
    }

    /**
     * Saves a biometrics template
     */
    public <T extends BiometricsTemplate> T enroll(T template) {
        log.debug("Enrolling template for " + template.getSubjectId());

        NBiometricClient client = null;
        NSubject subject = null;
        NBiometricTask task = null;

        if (template.getSubjectId() == null) {
            template.setSubjectId(UUID.randomUUID().toString()); // Setting subject id as a random uuid
        }

        if (template.getTemplate() == null) {
            throw new BiometricServiceException("Unable to enroll biometrics since template is missing");
        }

        obtainLicense();
        try {
            client = createBiometricClient();
            subject = createSubject(template);
            task = client.createTask(EnumSet.of(NBiometricOperation.ENROLL), subject);
            client.performTask(task);

            // Check the result and handle errors if they occur
            if (task.getStatus() != NBiometricStatus.OK) {
                if (task.getStatus() == NBiometricStatus.DUPLICATE_ID) {
                    throw new DuplicateSubjectException(template.getSubjectId());
                }
                else {
                    throw new BiometricServiceException("Unable to save the template. Status: " + task.getStatus(), task.getError());
                }
            }

            log.debug("Template saved successfully for " + template.getSubjectId());
        }
        finally {
            releaseLicense();
            dispose(task, subject, client);
        }

        return template;
    }

    /**
     * Updates a biometrics template
     */
    public <T extends BiometricsTemplate> T update(T template) {
        log.debug("Updating template for " + template.getSubjectId());

        NBiometricClient client = null;
        NSubject subject = null;
        NBiometricTask task = null;

        if (template.getSubjectId() == null) {
            throw new BiometricServiceException("Unable to update template as subjectId is missing");
        }

        if (template.getTemplate() == null) {
            throw new BiometricServiceException("Unable to update template since template is missing");
        }

        obtainLicense();
        try {
            client = createBiometricClient();
            subject = createSubject(template);
            task = client.createTask(EnumSet.of(NBiometricOperation.UPDATE), subject);
            client.performTask(task);

            // Check the result and handle errors if they occur
            if (task.getStatus() != NBiometricStatus.OK) {
                throw new BiometricServiceException("Unable to save the template. Status: " + task.getStatus(), task.getError());
            }

            log.debug("Template saved successfully for " + template.getSubjectId());
        }
        finally {
            releaseLicense();
            dispose(task, subject, client);
        }

        return template;
    }

    /**
     * @return a List of BiometricsMatch that match the given sourceTemplate, along with information on the match quality
     */
    public List<BiometricsMatch> identify(BiometricsTemplate sourceTemplate) {
        List<BiometricsMatch> ret = new ArrayList<BiometricsMatch>();

        log.debug("Identifying Matches for source template...");

        NBiometricClient client = null;
        NSubject subject = null;

        obtainLicense();
        try {
            client = createBiometricClient();
            subject = createSubject(sourceTemplate);
            NBiometricStatus status = client.identify(subject);

            if (status == NBiometricStatus.OK) {
                log.debug("Found " + subject.getMatchingResults().size() + " possible matches");
                for (NMatchingResult result : subject.getMatchingResults()) {
                    ret.add(new BiometricsMatch(result.getId(), result.getScore()));
                }
            }
            else if (status == NBiometricStatus.MATCH_NOT_FOUND) {
                log.debug("No match found");
            }
            else {
                log.warn("Identification failed. Status: " + status);
            }
        }
        finally {
            releaseLicense();
            dispose(subject, client);
        }

        return ret;
    }

    /**
     * @return a count of all biometrics enrolled in the system
     */
    public Integer getNumberEnrolled() {
        NBiometricClient client = null;
        obtainLicense();
        try {
            client = createBiometricClient();
            return client.getCount();
        }
        finally {
            releaseLicense();
            dispose(client);
        }
    }

    /**
     * @return the biometric template for the given subjectId with the default Neurotechnology format
     */
    public BiometricsTemplate getTemplate(String subjectId) {
        return getTemplate(subjectId, BiometricsTemplate.Format.NEUROTECHNOLOGY);
    }

    /**
     * @return the biometric template for the given subjectId with the specified format.
     * If format is null, it defaults to the Neurotechnology proprietary format
     */
    public BiometricsTemplate getTemplate(String subjectId, BiometricsTemplate.Format format) {
        log.debug("Retrieving template for subject " + subjectId);

        NBiometricClient client = null;
        NSubject subject = null;

        obtainLicense();
        try {
            client = createBiometricClient();
            subject = createSubject(new BiometricsTemplate(subjectId, format,null));
            NBiometricStatus status = client.get(subject);

            format = (format == null ? BiometricsTemplate.Format.NEUROTECHNOLOGY : format);

            if (status == NBiometricStatus.OK) {
                log.debug("Found subject " + subjectId + ", extracting template in format: " + format);

                byte[] templateBytes = subject.getTemplateBuffer().toByteArray();

                // Extracting a template in a format other than the default requires an extraction license
                if (format != null && format != BiometricsTemplate.Format.NEUROTECHNOLOGY) {
                    try {
                        licenseManager.obtainExtractionLicense();
                        if (format == BiometricsTemplate.Format.ISO) {
                            templateBytes = subject.getTemplateBuffer(
                                    CBEFFBiometricOrganizations.ISO_IEC_JTC_1_SC_37_BIOMETRICS,
                                    CBEFFBDBFormatIdentifiers.ISO_IEC_JTC_1_SC_37_BIOMETRICS_FINGER_MINUTIAE_RECORD_FORMAT,
                                    FMRecord.VERSION_ISO_CURRENT).toByteArray();
                        }
                        else if (format == BiometricsTemplate.Format.ANSI) {
                            templateBytes = subject.getTemplateBuffer(
                                    CBEFFBiometricOrganizations.INCITS_TC_M1_BIOMETRICS,
                                    CBEFFBDBFormatIdentifiers.INCITS_TC_M1_BIOMETRICS_FINGER_MINUTIAE_U,
                                    FMRecord.VERSION_ANSI_CURRENT).toByteArray();
                        }
                        else {
                            throw new BiometricServiceException("Unable to handle extract template in format: " + format);
                        }
                    }
                    finally {
                        licenseManager.releaseExtractionLicense();
                    }
                }

                return new BiometricsTemplate(subject.getId(), format, Base64.encodeBase64String(templateBytes));
            }
            else if (status != NBiometricStatus.ID_NOT_FOUND) {
                throw new BiometricServiceException("An error occurred while looking up biometrics for subject. Status: " + status);
            }
            else {
                log.debug("No saved biometrics found for subject: " + subjectId);
            }
        }
        finally {
            releaseLicense();
            dispose(subject, client);
        }

        return null;
    }

    /**
     * Deletes the template associated with the given subjectId
     */
    public void deleteTemplate(String subjectId) {
        log.debug("Deleting template for subject " + subjectId);

        NBiometricClient client = null;

        obtainLicense();
        try {
            client = createBiometricClient();
            NBiometricStatus status = client.delete(subjectId);

            if (status != NBiometricStatus.OK) {
                throw new BiometricServiceException("An error occurred while deleting the template for subject " + subjectId + ". Status: " + status);
            }

            log.debug("No saved biometrics found for subject: " + subjectId);
        }
        finally {
            releaseLicense();
            dispose(client);
        }
    }

    //***** CONVENIENCE METHODS *****

    private void obtainLicense() {
        licenseManager.obtainMatchingLicense();
    }

    private void releaseLicense() {
        licenseManager.releaseMatchingLicense();
    }

    private void initializeDatabase() {
        if (!StringUtils.isEmpty(config.getSqliteDatabasePath())) {
            File sqlDb = new File(config.getSqliteDatabasePath());
            if (!sqlDb.exists()) {
                try {
                    sqlDb.getParentFile().mkdirs();
                    sqlDb.createNewFile();
                }
                catch (Exception e) {
                    throw new BiometricServiceException("Unable to create database file at " + config.getSqliteDatabasePath(), e);
                }
            }
        }
    }

    /**
     * @return Biometric client, configured with appropriate properties from configuration
     */
    private NBiometricClient createBiometricClient() {
        if (!config.isMatchingServiceEnabled()) {
            throw new ServiceNotEnabledException("Biometric Enrollment, Identification, and Matching");
        }
        NBiometricClient client = new NBiometricClient();
        client.setDatabaseConnectionToSQLite(config.getSqliteDatabasePath());
        client.setMatchingThreshold(config.getMatchingThreshold());
        client.setFingersMatchingSpeed(NMatchingSpeed.valueOf(config.getMatchingSpeed().name()));
        client.setFingersTemplateSize(NTemplateSize.valueOf(config.getTemplateSize().name()));
        return client;
    }

    /**
     * @return converts a Biometrics Template to an NSubject
     */
    private NSubject createSubject(BiometricsTemplate template) {
        NSubject subject = new NSubject();
        if (template.getTemplate() != null) {
            byte[] templateBytes = Base64.decodeBase64(template.getTemplate());
            subject.setTemplateBuffer(new NBuffer(templateBytes));
        }
        if (template.getSubjectId() != null) {
            subject.setId(template.getSubjectId()); // This needs to come second, as setting template buffer resets this
        }
        return subject;
    }

    /**
     * Ensures a list of possible disposable objects are disposed of
     */
    private void dispose(NObject... objects) {
        for (NObject o : objects) {
            if (o != null) {
                o.dispose();
            }
        }
    }
}