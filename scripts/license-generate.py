#!/usr/bin/python
import json
import os
import sys

path = os.getcwd() + "/MapboxSearch"
licenseFilePath = os.getcwd() + "/LICENSE.md"

def writeDependency(licenseFile, projectName, projectUrl, licenseName, licenseUrl):
    licenseFile.write("Mapbox Search Android uses portions of the %s.  \n" % projectName +
                        ("URL: [%s](%s)  \n" % (projectUrl, projectUrl) if projectUrl is not None else "") +
                        "License: [%s](%s)" % (licenseName, licenseUrl) +
                        "\n\n===========================================================================\n\n")

def generateLicense(licenseFile, moduleName):
    try:
        # run gradle license generation
        os.system("cd MapboxSearch && ./gradlew %s:licenseReleaseReport" % moduleName)

        with open(path + "/%s/build/reports/licenses/licenseReleaseReport.json" % moduleName, 'r') as dataFile:
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

    except IOError as (errno,strerror):
        print "I/O error(%s): %s" % (errno, strerror)

try:
    with open(licenseFilePath, 'w+') as licenseFile:
        licenseFile.write("### License\n")
        licenseFile.write("\n")
        licenseFile.write("Mapbox Search SDK for Android version 1.0\n")
        licenseFile.write("\n")
        licenseFile.write("Mapbox Search Android SDK\n")
        licenseFile.write("\n")
        licenseFile.write("Copyright &copy; 2021 Mapbox\n")
        licenseFile.write("\n")
        licenseFile.write("All rights reserved.\n")
        licenseFile.write("\n")
        licenseFile.write("Mapbox Search SDK for Android version 1.0 (\"Mapbox Search Android SDK\") or higher must be used according to the Mapbox Terms of Service. This license allows developers with a current active Mapbox account to use and modify the Mapbox Search Android SDK. Developers may modify the Mapbox Search Android SDK code so long as the modifications do not change or interfere with marked portions of the code related to billing, accounting, and anonymized data collection. The Mapbox Search Android SDK sends anonymized location and usage data, which Mapbox uses for fixing bugs and errors, accounting, and generating aggregated anonymized statistics. This license terminates automatically if a user no longer has an active Mapbox account.\n")
        licenseFile.write("\n")
        licenseFile.write("For the full license terms, please see the Mapbox Terms of Service at https://www.mapbox.com/legal/tos/\n")
        licenseFile.write("\n")
        licenseFile.write("---------------------------------------\n")

        project = "Gradle License Plugin"
        url = "https://github.com/jaredsburrows/gradle-license-plugin"
        license = "The Apache Software License, Version 2.0"
        license_url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        licenseFile.write("URL: [%s](%s)  \n" % (project, url) + "License: [%s](%s)" % (license, license_url))

        licenseFile.write("\n\n#### Search Core module\n")
        generateLicense(licenseFile, "sdk")
        licenseFile.write("\n\n#### Search UI module\n")
        generateLicense(licenseFile, "ui")
        licenseFile.write("\n\n#### Search Autofill module\n")
        generateLicense(licenseFile, "autofill")

    licenseFile.close()
except IOError as err:
    print("I/O error:} " + err)


