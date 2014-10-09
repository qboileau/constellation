/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
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
package org.constellation.business;

import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.ChainProcess;
import org.constellation.engine.register.Task;
import org.constellation.engine.register.TaskParameter;
import org.constellation.scheduler.CstlSchedulerListener;
import org.constellation.scheduler.QuartzTask;
import org.constellation.scheduler.TaskState;
import org.geotoolkit.process.*;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.chain.model.Chain;

import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Christophe Mourette (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public interface IProcessBusiness {

    List<ProcessDescriptor> getChainDescriptors() throws ConstellationException;

    void createChainProcess(final Chain chain) throws ConstellationException;

    boolean deleteChainProcess(final String auth, final String code);

    ChainProcess getChainProcess(final String auth, final String code);

    Task getTask(String uuid);

    Task addTask(org.constellation.engine.register.Task task) throws ConstellationException;

    void updateTask(org.constellation.engine.register.Task task) throws ConstellationException;

    List<Task> listRunningTasks();

    /**
     * List all task for a TaskParameter id
     * @param id TaskParameter
     * @return
     */
    List<Task> listRunningTasks(Integer id);

    /**
     * Run an instantiated geotk process on scheduler only once.
     *
     * @param title
     * @param process
     * @param taskParameterId
     * @param userId
     * @throws ConstellationException
     */
    void runProcess(String title, Process process, Integer taskParameterId, Integer userId) throws ConstellationException;

    /**
     * Instantiate and run once a TaskParameter
     *
     * @param task
     * @param title
     * @param userId
     * @throws ConstellationException
     * @throws ConfigurationException
     */
    void executeTaskParameter (TaskParameter task, String title, Integer userId) throws ConstellationException, ConfigurationException;

    /**
     * Add a TaskParameter with trigger on scheduler.
     *
     * @param task
     * @param title
     * @param userId
     * @throws ConstellationException
     * @throws ConfigurationException
     */
    void scheduleTaskParameter (TaskParameter task, String title, Integer userId) throws ConstellationException, ConfigurationException;

    void removeTask(String id) throws ConstellationException;

    QuartzTask getProcessTask(String id);

    void addListenerOnRunningTasks(CstlSchedulerListener cstlSchedulerListener);

    TaskParameter addTaskParameter(TaskParameter taskParameter);
}
