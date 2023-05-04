PIH Biometrics
==================

This is a lightweight application that is built on top of the Neurotechnology VeriFinger Extended SDK.  This application is designed to serve as either a client application, a server application, or both.  When serving as a server application, this enables saving, retrieving, and matching of fingerprints.  When run as a client application, this enables communicating with a fingerprint scanner to scan and collect fingerprint images and templates.

There are 3 implementation patterns:

1. Single-server mode.  Used in situations where the same client computer (laptop) that is connected to the fingerprint scanner to collect the fingerprints is also the one that stores the fingerprint and is running the server-side fingerprint-consuming applications (eg. OpenMRS).  This would be typical on a development server or a standalone, single-user production server.

2. Client-only mode.  This is the typical configuration of a client device (laptop) that is used to collect fingerprints and to communicate this to a separate server that is running this application in server mode.  This would be the typical configuration for clients in most production or test environments.

3. Server-only mode.  This is the typical configuration for a backend server that is used to host the fingerprint data and services used for storing and matching fingerprints submitted to it, but is not directly connected to a fingerprint scanner to collect fingerprints.  This is the typical configuration on a production or test server.

This application has been designed to be [integrated with OpenMRS for fingerprint collection and identification](https://talk.openmrs.org/t/biometrics-implementation-in-openmrs/14596/4).  However, it is not dependent upon or tied to OpenMRS.  All operations (in both client and server modes) are performed via a REST API.

# Installation and Deployment

Specific instructions related to installation and deployment at PIH can be found in [Bitbucket](https://bitbucket.org/partnersinhealth/biometrics-client-package).  Please see this repository for information on how to set things up for testing or production within on PIH systems.  A playbook for setting up the server component at PIH via Ansible can also be found in [Bitbucket](https://bitbucket.org/partnersinhealth/deployment).

General instructions are as follows:

**Create installation directory**:

Create a top-level directory for the application (eg. `/opt/pih-biometrics`).  Call this `$PIH_BIOMETRICS_HOME`

**Install the application binary**:

Add a `bin` subdirectory and add the `pih-biometrics-X.Y.Z.jar` file to it.  To obtain this jar file:

1. Download specific version from [Maven](https://s01.oss.sonatype.org/index.html#nexus-search;quick~pih-biometrics)
2. Grab specific version from an existing installation/server
3. Build from source (see Developer instructions below)

**Install the Neurotechnology libraries for your operating system**:

Add a `lib` subdirectory and add the appropriate Neurotechnology libraries to it.  To obtain these, you can copy the appropriate directory from the Neurotechnology SDK (see Developer instructions below) plus any additional driver libraries as needed for specific fingerprint readers.

**Add keystore.jks if SSL support is needed**:

If the server-side configuration expect the URL to the fingerprint service to be at HTTPS, then SSL configuration must be added to the configuration.  As a part of this, you need to generate a keystore to use as a part of the configuration

**Add configuration and licenses**:

Add a `config` subdirectory, and add the following:

* Neurotechnology license file for the Fingerprint Matcher component (if needed)
* Neurotechnology license file for the Fingerprint Client component (if needed)
* application.yml file that configures the application:

The `application.yml` file supports configuration of several properties.  This configuration file can be located anywhere on one's system, the path to which is specified when starting up the application.  This supports the following options:

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

As described above, the server can be configured to support 2 modes:

* Matching Service:  If this machine will be providing the storage of fingerprints, and the matching, enrollment, and identification services
* Fingerprint Scanning:  If this machine will be connected to a fingerprint reader as a client, extracting fingerprints from subjects

One or both of these can be enabled or disabled by adjusting the `matchingServiceEnabled` and `fingerprintScanningEnabled` properties.  To ease client installation, by default these are set up to enable fingerprint scanning only.

The `matchingThreshold`, `matchingSpeed`, and `templateSize` settings are all optional.  If they are not supplied, they will receive default values (which are the same as those shown above).

If the `sqliteDatabasePath` is specified, this indicates to the server that Sqlite should be used as the underlying database, and should be stored at the given location.
**NOTE**: This database will be created if it does not yet exist
**NOTE**: Currently Sqllite is the only database supported, so this is currently a required property.

The `licenseFiles` property should include full paths to those License files that are required for the server operation (eg. Fingerprint Matcher license)

If HTTPS is not needed, the server port can be changed to 9000 and the server ssl properties can be omitted 

Log file location and log levels can be adjusted to meet the specific needs

```yaml
server:
  port: 9443
  ssl.key-store: keystore.jks
  ssl.key-store-password: <keystore-pw>
  ssl.key-password: <keystore-pw>

logging:
  file: "<absolute-path-to-where-the-log-file-should-go>"
  level:
    root: "WARN"
    org.pih: "DEBUG"

matchingServiceEnabled: "true"
fingerprintScanningEnabled: "true"

licenseFiles:
  - "absolute-path-to-license-1"
  - "absolute-path-to-license-2"
```

Add a `startup.bat`/`startup.sh` file that contains the following (or execute directly) to startup:

```shell
java \
  -Djna.library.path=$PIH_BIOMETRICS_HOME/lib \
  -jar $PIH_BIOMETRICS_HOME/bin/pih-biometrics-1.0.0-SNAPSHOT.jar \
  --spring.config.location=file:$PIH_BIOMETRICS_HOME/config/application.yml
```

# Developer Installation

## Installing the Neurotechnology SDK

In order to build this project, one needs to have the Neurotechnology SDK installed in order to use it to install the library dependencies into the local Maven repository.  

* Download the Verifinger Extended SDK from http://www.neurotechnology.com (or other suitable source)
* Unzip this into a suitable location (eg. `/opt/Neurotec_Biometric_9_0_SDK`)
* Open a terminal, navigate into this directory, and run `mvn clean install` to install the appropriate jars

If one has the Neurotechnology SDK installed, one can also use it as the source of the libraries needed to run the application.  These libraries are contained in the `Lib` subdirectory, divided by operating system.  To use these, you would simply need to ensure that the `-Djna.library.path` points to the appropriate subdirectory when the application is started.  This is also required to build and test the application.  For example:  `-Djna.library.path=/opt/Neurotec_Biometric_9_0_SDK/Lib/Linux_x86_64`

### Building from source

To build from source, one first needs to follow the steps above under `Installing the Neurotechnology SDK`.  Once this is complete, building requires the following steps:

* Create a directory at `~/.pih-biometrics`, and add valid Neurotechnology license files to this directory
* Build with Maven, setting the `jna.library.path` as appropriate per the above step.

```mvn clean install -Djna.library.path=/opt/Neurotec_Biometric_9_0_SDK/Lib/Linux_x86_64/```

# Running the Server

After building the project, the server can be run via the jar file.  It can also be run through the IDE.

**Jar File execution**:

* Must include the jna.library.path explicitly, as specified above.
* Must pass in the location of the yaml configuration file, as specified above.

Example:
```
java -Djna.library.path=/opt/Neurotec_Biometric_9_0_SDK/Lib/Linux_x86_64 -jar target/pih-biometrics-1.0.0-SNAPSHOT.jar --spring.config.location=file:/opt/pih-biometrics/config/application.yml
```

**Intellij execution**:

* Run the BiometricService class
* As a "VM Option", specify the path to the native libraries as above (eg. `-Djna.library.path=/opt/Neurotec_Biometric_9_0_SDK/Lib/Linux_x86_64`)
* As a "Program Argument", specify the path to the configuration folder as above (eg. `--spring.config.location=file:/opt/pih-biometrics/config/application.yml`)

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

# TODO

* Determine if we want to assign UUIDs as our main means of subjectId generation.  Do we want to allow specifying subjectId by the user?
* Determine if we want to allow multiple templates per subject, or to expose additional methods to add/delete/update various modalities.
* Determine if we want to associate additional demographic data to allow for faster queries, or if this can come later as needed
* Potentially support additional database engines (eg. MySQL)
* Determine if the template format should be the responsibility of the server, or should it simply store and match what it is given
* Should information around the template format be stored along with the template?
* Should we simply support importing/exporting in ISO format as needed for integration?
