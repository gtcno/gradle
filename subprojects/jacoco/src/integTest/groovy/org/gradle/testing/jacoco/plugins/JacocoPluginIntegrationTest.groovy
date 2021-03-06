/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.testing.jacoco.plugins

import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.junit.Test

class JacocoPluginIntegrationTest extends AbstractIntegrationSpec {

    private static final String REPORTING_BASE = "${Project.DEFAULT_BUILD_DIR_NAME}/${ReportingExtension.DEFAULT_REPORTS_DIR_NAME}"

    def setup() {
        buildFile << """
            apply plugin: "java"
            apply plugin: "jacoco"

            repositories {
                mavenCentral()
            }
            dependencies {
                testCompile 'junit:junit:4.11'
            }
        """
        createTestFiles()
    }

    @Test
    public void generatesHtmlReportOnlyAsDefault() {
        given:
        when:
        succeeds('test', 'jacocoTestReport')
        then:
        file("${REPORTING_BASE}/jacoco/test/html/index.html").exists()
        !file("${REPORTING_BASE}/jacoco/test/jacocoTestReport.xml").exists()
        !file("${REPORTING_BASE}/jacoco/test/jacocoTestReport.csv").exists()
    }

    @Test
    public void canConfigureReportsInJacocoTestReport() {
        given:
        buildFile << """
            jacocoTestReport{
                reports {
                    xml.enabled true
                    csv.enabled false
                    html.destination "\${buildDir}/jacocoHtml"
                }
            }
            """
        when:
        succeeds('test', 'jacocoTestReport')
        then:
        file("build/jacocoHtml/index.html").exists()
        file("${REPORTING_BASE}/jacoco/test/jacocoTestReport.xml").exists()
        !file("${REPORTING_BASE}/jacoco/test/jacocoTestReport.csv").exists()
    }

    @Test
    public void respectsReportingBaseDir() {
        given:
        buildFile << """
            jacocoTestReport{
                reports.xml.enabled = true
                reports.csv.enabled = true
            }
            reporting{
                baseDir = "\$buildDir/customReports"
            }"""
        when:
        succeeds('test', 'jacocoTestReport')
        then:
        file("build/customReports/jacoco/test/html/index.html").exists()
        file("build/customReports/jacoco/test/jacocoTestReport.xml").exists()
        file("build/customReports/jacoco/test/jacocoTestReport.csv").exists()
    }

    @Test
    public void canConfigureReportDirectory() {
        given:
        buildFile << """
            jacocoTestReport{
                reports.xml.enabled = true
                reports.csv.enabled = true
            }
            jacoco {
                reportsDir = new File(buildDir, "customJacocoReportDir")
            }
            """
        when:
        succeeds('test', 'jacocoTestReport')
        then:
        file("build/customJacocoReportDir/test/html/index.html").exists()
        file("build/customJacocoReportDir/test/jacocoTestReport.xml").exists()
        file("build/customJacocoReportDir/test/jacocoTestReport.csv").exists()
    }

    @Test
    public void jacocoReportIsIncremental() {
        when:
        succeeds('test', 'jacocoTestReport')
        then:
        file("${REPORTING_BASE}/jacoco/test/html/index.html").exists()

        when:
        succeeds('jacocoTestReport')
        then:
        skippedTasks.contains(":jacocoTestReport")
        file("${REPORTING_BASE}/jacoco/test/html/index.html").exists()

        when:
        file("${REPORTING_BASE}/jacoco/test/html/.resources").deleteDir()
        succeeds('test', 'jacocoTestReport')
        then:
        !skippedTasks.contains(":jacocoTestReport")
        file("${REPORTING_BASE}/jacoco/test/html/index.html").exists()
    }

    @Test
    public void jacocoTestReportIsSkippedIfNoCoverageDataAvailable() {
        when:
        def executionResult = succeeds('jacocoTestReport')
        then:
        executionResult.assertTaskSkipped(':jacocoTestReport')
    }

    @Test
    public void canUseCoverageDataFromPreviousRunForCoverageReport() {
        when:
        succeeds('jacocoTestReport')
        then:
        skippedTasks.contains(":jacocoTestReport")
        !file("${REPORTING_BASE}/jacoco/test/html/index.html").exists()

        when:
        succeeds('test')
        and:

        succeeds('jacocoTestReport')
        then:
        executedTasks.contains(":jacocoTestReport")
        file("${REPORTING_BASE}/jacoco/test/html/index.html").exists()
    }

    private void createTestFiles() {
        file("src/main/java/org/gradle/Class1.java") <<
                "package org.gradle; public class Class1 { public boolean isFoo(Object arg) { return true; } }"
        file("src/test/java/org/gradle/Class1Test.java") <<
                "package org.gradle; import org.junit.Test; public class Class1Test { @Test public void someTest() { new Class1().isFoo(\"test\"); } }"
    }
}

