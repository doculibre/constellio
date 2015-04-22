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
package com.constellio.app.ui.pages.management.schemas.display.group;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;

public class ListMetadataGroupSchemaTypePresenter extends SingleSchemaBasePresenter<ListMetadataGroupSchemaTypeView> {

	public ListMetadataGroupSchemaTypePresenter(ListMetadataGroupSchemaTypeView view) {
		super(view);
	}

	private String schemaTypeCode;

	public void setSchemaTypeCode(String schemaTypeCode) {
		this.schemaTypeCode = schemaTypeCode;
	}

	public List<String> getMetadataGroupList() {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		return new ArrayList<>(typeConfig.getMetadataGroup());
	}

	public void addGroupMetadata(String group) {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		List<String> labels = new ArrayList<>(typeConfig.getMetadataGroup());

		if (!labels.contains(group) && !group.trim().equals("")) {
			labels.add(group);
			typeConfig = typeConfig.withMetadataGroup(labels);
			schemasDisplayManager().saveType(typeConfig);
			view.refreshTable();
		} else {
			view.displayAddError();
		}
	}

	public void deleteGroupMetadata(String group) {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		List<String> labels = new ArrayList<>(typeConfig.getMetadataGroup());
		if (labels.size() > 1) {
			labels.remove(group);
			typeConfig = typeConfig.withMetadataGroup(labels);
			schemasDisplayManager().saveType(typeConfig);
			view.refreshTable();
		} else {
			view.displayDeleteError();
		}
	}

	public void backButtonClicked() {
		view.navigateTo().listSchemaType();
	}
}
