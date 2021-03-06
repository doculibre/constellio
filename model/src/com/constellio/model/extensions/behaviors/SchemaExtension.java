package com.constellio.model.extensions.behaviors;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.extensions.events.schemas.PreparePhysicalDeleteFromTrashParams;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.extensions.events.schemas.SearchFieldPopulatorParams;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.frameworks.extensions.ExtensionBooleanResult.NOT_APPLICABLE;

public class SchemaExtension {

	public ExtensionBooleanResult isPutInTrashBeforePhysicalDelete(SchemaEvent event) {
		//TODO
		return NOT_APPLICABLE;
	}

	public LogicalSearchCondition getPhysicallyDeletableRecordsForSchemaType(SchemaEvent event) {
		//TODO
		return null;
	}

	public List<String> getAllowedSystemReservedMetadatasForExcelReport(String schemaTypeCode) {
		return new ArrayList<>();
	}

	public Object populateSearchField(SearchFieldPopulatorParams params) {
		return NOT_APPLICABLE;
	}

	public void preparePhysicalDeleteFromTrash(PreparePhysicalDeleteFromTrashParams params) {
	}

	public void schemaInCreationBeforeSave(SchemaInCreationBeforeSaveEvent event) {
	}

	public boolean isRecordTitleShouldBeCalculatedFromContent(Record record) {
		return true;
	}

	@AllArgsConstructor
	@Getter
	public static class SchemaInCreationBeforeSaveEvent {
		private MetadataSchemaBuilder defaultSchemaBuilder;
		private List<Language> languages;
	}
}
