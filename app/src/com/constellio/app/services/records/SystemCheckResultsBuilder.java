package com.constellio.app.services.records;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;

public class SystemCheckResultsBuilder {

	private static final String BROKEN_LINK_ERROR = "brokenLink";
	private static final String LOGICALLY_DELETED_RECORD_ERROR = "logicallyDeletedRecord";

	Language language;

	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	SystemCheckResults results;

	public SystemCheckResultsBuilder(Language language, AppLayerFactory appLayerFactory, SystemCheckResults results) {
		this.language = language;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.results = results;
	}

	public void markLogicallyDeletedRecordAsError(RecordWrapper recordWrapper) {
		markLogicallyDeletedRecordAsError(recordWrapper.getWrappedRecord());
	}

	public void markLogicallyDeletedRecordAsError(Record record) {
		String type = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection());
		String label = types.getSchemaType(type).getLabel(language);

		Map<String, Object> params = new HashMap<>();

		String title = record.get(Schemas.CODE);
		if (title == null) {
			title = record.getTitle();
		} else {
			title += " - " + record.getTitle();
		}

		params.put("schemaType", type);
		params.put("schemaTypeLabel", label);
		params.put("recordId", record.getId());
		params.put("recordTitle", record.getTitle());

		results.getErrors().add(SystemCheckResultsBuilder.class, LOGICALLY_DELETED_RECORD_ERROR, params);
	}

	public void addBrokenLink(String recordId, String brokenLinkRecordId, Metadata referenceMetadata) {
		incrementMetric(SystemCheckManager.BROKEN_REFERENCES_METRIC);
		Map<String, Object> params = new HashMap<>();

		params.put("metadataCode", referenceMetadata.getCode());
		params.put("record", recordId);
		params.put("brokenLinkRecordId", brokenLinkRecordId);

		results.getErrors().add(SystemCheckResultsBuilder.class, BROKEN_LINK_ERROR, params);
	}

	public void markAsRepaired(String id) {
		results.markAsRepaired(id);
	}

	public Map<String, Integer> getMetrics() {
		return results.getMetrics();
	}

	public int getMetric(String key) {
		Integer metric = getMetrics().get(key);
		if (metric == null) {
			metric = 0;
			setMetric(key, 0);
		}

		return metric;

	}

	public <T> SystemCheckResultsBuilder incrementMetric(String key) {
		getMetrics().put(key, getMetric(key) + 1);
		return this;
	}

	public <T> SystemCheckResultsBuilder incrementMetric(String key, int value) {
		getMetrics().put(key, getMetric(key) + value);
		return this;
	}

	public <T> SystemCheckResultsBuilder setMetric(String key, int value) {
		getMetrics().put(key, value);
		return this;
	}

	public Map<String, Object> getResultsInfos() {
		return results.getResultsInfos();
	}

	public void addNewValidationError(RecordServicesException.ValidationException validationException) {
		results.errors.addAll(validationException.getErrors().getValidationErrors());
	}

	public void addNewValidationError(Class<?> clazz, String errorCode, Map<String, Object> parameters) {
		results.errors.add(clazz, errorCode, parameters);
	}

	public <T> T get(String key) {
		return (T) getResultsInfos().get(key);
	}

	public <T> SystemCheckResultsBuilder set(String key, T value) {
		getResultsInfos().put(key, value);
		return this;
	}

	public <T> List<T> getList(String key) {
		List<T> value = (List<T>) getResultsInfos().get(key);
		if (value == null) {
			value = new ArrayList<T>();
			setList(key, value);
		}
		return value;
	}

	public <T> SystemCheckResultsBuilder setList(String key, List<T> value) {
		getResultsInfos().put(key, value);
		return this;
	}

	public <T> SystemCheckResultsBuilder addListItem(String key, T value) {
		getList(key).add(value);
		return this;
	}
}
