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
package com.constellio.app.modules.tasks.ui.components.breadcrumb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbTrail;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class TaskBreadcrumbTrailPresenter implements Serializable {
	
	private String recordId;
	
	private BreadcrumbTrail breadcrumbTrail;
	
	private transient SchemaPresenterUtils folderPresenterUtils;
	
	private transient TasksSchemasRecordsServices tasksSchemasRecordsServices;

	public TaskBreadcrumbTrailPresenter(String recordId, BreadcrumbTrail breadcrumbTrail) {
		this.recordId = recordId;
		this.breadcrumbTrail = breadcrumbTrail;
		initTransientObjects();
		addBreadcrumbItems();
	}
	
	private void addBreadcrumbItems() {
		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();
		
		String currentRecordId = recordId;
		while (currentRecordId != null) {
			breadcrumbItems.add(0, new TaskBreadcrumbItem(currentRecordId));
			Task task = tasksSchemasRecordsServices.getTask(currentRecordId);
			currentRecordId = task.getParentTask();
		}
		for (BreadcrumbItem breadcrumbItem : breadcrumbItems) {
			breadcrumbTrail.addItem(breadcrumbItem);
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = breadcrumbTrail.getConstellioFactories();
		SessionContext sessionContext = breadcrumbTrail.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		folderPresenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		tasksSchemasRecordsServices = new TasksSchemasRecordsServices(collection, constellioFactories.getAppLayerFactory());
	}

	public void itemClicked(BreadcrumbItem item) {
		String taskId = ((TaskBreadcrumbItem) item).getTaskId();
		breadcrumbTrail.navigateTo().displayTask(taskId);
	}
	
	class TaskBreadcrumbItem implements BreadcrumbItem {
		
		private String itemTaskId;
		
		TaskBreadcrumbItem(String itemTaskId) {
			this.itemTaskId = itemTaskId;
		}

		public final String getTaskId() {
			return itemTaskId;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(itemTaskId);
		}

		@Override
		public boolean isEnabled() {
			boolean enabled;
			if (itemTaskId.equals(recordId)) {
				enabled = false;
			} else {
				Record record = folderPresenterUtils.getRecord(itemTaskId);
				User user = folderPresenterUtils.getCurrentUser();
				enabled = user.hasReadAccess().on(record);
			}
			return enabled;
		}
		
	}

}
