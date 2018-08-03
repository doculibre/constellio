package com.constellio.app.modules.rm.extensions.schema;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.extensions.behaviors.SchemaExtension;

import java.util.List;

import static java.util.Arrays.asList;

public class RMExcelReportSchemaExtension extends SchemaExtension {

	@Override
	public List<String> getAllowedSystemReservedMetadatasForExcelReport(String schemaTypeCode) {
		switch (schemaTypeCode) {
			case Folder.SCHEMA_TYPE:
				return asList(Folder.BORROW_USER_ENTERED);
		}
		return super.getAllowedSystemReservedMetadatasForExcelReport(schemaTypeCode);
	}
}
