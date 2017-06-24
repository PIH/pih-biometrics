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

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFPosition;
import com.neurotec.biometrics.NFRecord;
import com.neurotec.biometrics.NFTemplate;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFScanner;
import com.neurotec.io.NBuffer;
import com.neurotec.lang.NObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pih.biometric.service.exception.BiometricServiceException;
import org.pih.biometric.service.exception.DeviceNotFoundException;
import org.pih.biometric.service.exception.NonUniqueDeviceException;
import org.pih.biometric.service.exception.ServiceNotEnabledException;
import org.pih.biometric.service.model.BiometricsConfig;
import org.pih.biometric.service.model.BiometricsScanner;
import org.pih.biometric.service.model.BiometricsTemplate;
import org.pih.biometric.service.model.Fingerprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Component that enables interaction with the devices for scanning and extracting biometric templates
 */
@Component
public class FingerprintScanningEngine {
	
	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
    BiometricsConfig config;

    @Autowired
    BiometricLicenseManager licenseManager;

    /**
     * Retrieves all connected Fingerprint Scanners
     */
    public List<BiometricsScanner> getFingerprintScanners() {
        log.debug("Retrieving fingerprint scanners...");
        List<BiometricsScanner> ret = new ArrayList<>();
        NBiometricClient client = null;
        obtainLicense();
        try {
            client = createBiometricClient();
            client.setUseDeviceManager(true);
            NDeviceManager deviceManager = client.getDeviceManager();
            deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
            deviceManager.initialize();
            for (NDevice device : deviceManager.getDevices()) {
                BiometricsScanner scanner = new BiometricsScanner();
                scanner.setId(device.getId());
                scanner.setDisplayName(device.getDisplayName());
                scanner.setMake(device.getMake());
                scanner.setModel(device.getModel());
                scanner.setSerialNumber(device.getSerialNumber());
                ret.add(scanner);
            }
            return ret;
        }
        finally {
            releaseLicense();
            dispose(client);
        }
    }

    /**
     * Scans a fingerprint.
     * Throws an exception if exactly one fingerprint reader is not found
     */
    public Fingerprint scanFingerprint() {
        return scanFingerprint(null, null);
    }

    /**
     * Scans a fingerprint using the given device
     */
    public Fingerprint scanFingerprint(String deviceId) {
        return scanFingerprint(deviceId, null);
    }

    /**
     * Scans a fingerprint using the given device, associating with the finger(s) of the given type
     * If deviceId is null, but only one device is found, use that device
     */
    public Fingerprint scanFingerprint(String deviceId, String type) {
        log.debug("Scanning fingerprint from device: " + deviceId);
        NBiometricClient client = null;
        NSubject subject = null;
        NFinger finger = null;
        obtainLicense();
        try {
            client = createBiometricClient();

            log.debug("Retrieving device");
            client.setUseDeviceManager(true);
            NDeviceManager deviceManager = client.getDeviceManager();
            deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
            deviceManager.initialize();
            List<NDevice> devicesFound = new ArrayList<>();
            for (NDevice device : deviceManager.getDevices()) {
                if (deviceId == null || deviceId.equals(device.getId())) {
                    devicesFound.add(device);
                }
            }
            if (devicesFound.isEmpty()) {
                throw new DeviceNotFoundException(deviceId);
            }
            if (devicesFound.size() > 1) {
                throw new NonUniqueDeviceException();
            }

            NFScanner scanner = (NFScanner) devicesFound.get(0);
            log.debug("Device found successfully: " + scanner.getId());

            client.setFingerScanner(scanner);
            subject = new NSubject();
            finger = new NFinger();
            NFPosition position = getFingerPosition(type);
            if (position != null) {
                finger.setPosition(getFingerPosition(type));
            }
            subject.getFingers().add(finger);

            log.debug("Capturing fingerprint...");
            NBiometricStatus status = client.capture(subject);
            if (status != NBiometricStatus.OK) {
                throw new BiometricServiceException("Error capturing fingerprint.  Status = " + status);
            }

            log.debug("Extracting template...");
            status = client.createTemplate(subject);
            if (status != NBiometricStatus.OK) {
                throw new BiometricServiceException("Error extracting template for fingerprint.  Status = " + status);
            }

            log.debug("Fingerprint captured successfully...");

            Fingerprint fp = new Fingerprint();
            fp.setTemplate(encode(subject.getTemplateBuffer().toByteArray()));
            fp.setImage(encode(finger.getImage().save().toByteArray()));

            return fp;
        }
        finally {
            releaseLicense();
            dispose(finger, subject, client);
        }
    }

    /**
     * Generates a single template from a List of fingerprints
     */
    public BiometricsTemplate generateTemplate(List<Fingerprint> fingerprints) {
        log.debug("Generating template for: " + fingerprints);
        NBiometricClient client = null;
        NFTemplate compositeTemplate = null;
        obtainLicense();
        try {
            String subjectId = null;
            BiometricsTemplate.Format format = null;
            client = createBiometricClient();
            compositeTemplate = new NFTemplate();

            for (Fingerprint fingerprint : fingerprints) {
                subjectId = fingerprint.getSubjectId() == null ? null : fingerprint.getSubjectId(); // Assumes all fingerprints are from the same subject
                format = fingerprint.getFormat() == null ? null : fingerprint.getFormat();  // Assumes all fingerprints are in the same format
                if (fingerprint.getTemplate() != null) {
                    NTemplate template = null;
                    try {
                        byte[] templateBytes = Base64.decodeBase64(fingerprint.getTemplate());
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
            log.debug("Created composite template containing " + compositeTemplate.getRecords().size() + " records");
            BiometricsTemplate ret = new BiometricsTemplate();
            ret.setSubjectId(subjectId);
            ret.setFormat(format);
            ret.setTemplate(encode(compositeTemplate.save().toByteArray()));
            return ret;
        }
        finally {
            releaseLicense();
            dispose(compositeTemplate, client);
        }
    }

    //***** CONVENIENCE METHODS *****

    private void obtainLicense() {
        licenseManager.obtainExtractionLicense();
        licenseManager.obtainScanningLicense();
    }

    private void releaseLicense() {
        licenseManager.releaseExtractionLicense();
        licenseManager.releaseScanningLicense();
    }

    private String encode(byte[] bytesToEncode) {
        return Base64.encodeBase64String(bytesToEncode);
    }

    /**
     * @return Biometric client, configured with appropriate properties from configuration
     */
    private NBiometricClient createBiometricClient() {
        if (!config.isFingerprintScanningEnabled()) {
            throw new ServiceNotEnabledException("Fingerprint Scanning");
        }
        NBiometricClient client = new NBiometricClient();
        client.setDatabaseConnectionToSQLite(config.getSqliteDatabasePath());
        client.setFingersTemplateSize(NTemplateSize.valueOf(config.getTemplateSize().name()));
        return client;
    }

    /**
     * @return the NFPosition that matches the given type (by enum lookup)
     */
    private NFPosition getFingerPosition(String type) {
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        return NFPosition.valueOf(type);
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