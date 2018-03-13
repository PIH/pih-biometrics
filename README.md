PIH Biometrics
==================

This is a lightweight application that exposes REST web services for all core biometric operations.
This utilizes the Neurotechnology VeriFinger SDK and currently is geared towards supporting fingerprints.

# Overview

An overview of our current Biometrics design can be found here:

https://talk.openmrs.org/t/biometrics-implementation-in-openmrs/14596/4


# Developer Installation

* Ensure you have the VeriFinger Extended SDK installed
  * Option 1:
    * http://www.neurotechnology.com/cgi-bin/support.cgi#pu
    * Enter account password
    * Click the download link for "VeriFinger 9.0 Standard / Extended SDK"
  * Option 2
    * Download from Bamboo at /opt/Neurotec_Biometric_9_0_SDK.zip
  * Unzip this into a the desired location on your computer (for further reference here, we will refer to that as _NEUROTECHNOLOGY_SDK_HOME_)

* Ensure you have the Neurotechnology jars available in your local .m2 repository
  * Open a terminal, navigate to _NEUROTECHNOLOGY_SDK_HOME_, and run ```mvn clean install```

* Ensure that the Neurotechnology native libraries are available when building the project
  * Edit your .m2/settings.xml file, and ensure you have an active profile defined.
  * Within the active profile, add a new parameter named _neurotechnology-jna-library-path_ with a value of the path to _NEUROTECHNOLOGY_SDK_HOME_/Lib/your/os/version.
  For example:
  ```xml
  <profiles>
    <profile>
      <properties>
        <neurotechnology-jna-library-path>/opt/Neurotec_Biometric_9_0_SDK/Lib/Linux_x86_64</neurotechnology-jna-library-path>
      </properties>
    </profile>
  </profiles>
  ```
  For more information, see: https://maven.apache.org/examples/injecting-properties-via-settings.html

* Ensure you have a valid license for the "Fingerprint Matcher" component.  (You probably also need the "Fingerprint Client" component, for ISO extraction support)
  * Purchase a new license if necessary at the Neurotechnolgy website (http://www.neurotechnology.com/cgi-bin/order.cgi)
  * If you have purchased an Internet License (*_internet_license_*.*.lic), copy this license file to a new directory at ${user.home}/.pih-biometrics

* Build the project
  * From the root of this repository, run ```mvn clean install```
 

# Server Configuration

The server supports configuration of several properties that are defined in a yml file.  
This configuration file can be located anywhere on one's system, and supports the following options:

```yaml
matchingServiceEnabled: "false"
fingerprintScanningEnabled: "true"
matchingThreshold:  72,
matchingSpeed:  "LOW",
templateSize: "LARGE",
sqliteDatabasePath: "/opt/pih-biometrics/data/biometrics.db"
licenseFiles:
  - "/opt/pih-biometrics/licenses/Zanmi_Lasante_internet_license_12312665236124965265.lic"
```

The server can be configured to support 2 modes:
* Matching Service:  If this machine will be providing the storage of fingerprints, and the matching, enrollment, and identification services
* Fingerprint Scanning:  If this machine will be connected to a fingerprint reader as a client, extracting fingerprints from subjects

One or both of these can be enabled or disabled by adjusting the _matchingServiceEnabled_ and _fingerprintScanningEnabled_ properties.
To ease client installation, by default these are set up to enable fingerprint scanning only.

The _matchingThreshold_, _matchingSpeed_, and _templateSize_ settings are all optional.  If they are not supplied, they will receive default values (which are the same as those shown above).

If the _sqliteDatabasePath_ is specified, this indicates to the server that Sqlite should be used as the underlying database, and should be stored at the given location.
**NOTE**: This database will be created if it does not yet exist
**NOTE**: Currently Sqllite is the only database supported, so this is currently a required property.

The _licenseFiles_ property should include full paths to those License files that are required for the server operation (eg. Fingerprint Matcher license)


# Running the Server

After building the project, the server can be run via the jar file.  It can also be run through the IDE.

**Jar File execution**:

* Must include the jna.library.path explicitly (use the same values as in Developer Installation step 3 above)
* Must pass in the location of the yaml configuration file, as specified above.

Example:
```
java -Djna.library.path=/opt/Neurotec_Biometric_9_0_SDK/Lib/Linux_x86_64 -jar target/pih-biometrics-1.0.0-SNAPSHOT.jar --spring.config.location=file:/opt/pih-biometrics/config/application.yml
```

**Intellij execution**:

* Run the BiometricService class
* As a "VM Option", specify the path to the native libraries as above (eg. -Djna.library.path=/opt/Neurotec_Biometric_9_0_SDK/Lib/Linux_x86_64)
* As a "Program Argument", specify the path to the configuration folder as above (eg. --spring.config.location=file:/opt/pih-biometrics/config/application.yml)

# Usage

Once successfully configured and started, one should be able to interact with the server via REST:

## Biometric Services

**Return information on whether biometrics are enabled, which engine is configured, and how many subjects are enrolled**
* GET /status

**Enroll biometrics for a subject and manage existing biometrics for a subject**
* GET /template/{subjectId}
* POST /template (create a template)
* PUT /template (create or update a template)
* DELETE /template/{subjectId}

**Match a biometric template with the library of existing saved templates**
* GET/POST /match?template={template}

These services operate on the following object representations:

**status**:
Provides information about the running system.  The _config_ property contains all of the active properties from the application.yml configuration:
```json
{
  "status":  "User-friendly status message or information about any problems",
  "numberEnrolled": 123,
  "config": { }
}
```

**template**:
Represents the biometric template for a particular subject
```json
{
  "subjectId":  "unique-id-of-the-subject",
  "template":  "text-representation-of-the-template-usually-base-64-encoded-binary-data"
}
```

**match**:
Represents a query to retrieve potential matches for a given template
```json
{
  "subjectId":  "unique-id-of-the-subject-matched",
  "matchScore":  "number-indicating-strength-of-the-match"
}
```

## Fingerprint Scanning Services

**Return information on the connected devices**
* GET /fingerprint/devices

**Scan a fingerprint from a device if exactly one fingerprint scanner is available**
* GET /fingerprint/scan

**Scan a fingerprint from a specific device by device id**
** TODO: currently not implemented--engine always uses "first" scanner it finds if multiple present **
* GET /fingerprint/scan?deviceId={deviceId}

These services operate on the following object representations:

**device**:
A connected fingerprint scanner that has been identified.  The devices endpoint returns a list of these.
```json
{
  "id":  "Unique identifier for this scanner",
  "displayName": "User-friendly display name",
  "make": "Make of the scanner",
  "model": "Model of the scanner",
  "serialNumber": "Serial number of the scanner"
}
```

**fingerprint**:
Represents the scanned fingerprint retrieved from the scan endpoint.
```json
{
  "subjectId":  "generally will be null when performing a new scan",
  "template":  "text-representation-of-the-template-as-base-64-encoded-binary-data",
  "image":  "text-representation-of-the-fingerprint-image-as-base-64-encoded-binary-data"
}
```

# Deployment

For deploying, we have created a "biometric-client-package" that can be installed on client machines, and have
written an Ansible playbook for setting up the server component. These should be used when setting up clients and
servers.  Details can be found in the README of the the "biometric-client-package" on Bitbucket:

https://bitbucket.org/partnersinhealth/biometrics-client-package

# TODO

* Determine if we want to assign UUIDs as our main means of subjectId generation.  Do we want to allow specifying subjectId by the user?
* Determine if we want to allow multiple templates per subject, or to expose additional methods to add/delete/update various modalities.
* Determine if we want to associate additional demographic data to allow for faster queries, or if this can come later as needed
* Potentially support additional database engines (eg. MySQL)
* Determine if the template format should be the responsibility of the server, or should it simply store and match what it is given
* Should information around the template format be stored along with the template?
* Should we simply support importing/exporting in ISO format as needed for integration?
