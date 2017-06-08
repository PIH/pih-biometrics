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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pih.biometric.service.model.BiometricsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * This is the main class that starts up the application.
 * The Configuration, EnableConfigurationProperties, and ConfigurationProperties annotations work to
 * enable the use of an application.properties or application.yml file to pass in configuration to the application.
 * There is good documentation on how we can set this up to have overrides in various environments here:
 * http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
 *
 * The intention is to provide appropriate defaults for properties where it makes sense within the embedded application.yml
 * Runtime values should be passed in via external configuration files when the application starts up
 */
@SpringBootApplication
@EnableConfigurationProperties
@Configuration
public class BiometricService {

    private static final Log log = LogFactory.getLog(BiometricService.class);

    @ConfigurationProperties
    @Bean
    public BiometricsConfig getConfig() {
        return new BiometricsConfig();
    }

    /**
     * Run the application
     */
	public static void main(String[] args) {

        log.info("Starting up Fingerprint Server");

        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        log.info("JAVA VM: " + runtimeMxBean.getVmName());
        log.info("JAVA VENDOR: " + runtimeMxBean.getSpecVendor());
        log.info("JAVA VERSION: " + runtimeMxBean.getSpecVersion() + " (" + runtimeMxBean.getVmVersion() + ")");
        log.info("JAVA_OPTS: " + runtimeMxBean.getInputArguments());

        SpringApplication.run(BiometricService.class, args);
	}
}
