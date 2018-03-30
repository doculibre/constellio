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

import java.util.List;

import static java.util.Arrays.asList;

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


	public static List<String> getAllTypes() {
		return asList(ADMINISTRATIVE_UNIT, CATEGORY, CONTAINER_RECORD, DECOMMISSIONING_LIST, DOCUMENT, FILING_SPACE, FOLDER,
				RETENTION_RULE, STORAGE_SPACE, UNIFORM_SUBDIVISION);
	}
}
