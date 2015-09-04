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
package com.constellio.app.modules.tasks;

public class TasksEmailTemplates {
    //Templates ids
    public static final String TASK_DELETED = "taskDeleted";
    public static final String TASK_STATUS_MODIFIED = "taskStatusModified";
    public static final String TASK_ASSIGNEE_MODIFIED = "taskAssigneeModified";
    public static final String TASK_SUB_TASKS_MODIFIED = "taskSubTasksModified";
    public static final String TASK_COMPLETED = "taskTaskCompleted";
    public static final String TASK_REMINDER = "taskReminder";

    //templates parameters
    public static final String TASK_TITLE_PARAMETER = "taskTitle";
    public static final String PREVIOUS_STATUS = "previousStatus";
    public static final String ACTUAL_STATUS = "actualStatus";
    public static final String PREVIOUS_ASSIGNEE = "previousAssignee";
    public static final String ACTUAL_ASSIGNEE = "actualAssignee";

}
