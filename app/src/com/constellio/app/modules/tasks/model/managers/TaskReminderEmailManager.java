/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.tasks.model.managers;

import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class TaskReminderEmailManager implements StatefulService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskReminderEmailManager.class);
    static int RECORDS_BATCH = 1000;
    private static final long TWENTY_SECONDS = 20 * 1000l;
    private static final Duration DURATION_BETWEEN_EXECUTION = new Duration(TWENTY_SECONDS);
    public static final String ID = "taskReminderEmailManager";
    private final BackgroundThreadsManager backgroundThreadsManager;
    private final TasksSchemasRecordsServices taskSchemas;
    private final AppLayerFactory appLayerFactory;
    private final RecordServices recordServices;
    private final UserServices userServices;
    SearchServices searchServices;

    public TaskReminderEmailManager(AppLayerFactory appLayerFactory, String collection) {
        this.appLayerFactory = appLayerFactory;
        this.backgroundThreadsManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getBackgroundThreadsManager();
        taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
        searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
        userServices = appLayerFactory.getModelLayerFactory().newUserServices();
    }

    @Override
    public void initialize() {
        configureBackgroundThread();
    }

    void configureBackgroundThread() {

        Runnable sendEmailsAction = new Runnable() {
            @Override
            public void run() {
                generateReminderEmails();
            }
        };

        backgroundThreadsManager.configure(BackgroundThreadConfiguration
                .repeatingAction("EmailQueueManager", sendEmailsAction)
                .handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
                .executedEvery(DURATION_BETWEEN_EXECUTION));
    }

    void generateReminderEmails() {

        LogicalSearchQuery query = new LogicalSearchQuery(from(taskSchemas.userTask.schema()).where(taskSchemas.userTask.nextReminderOn()).isLessOrEqualThan(TimeProvider.getLocalDate()));
        do{
            query.setNumberOfRows(RECORDS_BATCH);
            Transaction transaction = new Transaction();
            List<Task> readyToSendTasks = taskSchemas.searchTasks(query);
            for(Task task : readyToSendTasks){
                generateReminderEmail(task, transaction);
            }
            try {
                recordServices.execute(transaction);
            } catch (RecordServicesException e) {
                LOGGER.warn("Batch not processed", e);
            }
        }while(searchServices.hasResults(query));
    }

    private void generateReminderEmail(Task task, Transaction transaction) {
        List<String> assigneeCandidates = getAssigneeCandidates(task);
        List<EmailAddress> validEmailAddresses = getValidEmailAddresses(assigneeCandidates);
        if(!validEmailAddresses.isEmpty()){
            EmailToSend emailToSend = createEmailToSend(task, validEmailAddresses);
            transaction.add(emailToSend);
        }else{
            LOGGER.warn("Task reminder not sent because no assignee candidate with valid email " + task.getTitle());
        }
        task = updateTaskReminders(task);
        transaction.add(task);
    }

    private Task updateTaskReminders(Task task) {
        List<TaskReminder> newReminders = new ArrayList<>();
        for(TaskReminder taskReminder : task.getReminders()){
            if(taskReminder.computeDate(task).isBefore(TimeProvider.getLocalDate())){
                taskReminder.setProcessed(true);
            }
            newReminders.add(taskReminder);
        }
        return task.setReminders(newReminders);
    }

    private EmailToSend createEmailToSend(Task task, List<EmailAddress> validEmailAddresses) {
        List<String> parameters = new ArrayList<>();
        parameters.add(TasksEmailTemplates.TASK_TITLE_PARAMETER + ":" + task.getTitle());
        EmailToSend emailToSend = taskSchemas.newEmailToSend()
                                    .setTemplate(TasksEmailTemplates.TASK_REMINDER)
                                    .setTo(validEmailAddresses)
                                    .setSubject($("emailToSend.subject.taskReminder"))
                                    .setParameters(parameters)
                                    .setSendOn(TimeProvider.getLocalDateTime());
        return emailToSend;
    }

    private List<EmailAddress> getValidEmailAddresses(List<String> usersIds) {
        List<EmailAddress> returnList = new ArrayList<>();
        LogicalSearchCondition condition = from(taskSchemas.userSchema()).where(Schemas.IDENTIFIER).isIn(usersIds);
        Metadata userEmailMetadata = taskSchemas.userSchema().get(User.EMAIL);
        List<Record> usersFromGroups = searchServices.search(new LogicalSearchQuery(condition).
                setReturnedMetadatas(new ReturnedMetadatasFilter(asList(Schemas.TITLE, userEmailMetadata))));
        for(Record userRecord : usersFromGroups){
            String userEmail = userRecord.get(userEmailMetadata);
            String userTitle = userRecord.get(Schemas.TITLE);
            if(!StringUtils.isBlank(userEmail)){
                returnList.add(new EmailAddress(userTitle, userEmail));
            }else{
                LOGGER.warn("User with blank email " + userTitle);
            }
        }
        return returnList;
    }

    private List<String> getAssigneeCandidates(Task task) {
        Set<String> returnSet = new HashSet<>();
        String assignee = task.getAssignee();
        if(assignee != null){
            returnSet.add(assignee);
        }
        List<String> taskAssignationUsersCandidates = task.getAssigneeUsersCandidates();
        if(taskAssignationUsersCandidates != null){
            returnSet.addAll(taskAssignationUsersCandidates);
        }
        List<String> taskAssigneeGroupsCandidates = task.getAssigneeGroupsCandidates();
        if(taskAssigneeGroupsCandidates != null && !taskAssigneeGroupsCandidates.isEmpty()){
            Metadata userGroups = taskSchemas.userSchema().getMetadata(User.GROUPS);
            LogicalSearchCondition condition = from(taskSchemas.userSchema()).where(userGroups).isContaining(taskAssigneeGroupsCandidates);
            List<Record> usersFromGroups = searchServices.search(new LogicalSearchQuery(condition).setReturnedMetadatas(new ReturnedMetadatasFilter(asList(Schemas.IDENTIFIER))));
            for(Record userRecord : usersFromGroups){
                returnSet.add(userRecord.getId());
            }
        }
        return new ArrayList<>(returnSet);
    }

    @Override
    public void close() {

    }
}
