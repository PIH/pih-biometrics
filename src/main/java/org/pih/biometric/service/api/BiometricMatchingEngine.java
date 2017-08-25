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
import com.neurotec.biometrics.NFRecord;
import com.neurotec.biometrics.NFTemplate;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NMatchingSpeed;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
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
import org.pih.biometric.service.model.BiometricConfig;
import org.pih.biometric.service.model.BiometricMatch;
import org.pih.biometric.service.model.BiometricSubject;
import org.pih.biometric.service.model.BiometricTemplateFormat;
import org.pih.biometric.service.model.Fingerprint;
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
    BiometricConfig config;

    @Autowired
    BiometricLicenseManager licenseManager;

    /**
     * On startup, we ensure licenses are appropriately added and the server is available
     * TODO: do we want to obtain licenses on startup as well, as we now have the scanning engine do?
     */
    @PostConstruct
    public void startup() {
        initializeDatabase();
    }

    /**
     * Saves a biometrics subject
     */
    public BiometricSubject enroll(BiometricSubject biometricSubject) {
        log.debug("Enrolling subject: " + biometricSubject.getSubjectId());

        NBiometricClient client = null;
        NSubject subject = null;
        NBiometricTask task = null;

        if (biometricSubject.getSubjectId() == null) {
            biometricSubject.setSubjectId(UUID.randomUUID().toString()); // Setting subject id as a random uuid
        }

        if (biometricSubject.getFingerprints().isEmpty()) {
            throw new BiometricServiceException("Unable to enroll biometrics since subject does not contain any fingerprints");
        }

        obtainLicense();
        try {
            client = createBiometricClient();
            subject = createSubject(biometricSubject);
            task = client.createTask(EnumSet.of(NBiometricOperation.ENROLL), subject);
            client.performTask(task);

            // Check the result and handle errors if they occur
            if (task.getStatus() != NBiometricStatus.OK) {
                if (task.getStatus() == NBiometricStatus.DUPLICATE_ID) {
                    throw new DuplicateSubjectException(biometricSubject.getSubjectId());
                }
                else {
                    throw new BiometricServiceException("Unable to save the template. Status: " + task.getStatus(), task.getError());
                }
            }

            log.debug("Template saved successfully for " + biometricSubject.getSubjectId());
        }
        finally {
            releaseLicense();
            dispose(task, subject, client);
        }

        return biometricSubject;
    }

    /**
     * Updates a biometrics subject
     */
    public BiometricSubject update(BiometricSubject biometricSubject) {
        log.debug("Updating subject: " + biometricSubject.getSubjectId());

        NBiometricClient client = null;
        NSubject subject = null;
        NBiometricTask task = null;

        if (biometricSubject.getSubjectId() == null) {
            throw new BiometricServiceException("Unable to update template as subjectId is missing");
        }

        if (biometricSubject.getFingerprints().isEmpty()) {
            throw new BiometricServiceException("Unable to update template since no fingerprints are included");
        }

        obtainLicense();
        try {
            client = createBiometricClient();
            subject = createSubject(biometricSubject);
            task = client.createTask(EnumSet.of(NBiometricOperation.UPDATE), subject);
            client.performTask(task);

            // Check the result and handle errors if they occur
            if (task.getStatus() != NBiometricStatus.OK) {
                throw new BiometricServiceException("Unable to save the template. Status: " + task.getStatus(), task.getError());
            }

            log.debug("Template saved successfully for " + biometricSubject.getSubjectId());
        }
        finally {
            releaseLicense();
            dispose(task, subject, client);
        }

        return biometricSubject;
    }

    /**
     * @return a List of BiometricsMatch that match the given biometricSubject, along with information on the match quality
     */
    public List<BiometricMatch> identify(BiometricSubject biometricSubject) {
        List<BiometricMatch> ret = new ArrayList<BiometricMatch>();

        log.debug("Identifying Matches for source template...");

        NBiometricClient client = null;
        NSubject subject = null;

        obtainLicense();
        try {
            client = createBiometricClient();
            subject = createSubject(biometricSubject);
            NBiometricStatus status = client.identify(subject);

            if (status == NBiometricStatus.OK) {
                log.debug("Found " + subject.getMatchingResults().size() + " possible matches");
                for (NMatchingResult result : subject.getMatchingResults()) {
                    ret.add(new BiometricMatch(result.getId(), result.getScore()));
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
    public BiometricSubject getSubject(String subjectId) {
        return getSubject(subjectId, BiometricTemplateFormat.PROPRIETARY);
    }

    /**
     * @return the biometric template for the given subjectId with the specified format.
     * If format is null, it defaults to the Neurotechnology proprietary format
     */
    public BiometricSubject getSubject(String subjectId, BiometricTemplateFormat format) {
        log.debug("Retrieving subject: " + subjectId);
        BiometricSubject biometricSubject = null;

        NBiometricClient client = null;
        NSubject subject = null;

        obtainLicense();
        try {
            client = createBiometricClient();
            subject = createSubject(new BiometricSubject(subjectId));
            NBiometricStatus status = client.get(subject);

            format = (format == null ? BiometricTemplateFormat.PROPRIETARY : format);

            if (status == NBiometricStatus.OK) {

                biometricSubject = new BiometricSubject(subjectId);
                log.debug("Found subject " + subjectId + ", extracting overall template in format: " + format);

                if (format != BiometricTemplateFormat.PROPRIETARY) {
                    subject = convertSubjectFromFormat(subject, format);
                }

                NFTemplate fingers = subject.getTemplate().getFingers();
                if (fingers != null) {
                    for (NFRecord record : fingers.getRecords()) {
                        Fingerprint fp = new Fingerprint();
                        fp.setFormat(format);
                        if (record.getPosition() != null) {
                            fp.setType(record.getPosition().name());
                        }
                        byte[] fingerBytes = record.save().toByteArray();
                        fp.setTemplate(Base64.encodeBase64String(fingerBytes));
                        biometricSubject.addFingerprint(fp);
                    }
                }

                return biometricSubject;
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
     * // TODO: This method is currently untested.  Here for reference only
     */
    protected NSubject convertSubjectFromFormat(NSubject subject, BiometricTemplateFormat format) {
        // Extracting a template in a format other than the default requires an extraction license
        if (format != null && format != BiometricTemplateFormat.PROPRIETARY) {
            try {
                licenseManager.obtainExtractionLicense();
                if (format == BiometricTemplateFormat.ISO) {
                    subject.setTemplateBuffer(subject.getTemplateBuffer(
                            CBEFFBiometricOrganizations.ISO_IEC_JTC_1_SC_37_BIOMETRICS,
                            CBEFFBDBFormatIdentifiers.ISO_IEC_JTC_1_SC_37_BIOMETRICS_FINGER_MINUTIAE_RECORD_FORMAT,
                            FMRecord.VERSION_ISO_CURRENT));
                }
                else {
                    throw new BiometricServiceException("Unable to handle extract template in format: " + format);
                }
            }
            finally {
                licenseManager.releaseExtractionLicense();
            }
        }
        return subject;
    }

    /**
     * Deletes the subject associated with the given subjectId
     */
    public void deleteSubject(String subjectId) {
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
     * @return converts a BiometricSubject to an NSubject
     * // TODO: Unclear how the type and format should be applied here
     */
    private NSubject createSubject(BiometricSubject biometricSubject) {
        NSubject subject = new NSubject();
        NFTemplate compositeTemplate = null;
        if (!biometricSubject.getFingerprints().isEmpty()) {
            try {
                compositeTemplate = new NFTemplate();
                for (Fingerprint fp : biometricSubject.getFingerprints()) {
                    if (fp.getTemplate() != null) {
                        NTemplate template = null;
                        try {
                            byte[] templateBytes = Base64.decodeBase64(fp.getTemplate());
                            template = new NTemplate(new NBuffer(templateBytes));
                            if (template.getFingers() != null) {
                                for (NFRecord record : template.getFingers().getRecords()) {
                                    compositeTemplate.getRecords().add(record);
                                }
                            }
                        }
                        finally {
                            dispose(template);
                        }
                    }
                }
                subject.setTemplateBuffer(compositeTemplate.save());
            }
            finally {
                dispose(compositeTemplate);
            }
        }
        // This needs to come last, or it gets reset
        if (biometricSubject.getSubjectId() != null) {
            subject.setId(biometricSubject.getSubjectId());
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