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
package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.AllSchemaRecordVODataProvider;
import com.constellio.model.services.factories.ModelLayerFactory;

public class AllSchemaRecordVOContainer extends RecordVOLazyContainer {

	public AllSchemaRecordVOContainer(String schemaCode, String collection) {
		super(new AllSchemaRecordVODataProvider(schemaCode, collection));
	}

	public AllSchemaRecordVOContainer(MetadataSchemaVO schema, RecordToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory) {
		super(new AllSchemaRecordVODataProvider(schema, voBuilder, modelLayerFactory));
	}

}
