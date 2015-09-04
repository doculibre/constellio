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
package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.vaadin.ui.Field;

public class ConnectorInstanceFieldFactory extends MetadataFieldFactory {
	@Override
	public Field<?> build(MetadataVO metadata) {
		if (metadata.getCode().contains(ConnectorInstance.CONNECTOR_TYPE)) {
			Field<?> field;

			boolean multivalue = metadata.isMultivalue();
			if (multivalue) {
				field = newMultipleValueField(metadata);
			} else {
				field = newSingleValueField(metadata);
			}
			// FIXME Temporary workaround for inconsistencies
			if (metadata.getJavaType() == null) {
				field = null;
			}
			if (field != null) {
				postBuild(field, metadata);
			}
			field.setReadOnly(true);
			return field;
		} else if (metadata.getCode().contains(ConnectorInstance.PROPERTIES_MAPPING)) {
			return null;
		} else if (metadata.getCode().contains(ConnectorInstance.TRAVERSAL_CODE)) {
			return null;
		} else {
			return super.build(metadata);
		}
	}
}
