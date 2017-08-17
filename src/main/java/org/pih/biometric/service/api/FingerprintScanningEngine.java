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
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFScanner;
import com.neurotec.lang.NObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pih.biometric.service.exception.BadScanException;
import org.pih.biometric.service.exception.BiometricServiceException;
import org.pih.biometric.service.exception.DeviceNotFoundException;
import org.pih.biometric.service.exception.DeviceTimeoutException;
import org.pih.biometric.service.exception.ServiceNotEnabledException;
import org.pih.biometric.service.model.BiometricConfig;
import org.pih.biometric.service.model.BiometricScanner;
import org.pih.biometric.service.model.Fingerprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Component that enables interaction with the devices for scanning and extracting biometric templates
 */
@Component
public class FingerprintScanningEngine {

    static private final Integer TIMEOUT_IN_MS = 5000;

	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
    BiometricConfig config;

    @Autowired
    BiometricLicenseManager licenseManager;

    NBiometricClient client = null;

    NDeviceManager deviceManager = null;

    @PostConstruct
    public void init() {
        if (config.isFingerprintScanningEnabled()) {
            obtainLicense();
            createBiometricClient();
            createDeviceManager();
            initializeDevice();
        }
    }

    @PreDestroy
    public void destroy() {
        if (config.isFingerprintScanningEnabled()) {
            dispose(client, deviceManager);
            releaseLicense();
        }
    }


    /**
     * Retrieves all connected Fingerprint Scanners
     */
    public synchronized List<BiometricScanner> getFingerprintScanners() {
        log.debug("Retrieving fingerprint scanners...");
        List<BiometricScanner> ret = new ArrayList<>();
        for (NDevice device : deviceManager.getDevices()) {
            BiometricScanner scanner = new BiometricScanner();
            scanner.setId(device.getId());
            scanner.setDisplayName(device.getDisplayName());
            scanner.setMake(device.getMake());
            scanner.setModel(device.getModel());
            scanner.setSerialNumber(device.getSerialNumber());
            ret.add(scanner);
        }
        return ret;
    }

    /**
     * Scans a fingerprint.
     */
    public Fingerprint scanFingerprint() {
        return scanFingerprint(null);
    }


    /**
     * Scans a fingerprint using the given device, associating with the finger(s) of the given type
     */
    public synchronized Fingerprint scanFingerprint(String type) {

        if (!config.isFingerprintScanningEnabled()) {
            throw new ServiceNotEnabledException("Fingerprint Scanning");
        }

        // if there's no device attached, try to find it, but if still no device fail
        if (client.getFingerScanner() == null) {
            initializeDevice();
        }
        if (client.getFingerScanner() == null) {
            throw new DeviceNotFoundException();
        }

        log.debug("Scanning fingerprint from device");

        NSubject subject = null;
        NFinger finger = null;

        try {
            subject = new NSubject();
            finger = new NFinger();
            NFPosition position = getFingerPosition(type);
            if (position != null) {
                finger.setPosition(getFingerPosition(type));
            }
            subject.getFingers().add(finger);

            log.debug("Capturing fingerprint...");

            NBiometricStatus status;

            status = client.capture(subject);

            if (status == NBiometricStatus.OK) {
                log.debug("Extracting template...");
                status = client.createTemplate(subject);
                if (status != NBiometricStatus.OK) {
                    throw new BadScanException("Error extracting template for fingerprint.  Status = " + status);
                }

                log.debug("Fingerprint captured successfully...");

                Fingerprint fp = new Fingerprint();
                fp.setTemplate(encode(subject.getTemplateBuffer().toByteArray()));
                fp.setImage(encode(finger.getImage().save().toByteArray()));

                return fp;
            }
            else if (status == NBiometricStatus.TIMEOUT) {
                throw new DeviceTimeoutException();
            }
            else {
                throw new BadScanException("Error capturing fingerprint.  Status = " + status);
            }

        }
        catch (DeviceTimeoutException e) {
            throw e;
        }
        catch (BadScanException e) {
            throw e;
        }
        catch (Exception e) {
            client.setFingerScanner(null);
            throw new BiometricServiceException("Error capturing fingerprint:", e);

        }
        finally {
            dispose(finger, subject);
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
    private void createBiometricClient() {
        client = new NBiometricClient();
        client.setUseDeviceManager(true);
        client.setDatabaseConnectionToSQLite(config.getSqliteDatabasePath());
        client.setFingersTemplateSize(NTemplateSize.valueOf(config.getTemplateSize().name()));
        client.setTimeout(TIMEOUT_IN_MS);
    }

    private void createDeviceManager() {
        deviceManager = client.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
    }

    private void initializeDevice() {
        log.debug("Retrieving device");
        List<NDevice> devicesFound = deviceManager.getDevices();

        if (devicesFound == null || devicesFound.isEmpty()) {
            log.warn("Device not found");
            return;
        }
        if (devicesFound.size() > 1) {
            log.warn("Found multiple devices, using first found: " + devicesFound.get(0).getId());
        }
        else {
            log.debug("Device found successfully: " + devicesFound.get(0).getId());
        }

        NFScanner scanner = (NFScanner) devicesFound.get(0);
        client.setFingerScanner(scanner);
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
            log.debug("Disposing of " + o);
            if (o != null) {
                o.dispose();
            }
        }
    }
}