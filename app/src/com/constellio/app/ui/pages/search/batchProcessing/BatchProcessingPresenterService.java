package com.constellio.app.ui.pages.search.batchProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;

public class BatchProcessingPresenterService {
	private final SchemasRecordsServices schemas;
	private final AppLayerFactory appLayerFactory;
	private final ModelLayerFactory modelLayerFactory;
	private final String collection;

	public BatchProcessingPresenterService(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.collection = collection;
		this.schemas = new SchemasRecordsServices(collection, modelLayerFactory);
	}

	public String getOriginSchema(String schemaType, List<String> selectedRecordIds) {
		if (selectedRecordIds == null || selectedRecordIds.isEmpty()) {
			throw new ImpossibleRuntimeException("Batch processing should be done on at least one record");
		}
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		String firstRecordSchema = getRecordSchemaCode(recordServices, selectedRecordIds.get(0));
		Boolean moreThanOneSchema = false;
		for (int i = 0; i < selectedRecordIds.size(); i++) {
			String currentRecordSchema = getRecordSchemaCode(recordServices, selectedRecordIds.get(i));
			if (!currentRecordSchema.equals(firstRecordSchema)) {
				moreThanOneSchema = true;
				break;
			}
		}
		if (moreThanOneSchema) {
			return schemaType + "_" + "default";
		} else {
			return firstRecordSchema;
		}
	}

	private String getRecordSchemaCode(RecordServices recordServices, String recordId) {
		return recordServices.getDocumentById(recordId).getSchemaCode();
	}

	private String getSchemataType(Set<String> recordsSchemata) {
		String firstType = getSchemaType(recordsSchemata.iterator().next());
		ensureAllSchemataOfSameType(recordsSchemata, firstType);
		return firstType;
	}

	private String getSchemaType(String schemaCode) {
		return StringUtils.substringBefore(schemaCode, "_");
	}

	private void ensureAllSchemataOfSameType(Set<String> recordsSchemata, String firstType) {
		for (String schemaCode : recordsSchemata) {
			String currentSchemaType = getSchemaType(schemaCode);
			if (!currentSchemaType.equals(firstType)) {
				throw new ImpossibleRuntimeException("Batch processing should be done on the same schema type :" +
						StringUtils.join(recordsSchemata, ";"));
			}
		}
	}

	public List<String> getDestinationSchemata(String schemaType) {
		List<String> schemataCodes = new ArrayList<>();
		List<MetadataSchema> schemata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(schemaType).getAllSchemas();
		for (MetadataSchema currentSchema : schemata) {
			schemataCodes.add(currentSchema.getCode());
		}
		return schemataCodes;
	}

	public RecordVO newRecordVO(String schemaCode, SessionContext sessionContext) {
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(schemaCode);
		Record tmpRecord = modelLayerFactory.newRecordServices().newRecordWithSchema(schema);
		return new RecordToVOBuilder().build(tmpRecord, RecordVO.VIEW_MODE.FORM, sessionContext);
	}

	public BatchProcessResults execute(BatchProcessRequest request)
			throws RecordServicesException {

		Transaction transaction = prepareTransaction(request);
		BatchProcessResults results = toBatchProcessResults(transaction);

		//results.getRecordModifications().add

		return results;
	}

	public BatchProcessResults simulate(BatchProcessRequest request)
			throws RecordServicesException {

		Transaction transaction = prepareTransaction(request);

		return toBatchProcessResults(transaction);
	}

	public BatchProcessingMode getBatchProcessingMode() {
		ConstellioEIMConfigs eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		return eimConfigs.getBatchProcessingMode();
	}

	private BatchProcessResults toBatchProcessResults(Transaction transaction) {
		return null;
	}

	private Transaction prepareTransaction(BatchProcessRequest request) {
		return null;
	}

}
