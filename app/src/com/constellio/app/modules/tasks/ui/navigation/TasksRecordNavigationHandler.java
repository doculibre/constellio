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
package com.constellio.app.modules.tasks.ui.navigation;

import java.io.IOException;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.navigation.AbstractRecordNavigationHandler;

public class TasksRecordNavigationHandler extends AbstractRecordNavigationHandler {

	public TasksRecordNavigationHandler(ConstellioFactories constellioFactories) {
		super(constellioFactories);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
	}

	@Override
	public boolean isViewForSchemaTypeCode(String schemaTypeCode) {
		return Task.SCHEMA_TYPE.equals(schemaTypeCode);
	}

	@Override
	protected void navigateToView(String recordId, String schemaTypeCode) {
		if (Task.SCHEMA_TYPE.equals(schemaTypeCode)) {
			ConstellioUI.getCurrent().navigateTo().displayTask(recordId);
		} else {
			throw new UnsupportedOperationException("No navigation for schema type code " + schemaTypeCode);
		}
	}
}
