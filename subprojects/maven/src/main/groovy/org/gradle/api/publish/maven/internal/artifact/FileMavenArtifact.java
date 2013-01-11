/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.api.publish.maven.internal.artifact;

import org.gradle.api.publish.maven.MavenArtifact;

import java.io.File;

public class FileMavenArtifact implements MavenArtifact {
    private String extension;
    private String classifier;
    private File file;

    public FileMavenArtifact(File file, String extension, String classifier) {
        this.extension = extension;
        this.classifier = classifier;
        this.file = file;
    }

    public String getExtension() {
        return extension;
    }

    public String getClassifier() {
        return classifier;
    }

    public File getFile() {
        return file;
    }
}