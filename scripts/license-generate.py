#!/usr/bin/python3
import json
import os
import sys
import datetime

path = os.getcwd() + "/MapboxSearch"
licenseFilePath = os.getcwd() + "/LICENSE.md"

def writeDependency(licenseFile, projectName, projectUrl, licenseName, licenseUrl):
    licenseFile.write("Mapbox Search Android uses portions of the {}.  \n".format(projectName) +
                        ("URL: [{}]({})  \n".format(projectUrl, projectUrl) if projectUrl is not None else "") +
                        "License: [{}]({})".format(licenseName, licenseUrl) +
                        "\n\n===========================================================================\n\n")

def generateLicense(licenseFile, moduleName):
    try:
        with open(path + "/{}/build/reports/licenses/licenseReleaseReport.json".format(moduleName), 'r') as dataFile:
            data = json.load(dataFile)

            uniqueProjects = set()

            for entry in data:
                projectName = entry["project"]
                if not projectName in uniqueProjects :
                    uniqueProjects.add(projectName)
                    projectUrl = entry["url"]
                    for license in entry["licenses"]:
                        licenseName = license["license"]
                        licenseUrl = license["license_url"]
                        writeDependency(licenseFile, projectName, projectUrl, licenseName, licenseUrl)

    except IOError as err:
        print("I/O error: {}".format(str(err)))

def generateLicenses():
    try:
        os.system("cd MapboxSearch && ./gradlew licenseReleaseReport --refresh-dependencies")
    except IOError as err:
        print("I/O error: {}".format(str(err)))

try:
    with open(licenseFilePath, 'w+') as licenseFile:
        generateLicenses()

        now = datetime.datetime.now()
        licenseFile.write("### License\n")
        licenseFile.write("\n")
        licenseFile.write("Mapbox Search SDK for Android version 1.0\n")
        licenseFile.write("\n")
        licenseFile.write("Mapbox Search Android SDK\n")
        licenseFile.write("\n")
        licenseFile.write("Copyright &copy; 2021 - {} Mapbox, Inc. All rights reserved.\n".format(now.year))
        licenseFile.write("\n")
        licenseFile.write("The software and files in this repository (collectively, \"Software\") are licensed under the Mapbox TOS for use only with the relevant Mapbox product(s) listed at www.mapbox.com/pricing. This license allows developers with a current active Mapbox account to use and modify the authorized portions of the Software as needed for use only with the relevant Mapbox product(s) through their Mapbox account in accordance with the Mapbox TOS.  This license terminates automatically if a developer no longer has a Mapbox account in good standing or breaches the Mapbox TOS. For the license terms, please see the Mapbox TOS at https://www.mapbox.com/legal/tos/ which incorporates the Mapbox Product Terms at www.mapbox.com/legal/service-terms.  If this Software is a SDK, modifications that change or interfere with marked portions of the code related to billing, accounting, or data collection are not authorized and the SDK sends limited de-identified location and usage data which is used in accordance with the Mapbox TOS. [Updated 2023-03]")
        licenseFile.write("\n")
        licenseFile.write("---------------------------------------\n")

        project = "Gradle License Plugin"
        url = "https://github.com/jaredsburrows/gradle-license-plugin"
        license = "The Apache Software License, Version 2.0"
        license_url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        licenseFile.write("URL: [{}]({})  \n".format(project, url) + "License: [{}]({})".format(license, license_url))

        licenseFile.write("\n\n#### Search Base module\n")
        generateLicense(licenseFile, "base")
        licenseFile.write("\n\n#### Search SDK-Common module\n")
        generateLicense(licenseFile, "sdk-common")
        licenseFile.write("\n\n#### Search Core module\n")
        generateLicense(licenseFile, "sdk")
        licenseFile.write("\n\n#### Search UI module\n")
        generateLicense(licenseFile, "ui")
        licenseFile.write("\n\n#### Search Offline module\n")
        generateLicense(licenseFile, "offline")
        licenseFile.write("\n\n#### Search Autofill module\n")
        generateLicense(licenseFile, "autofill")
        licenseFile.write("\n\n#### Search Discover module\n")
        generateLicense(licenseFile, "discover")
        licenseFile.write("\n\n#### Search Place Autocomplete module\n")
        generateLicense(licenseFile, "place-autocomplete")

    licenseFile.close()
except IOError as err:
    print("I/O error:} " + err)


