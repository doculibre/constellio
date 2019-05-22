package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;

import java.util.ArrayList;
import java.util.List;

public abstract class Script {

	String category;
	String name;

	protected AppLayerFactory appLayerFactory;
	protected ModelLayerFactory modelLayerFactory;
	protected DataLayerFactory dataLayerFactory;
	protected RecordServices recordServices;
	protected SearchServices searchServices;
	protected ScriptParameterValues parameterValues;
	protected ScriptActionLogger outputLogger;

	public Script(AppLayerFactory appLayerFactory, String category, String name) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.category = category == null ? "Others" : category;
		this.name = name;
	}

	public String getScriptCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	public String getWarningMessage() {
		return null;
	}

	public abstract List<ScriptParameter> getParameters();

	public abstract void execute(ScriptActionLogger outputLogger, ScriptParameterValues parameterValues)
			throws Exception;

	public abstract ScriptOutput getScriptOutput();

	public List<String> getCollectionCodesExcludingSystem() {
		return appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem();
	}

	public List<String> getCollectionCodesIncludingSystem() {
		return appLayerFactory.getCollectionsManager().getCollectionCodes();
	}

	public List<String> getUserManageableSchemaTypeCodes() {
		List<String> userManageableSchemaTypeCode = new ArrayList<>();

		userManageableSchemaTypeCode.add(Folder.SCHEMA_TYPE);
		userManageableSchemaTypeCode.add(Document.SCHEMA_TYPE);
		userManageableSchemaTypeCode.add(Task.SCHEMA_TYPE);
		userManageableSchemaTypeCode.add(RetentionRule.SCHEMA_TYPE);
		userManageableSchemaTypeCode.add(ContainerRecord.SCHEMA_TYPE);

		return userManageableSchemaTypeCode;
	}

	public List<String> getCollectionCodesWithSchemaType(String schemaType) {

		List<String> returnedCollections = new ArrayList<>();

		for (String collection : appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem()) {
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			if (types.hasType(schemaType)) {
				returnedCollections.add(collection);
			}
		}
		return returnedCollections;
	}

	public MetadataSchemaTypes types(String collection) {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public MetadataSchemaType type(String collection, String schemaType) {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(schemaType);
	}

	public RMSchemasRecordsServices rm(String collection) {
		return new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public ESSchemasRecordsServices es(String collection) {
		return new ESSchemasRecordsServices(collection, appLayerFactory);
	}

	public <T> T getParameterValue(ScriptParameter parameter) {
		return parameterValues.get(parameter);
	}

	public boolean getBooleanParameterValueWithDefaultValue(ScriptParameter parameter, boolean defaultValue) {
		Boolean value = parameterValues.get(parameter);
		return value == null ? defaultValue : value;
	}
}
