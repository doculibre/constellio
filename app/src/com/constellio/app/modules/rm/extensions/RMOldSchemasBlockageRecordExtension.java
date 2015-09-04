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
package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationEvent;

public class RMOldSchemasBlockageRecordExtension extends RecordExtension {

	@Override
	public void recordInCreation(RecordInCreationEvent event) {
		if (event.isSchemaType(FilingSpace.SCHEMA_TYPE)) {
			throw new ImpossibleRuntimeException(
					"Creation of Filing space records is no longer possible, use administrative units instead.");
		}
	}

}
