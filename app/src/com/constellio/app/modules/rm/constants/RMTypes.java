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
package com.constellio.app.modules.rm.constants;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;

public class RMTypes {

	public static final String ADMINISTRATIVE_UNIT = AdministrativeUnit.SCHEMA_TYPE;
	public static final String CATEGORY = Category.SCHEMA_TYPE;
	public static final String CONTAINER_RECORD = ContainerRecord.SCHEMA_TYPE;
	public static final String DECOMMISSIONING_LIST = DecommissioningList.SCHEMA_TYPE;
	public static final String DOCUMENT = Document.SCHEMA_TYPE;
	public static final String FILING_SPACE = FilingSpace.SCHEMA_TYPE;
	public static final String FOLDER = Folder.SCHEMA_TYPE;
	public static final String RETENTION_RULE = RetentionRule.SCHEMA_TYPE;
	public static final String STORAGE_SPACE = StorageSpace.SCHEMA_TYPE;
	public static final String UNIFORM_SUBDIVISION = UniformSubdivision.SCHEMA_TYPE;

}
