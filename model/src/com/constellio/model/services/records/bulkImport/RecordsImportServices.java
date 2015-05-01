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
package com.constellio.model.services.records.bulkImport;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.services.records.bulkImport.Resolver.toResolver;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.ContentImport;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.bulkImport.RecordsImportServicesRuntimeException.RecordsImportServicesRuntimeException_CyclicDependency;
import com.constellio.model.services.records.bulkImport.data.ImportData;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class RecordsImportServices {

	public static final String INVALID_SCHEMA_TYPE_CODE = "invalidSchemaTypeCode";
	public static final String LEGACY_ID_LOCAL_CODE = LEGACY_ID.getLocalCode();

	static final List<String> ALL_BOOLEAN_YES = Arrays.asList("o", "y", "t", "oui", "vrai", "yes", "true", "1");
	static final List<String> ALL_BOOLEAN_NO = Arrays.asList("n", "f", "non", "faux", "no", "false", "0");

	private static final String IMPORT_URL_INPUTSTREAM_NAME = "RecordsImportServices-ImportURL";

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsImportServices.class);

	private static final int DEFAULT_BATCH_SIZE = 100;

	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemasManager schemasManager;
	private RecordServices recordServices;
	private ContentManager contentManager;
	private IOServices ioServices;
	private int batchSize;

	public RecordsImportServices(ModelLayerFactory modelLayerFactory, int batchSize) {
		this.modelLayerFactory = modelLayerFactory;
		this.batchSize = batchSize;
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		recordServices = modelLayerFactory.newRecordServices();
		contentManager = modelLayerFactory.getContentManager();
		ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
	}

	public RecordsImportServices(ModelLayerFactory modelLayerFactory) {
		this(modelLayerFactory, DEFAULT_BATCH_SIZE);
	}

	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener,
			final User user)
			throws RecordsImportServicesRuntimeException {

		importDataProvider.initialize();
		try {

			MetadataSchemaTypes types = schemasManager.getSchemaTypes(user.getCollection());
			ResolverCache resolverCache = new ResolverCache(modelLayerFactory.newSearchServices(), types);
			validate(importDataProvider, types, resolverCache);
			return run(importDataProvider, bulkImportProgressionListener, user, types, resolverCache);

		} finally {
			importDataProvider.close();
		}

	}

	void validate(ImportDataProvider importDataProvider, MetadataSchemaTypes types, ResolverCache resolverCache) {
		List<String> importedSchemaTypes = getImportedSchemaTypes(types, importDataProvider);

		for (String schemaType : importedSchemaTypes) {
			new RecordsImportValidator(schemaType, importDataProvider, types, resolverCache).validate();
		}
	}

	private BulkImportResults run(ImportDataProvider importDataProvider,
			BulkImportProgressionListener bulkImportProgressionListener,
			User user, MetadataSchemaTypes types, ResolverCache resolverCache) {

		BulkImportResults importResults = new BulkImportResults();
		List<String> importedSchemaTypes = getImportedSchemaTypes(types, importDataProvider);
		for (String schemaType : importedSchemaTypes) {

			List<String> uniqueMetadatas = types.getSchemaType(schemaType).getAllMetadatas()
					.onlyWithType(STRING).onlyUniques().toLocalCodesList();
			int previouslySkipped = 0;

			boolean typeImportFinished = false;
			while (!typeImportFinished) {

				Iterator<ImportData> importDataIterator = importDataProvider.newDataIterator(schemaType);
				int skipped = bulkImport(importResults, uniqueMetadatas, resolverCache, importDataIterator, schemaType,
						bulkImportProgressionListener, user);
				if (skipped > 0 && skipped == previouslySkipped) {
					List<String> cyclicDependentIds = resolverCache.getNotYetImportedLegacyIds(schemaType);
					throw new RecordsImportServicesRuntimeException_CyclicDependency(schemaType, cyclicDependentIds);
				}
				if (skipped == 0) {
					typeImportFinished = true;
				}
				previouslySkipped = skipped;

			}
		}
		return importResults;
	}

	int bulkImport(BulkImportResults importResults, List<String> uniqueMetadatas, ResolverCache resolverCache,
			Iterator<ImportData> importDataIterator, String schemaType,
			BulkImportProgressionListener bulkImportProgressionListener, User user) {

		int skipped = 0;
		Iterator<List<ImportData>> importDataBatches = new BatchBuilderIterator<>(importDataIterator, 100);

		while (importDataBatches.hasNext()) {

			try {

				List<ImportData> batch = importDataBatches.next();
				Transaction transaction = new Transaction();
				skipped += importBatch(importResults, uniqueMetadatas, resolverCache, schemaType, user, batch, transaction);
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				while (importDataBatches.hasNext()) {
					importDataBatches.next();
				}
				throw new RuntimeException(e);
			}

		}
		return skipped;
	}

	private int importBatch(BulkImportResults importResults, List<String> uniqueMetadatas, ResolverCache resolverCache,
			String schemaType, User user, List<ImportData> batch, Transaction transaction) {
		int skipped = 0;
		for (ImportData toImport : batch) {

			String legacyId = toImport.getLegacyId();
			if (resolverCache.getNotYetImportedLegacyIds(schemaType).contains(legacyId)) {
				try {
					Record record = buildRecord(importResults, user, resolverCache, user.getCollection(), schemaType, toImport);
					transaction.add(record);
					resolverCache.mapIds(schemaType, LEGACY_ID_LOCAL_CODE, legacyId, record.getId());
					for (String uniqueMetadata : uniqueMetadatas) {
						String value = (String) toImport.getFields().get(uniqueMetadata);
						if (value != null) {
							resolverCache.mapIds(schemaType, uniqueMetadata, value, record.getId());
						}
					}
				} catch (SkippedRecordException e) {
					skipped++;
				}
			}
		}
		return skipped;
	}

	Record buildRecord(BulkImportResults importResults, User user, ResolverCache resolverCache, String collection,
			String schemaType, ImportData toImport)
			throws SkippedRecordException {
		MetadataSchema schema = getMetadataSchema(collection, schemaType + "_" + toImport.getSchema());

		Record record;
		String legacyId = toImport.getLegacyId();
		if (resolverCache.isRecordUpdate(schemaType, legacyId)) {
			record = modelLayerFactory.newSearchServices().searchSingleResult(from(schema).where(LEGACY_ID).isEqualTo(legacyId));
		} else {
			record = recordServices.newRecordWithSchema(schema);
		}
		record.set(LEGACY_ID, legacyId);
		for (Entry<String, Object> field : toImport.getFields().entrySet()) {
			Metadata metadata = schema.getMetadata(field.getKey());
			Object value = field.getValue();
			Object convertedValue = value == null ? null : convertValue(importResults, user, resolverCache, metadata, value);
			record.set(metadata, convertedValue);
		}

		return record;
	}

	Object convertScalarValue(BulkImportResults importResults, User user, ResolverCache resolverCache, Metadata metadata,
			Object value)
			throws SkippedRecordException {
		switch (metadata.getType()) {

		case NUMBER:
			return Double.valueOf((String) value);

		case BOOLEAN:
			return value == null ? null : ALL_BOOLEAN_YES.contains(((String) value).toLowerCase());

		case CONTENT:
			return convertContent(importResults, user, value);

		case STRUCTURE:
			throw new UnsupportedOperationException("TODO");

		case REFERENCE:
			return convertReference(resolverCache, metadata, (String) value);

		case ENUM:
			return EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), (String) value);

		default:
			return value;
		}
	}

	private Content convertContent(BulkImportResults importResults, User user, Object value) {
		ContentImport contentImport = (ContentImport) value;
		try {
			URL url = toURL(contentImport.getUrl());

			InputStream inputStream = ioServices.newBufferedInputStream(url.openStream(), IMPORT_URL_INPUTSTREAM_NAME);

			try {
				ContentVersionDataSummary contentVersionDataSummary = contentManager.upload(inputStream);
				if (contentImport.isMajor()) {
					return contentManager.createMajor(user, contentImport.getFileName(), contentVersionDataSummary);
				} else {
					return contentManager.createMinor(user, contentImport.getFileName(), contentVersionDataSummary);
				}
			} finally {
				ioServices.closeQuietly(inputStream);
			}

		} catch (Exception e) {
			LOGGER.warn("Could not retrieve content with url '" + contentImport.getUrl() + "'");
			importResults.getInvalidContentUrls().add(contentImport.getUrl());
			return null;
		}
	}

	private URL toURL(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException e) {
			try {
				return new File(value).toURI().toURL();
			} catch (MalformedURLException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	private Object convertReference(ResolverCache resolverCache, Metadata metadata, String value)
			throws SkippedRecordException {

		Resolver resolver = toResolver(value);

		String referenceType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
		if (!resolverCache.isAvailable(referenceType, resolver.metadata, resolver.value)) {
			throw new SkippedRecordException();
		}
		return resolverCache.resolve(referenceType, value);
	}

	Object convertValue(BulkImportResults importResults, User user, ResolverCache resolverCache, Metadata metadata, Object value)
			throws SkippedRecordException {

		if (metadata.isMultivalue()) {

			List<Object> convertedValues = new ArrayList<>();

			if (value != null) {
				List<Object> rawValues = (List<Object>) value;
				for (Object item : rawValues) {
					Object convertedValue = convertScalarValue(importResults, user, resolverCache, metadata, item);
					if (convertedValue != null) {
						convertedValues.add(convertedValue);
					}
				}
			}

			return convertedValues;

		} else {
			return convertScalarValue(importResults, user, resolverCache, metadata, value);
		}

	}

	List<String> getImportedSchemaTypes(MetadataSchemaTypes types, ImportDataProvider importDataProvider) {
		List<String> importedSchemaTypes = new ArrayList<>();
		List<String> availableSchemaTypes = importDataProvider.getAvailableSchemaTypes();

		validateAvailableSchemaTypes(types, availableSchemaTypes);

		for (String schemaType : types.getSchemaTypesSortedByDependency()) {
			if (availableSchemaTypes.contains(schemaType)) {
				importedSchemaTypes.add(schemaType);
			}
		}

		return importedSchemaTypes;
	}

	private void validateAvailableSchemaTypes(MetadataSchemaTypes types, List<String> availableSchemaTypes) {
		ValidationErrors errors = new ValidationErrors();

		for (String availableSchemaType : availableSchemaTypes) {
			try {
				types.getSchemaType(availableSchemaType);
			} catch (NoSuchSchemaType e) {
				Map<String, String> parameters = new HashMap<>();
				parameters.put("schemaType", availableSchemaType);

				errors.add(RecordsImportServices.class, INVALID_SCHEMA_TYPE_CODE, parameters);
			}
		}

		if (!errors.getValidationErrors().isEmpty()) {
			throw new ValidationRuntimeException(errors);
		}
	}

	MetadataSchema getMetadataSchema(String collection, String schema) {
		return schemasManager.getSchemaTypes(collection).getSchema(schema);
	}

	private static class SkippedRecordException extends Exception {
	}

}
