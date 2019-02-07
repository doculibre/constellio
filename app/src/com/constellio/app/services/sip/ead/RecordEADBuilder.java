package com.constellio.app.services.sip.ead;

import com.constellio.app.api.extensions.params.ConvertStructureToMapParams;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.constellio.data.utils.LangUtils.isNotEmptyValue;
import static com.constellio.model.entities.schemas.Schemas.CREATED_ON_CODE;
import static com.constellio.model.entities.schemas.Schemas.DESCRIPTION_TEXT;
import static com.constellio.model.entities.schemas.Schemas.PATH_PARTS;
import static com.constellio.model.entities.schemas.Schemas.TITLE_CODE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RecordEADBuilder {

	private static List<String> METADATAS_ALWAYS_IN_ARCHIVE_DESCRIPTION = asList("keywords",
			DESCRIPTION_TEXT.getLocalCode(), TITLE_CODE, CREATED_ON_CODE, Schemas.MODIFIED_ON.getLocalCode());

	private AppLayerFactory appLayerFactory;

	private MetadataSchemasManager metadataSchemasManager;

	private RecordServices recordServices;

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordEADBuilder.class);

	private RecordEADWriter eadXmlWriter;

	private ValidationErrors errors;

	private boolean includeRelatedMaterials = true;

	public RecordEADBuilder(AppLayerFactory appLayerFactory, ValidationErrors errors) {
		this.errors = errors;
		this.appLayerFactory = appLayerFactory;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	public boolean isIncludeRelatedMaterials() {
		return includeRelatedMaterials;
	}

	public RecordEADBuilder setIncludeRelatedMaterials(boolean includeRelatedMaterials) {
		this.includeRelatedMaterials = includeRelatedMaterials;
		return this;
	}

	private void addMetadata(Record record, Metadata metadata) {

		if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isMultivalue()) {
			writeMultivalueReferenceMetadata(record, metadata);

		} else if (metadata.getType() == MetadataValueType.REFERENCE && !metadata.isMultivalue()) {
			writeSinglevalueReferenceMetadata(record, metadata);

		} else if (metadata.getType() == MetadataValueType.STRUCTURE && metadata.isMultivalue()) {
			writeMultivalueStructureMetadata(record, metadata);

		} else if (metadata.getType() == MetadataValueType.STRUCTURE && !metadata.isMultivalue()) {
			writeSinglevalueStructureMetadata(record, metadata);

		} else if (metadata.getType() == MetadataValueType.CONTENT) {
			writeContentMetadata(record, metadata);

		} else {
			if (metadata.isMultivalue()) {
				eadXmlWriter.addMetadataWithSimpleValue(metadata, record.getValues(metadata));

			} else {
				eadXmlWriter.addMetadataWithSimpleValue(metadata, record.get(metadata));
			}
		}

	}

	private void writeContentMetadata(Record record, Metadata metadata) {


		List<Map<String, Object>> tableRows = new ArrayList<>();
		for (Content content : record.<Content>getValues(metadata)) {

			for (ContentVersion contentVersion : content.getVersions()) {
				tableRows.add(newContentVersionTableRow(contentVersion));
			}

			if (content.getCurrentCheckedOutVersion() != null) {
				Map<String, Object> row = newContentVersionTableRow(content.getCurrentCheckedOutVersion());
				row.put("checkedOutDate", content.getCheckoutDateTime());
				row.put("checkedOutByUser", content.getCheckoutUserId());
				row.put("version", null);
				tableRows.add(row);
			}
		}

		eadXmlWriter.addMetadataEADTable(metadata, tableRows);
	}

	private void writeSinglevalueStructureMetadata(Record record, Metadata metadata) {

		ModifiableStructure modifiableStructure = record.get(metadata);
		if (modifiableStructure != null) {
			Map<String, Object> infos = convertModifiableStructureToMap(record.getCollection(), metadata, modifiableStructure);
			eadXmlWriter.addMetadataEADDefList(metadata, infos);
		}

	}


	private void writeMultivalueStructureMetadata(Record record, Metadata metadata) {

		List<Map<String, Object>> tableRows = new ArrayList<>();
		for (ModifiableStructure modifiableStructure : record.<ModifiableStructure>getValues(metadata)) {
			Map<String, Object> tableRow = convertModifiableStructureToMap(record.getCollection(), metadata, modifiableStructure);
			tableRows.add(tableRow);
		}

		eadXmlWriter.addMetadataEADTable(metadata, tableRows);

	}


	private Map<String, Object> convertModifiableStructureToMap(String collection,
																Metadata metadata,
																ModifiableStructure modifiableStructure) {

		Map<String, Object> mappedStructure = appLayerFactory.getExtensions().forCollection(collection).convertStructureToMap(
				new ConvertStructureToMapParams(modifiableStructure, metadata));

		if (mappedStructure == null) {

			if (modifiableStructure instanceof MapStringStringStructure) {
				mappedStructure = new TreeMap<String, Object>((MapStringStringStructure) modifiableStructure);
			}

			if (modifiableStructure instanceof MapStringListStringStructure) {
				mappedStructure = new TreeMap<String, Object>((MapStringStringStructure) modifiableStructure);
			}

		}

		if (mappedStructure == null) {
			throw new ImpossibleRuntimeException("Unsupported structure : " + modifiableStructure.getClass());
		}

		return mappedStructure;
	}

	private Map<String, Object> newContentVersionTableRow(ContentVersion version) {
		Map<String, Object> row = new HashMap<>();
		row.put("sha1", version.getHash());
		row.put("filename", version.getFilename());
		row.put("version", version.getVersion());
		row.put("mimetype", version.getMimetype());
		row.put("modifiedBy", version.getModifiedBy());
		row.put("length", version.getLength());
		row.put("lastModification", version.getLastModificationDateTime());
		row.put("comment", version.getComment());
		return row;
	}

	private void writeMultivalueReferenceMetadata(Record record, Metadata metadata) {

		List<Map<String, Object>> tableRows = new ArrayList<>();
		for (String id : record.<String>getValues(metadata)) {
			try {
				Record referencedRecord = recordServices.getDocumentById(id);

				LinkedHashMap<String, Object> tableRow = new LinkedHashMap<>();
				tableRow.put("id", referencedRecord.getId());
				tableRow.put("code", referencedRecord.get(Schemas.CODE));
				tableRow.put("schema", referencedRecord.get(Schemas.SCHEMA));
				tableRow.put("title", referencedRecord.getTitle());
				tableRows.add(tableRow);

			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.warn("Record '" + id + "' was not found");
			}
		}

		eadXmlWriter.addMetadataEADTable(metadata, tableRows);
	}

	private void writeSinglevalueReferenceMetadata(Record record, Metadata metadata) {
		Record referencedRecord = null;

		String id = record.get(metadata);
		if (id != null) {
			try {
				referencedRecord = recordServices.getDocumentById(id);
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.warn("Record '" + id + "' was not found");
			}
		}

		if (referencedRecord != null) {
			LinkedHashMap<String, Object> infos = new LinkedHashMap<>();
			infos.put("id", id);
			infos.put("code", referencedRecord.get(Schemas.CODE));
			infos.put("schema", referencedRecord.get(Schemas.SCHEMA));
			infos.put("title", referencedRecord.getTitle());
			eadXmlWriter.newEADDefList(infos);
		}

	}

	public EADArchiveDescription buildArchiveDescription(Record record, MetadataSchema schema) {
		EADArchiveDescription archdesc = new EADArchiveDescription();

		if (schema.hasMetadataWithCode("keywords")) {
			Metadata keywordsMetadata = schema.getMetadata("keywords");
			archdesc.getControlAccessSubjects().addAll(record.<String>getValues(keywordsMetadata));
		}

		if (schema.hasMetadataWithCode("description")) {
			Metadata descriptionMetadata = schema.getMetadata("description");
			archdesc.getDidAbstracts().addAll(record.<String>getValues(descriptionMetadata));
		}

		if (includeRelatedMaterials) {
			Iterator<Record> recordsIterator = appLayerFactory.getModelLayerFactory().newSearchServices()
					.recordsIterator(fromAllSchemasIn(record.getCollection()).where(PATH_PARTS).isEqualTo(record.getId()));

			while (recordsIterator.hasNext()) {
				Record linkedRecord = recordsIterator.next();
				archdesc.getRelatedmaterialLists().add(singletonList(linkedRecord.getId() + " " + linkedRecord.getTitle()));
			}
		}

		archdesc.getDidUnitDates().put("creation", record.<LocalDateTime>get(Schemas.CREATED_ON));
		archdesc.getDidUnitDates().put("lastModification", record.<LocalDateTime>get(Schemas.MODIFIED_ON));

		if (com.constellio.app.modules.rm.wrappers.Document.SCHEMA_TYPE.equals(record.getTypeCode())) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);
			com.constellio.app.modules.rm.wrappers.Document document = rm.wrapDocument(record);
			Folder folder = rm.getFolder(document.getFolder());

			archdesc.getDidUnitDates().put("publication", document.getFolderActualDepositDate());


		} else if (Folder.SCHEMA_TYPE.equals(record.getTypeCode())) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);
			Folder folder = rm.getFolder(record.getId());

			Category processusActivite = rm.getCategory(folder.getCategory());
			archdesc.getFileplanPs().add(processusActivite.getTitle());
			archdesc.getDidUnitDates().put("closure", folder.getCloseDate());

			AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(folder.<String>get(Folder.ADMINISTRATIVE_UNIT));
			String adminstrativeUnitParentId = administrativeUnit.get(AdministrativeUnit.PARENT);
			if (adminstrativeUnitParentId != null) {
				AdministrativeUnit parentAdministrativeUnit = rm.getAdministrativeUnit(adminstrativeUnitParentId);
				archdesc.setDidOriginationCorpname(parentAdministrativeUnit.getCode());
			}

		}
		return archdesc;
	}

	private boolean isMetadataIncludedInEAD(Metadata metadata) {
		return !METADATAS_ALWAYS_IN_ARCHIVE_DESCRIPTION.contains(metadata.getLocalCode());
	}

	public void build(Record record, String sipPath, File file) throws IOException {

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		EADArchiveDescription archdesc = buildArchiveDescription(record, schema);

		this.eadXmlWriter = new RecordEADWriter();
		eadXmlWriter.addHeader(record.getId(), record.getTitle());
		eadXmlWriter.addArchdesc(archdesc, record.getId(), record.getTitle());

		for (Metadata metadata : types.getSchema(record.getSchemaCode()).getMetadatas()) {
			if (isMetadataIncludedInEAD(metadata)
				&& isNotEmptyValue(record.getValues(metadata))) {
				addMetadata(record, metadata);
			}
		}

		eadXmlWriter.build(sipPath, errors, file);
	}


}
