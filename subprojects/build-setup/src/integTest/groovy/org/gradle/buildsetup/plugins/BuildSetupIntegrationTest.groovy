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

package org.gradle.buildsetup.plugins

import org.gradle.integtests.fixtures.WellBehavedPluginTest
import org.gradle.test.fixtures.file.TestFile
import org.gradle.util.GradleVersion

class BuildSetupPluginIntegrationTest extends WellBehavedPluginTest {

    @Override
    String getMainTask() {
        return "setupBuild"
    }

    @Override
    String getPluginId() {
        "build-setup"
    }

    def "can be executed without existing pom"() {
        given:
        assert !buildFile.exists()
        when:
        run 'setupBuild'
        then:
        assertFileTemplateIsValid(file("build.gradle"))
        assertFileTemplateIsValid(file("settings.gradle"))
        file("gradlew").assertExists()
        file("gradlew.bat").assertExists()
        file("gradle/wrapper/gradle-wrapper.jar").assertExists()
        file("gradle/wrapper/gradle-wrapper.properties").assertExists()
    }

    def "buildSetup is skipped on existing gradle build"() {
        given:
        assert buildFile.createFile()
        when:
        def executed = run('setupBuild')
        then:
        executed.executedTasks.contains(":setupBuild")
        executed.output.contains("Running 'setupBuild' on existing gradle build setup is not supported. Build setup skipped.")
        executed.skippedTasks.contains(":setupBuild")

        when:
        settingsFile << "include 'projA'"
        executed = run('setupBuild')
        then:
        executed.executedTasks.contains(":setupBuild")
        executed.output.contains("Running 'setupBuild' on already defined multiproject build is not supported. Build setup skipped.")
        executed.skippedTasks.contains(":setupBuild")
    }

    void assertFileTemplateIsValid(TestFile generatedFile) {
        assert generatedFile.exists()
        def generatedFileContent = generatedFile.text
        assert generatedFileContent != ""

        //validate http links in the template
        generatedFileContent.eachLine {
            (it =~ /http:\/\/[^\s]+/).each { httpRef ->
                // since we use DocumentationRegistry to create version specific files, we replace possible
                // possible gradle versions in the URL by "current". This might not be 100% safe,
                // but it works for now.
                def testHttpLink = httpRef.toString().replace(GradleVersion.current().getVersion(), "current")
                assert getResponseCode(testHttpLink) == 200
            }
        }
    }

    private static int getResponseCode(String urlString) throws MalformedURLException, IOException {
        URL u = new URL(urlString);
        HttpURLConnection huc = (HttpURLConnection) u.openConnection();
        huc.setRequestMethod("HEAD");
        huc.connect();
        return huc.getResponseCode();
    }

}
