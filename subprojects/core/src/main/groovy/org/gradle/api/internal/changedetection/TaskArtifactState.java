/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.changedetection;

import org.gradle.api.tasks.TaskInputChanges;
import org.gradle.api.internal.TaskExecutionHistory;

/**
 * Encapsulates the state of the task when its outputs were last generated.
 */
public interface TaskArtifactState {
    /**
     * Returns true if the task outputs were generated using the given task inputs.
     */
    boolean isUpToDate();

    TaskInputChanges getInputChanges();

    /**
     * Called before the task is to be executed. Note that {@link #isUpToDate()} may not necessarily have been called.
     */
    void beforeTask();

    /**
     * Called on successful completion of task execution.
     */
    void afterTask();

    /**
     * Called when this state is finished with.
     */
    void finished();

    /**
     * Returns the history for this task.
     */
    TaskExecutionHistory getExecutionHistory();
}
