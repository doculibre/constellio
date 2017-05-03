package com.constellio.app.extensions.api.scripts;

import java.util.List;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;

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

}
