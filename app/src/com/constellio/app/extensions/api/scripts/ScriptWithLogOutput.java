package com.constellio.app.extensions.api.scripts;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ScriptWithLogOutput extends Script {

	private boolean saveOutputAsTemporaryRecord = false;

	public ScriptWithLogOutput(AppLayerFactory appLayerFactory, String category, String name) {
		this(appLayerFactory, category, name, false);
	}

	public ScriptWithLogOutput(AppLayerFactory appLayerFactory, String category, String name,
							   boolean saveOutputAsTemporaryRecord) {
		super(appLayerFactory, category, name);
		this.saveOutputAsTemporaryRecord = saveOutputAsTemporaryRecord;
	}

	@Override
	public List<ScriptParameter> getParameters() {
		return new ArrayList<>();
	}

	@Override
	public ScriptOutput getScriptOutput() {
		return ScriptOutput.toLogFile(getClass().getSimpleName() + ".log");
	}

	@Override
	public void execute(ScriptActionLogger outputLogger, ScriptParameterValues parameterValues)
			throws Exception {
		if (outputLogger != null) {
			outputLogger.info("Starting execution of " + getClass().getSimpleName());
		}

		this.outputLogger = outputLogger;
		this.parameterValues = parameterValues;
		try {
			execute();
		} catch (Exception e) {
			if (outputLogger != null) {
				this.outputLogger.error("Exception thrown while executing script : " + ExceptionUtils.getStackTrace(e));
			}
			throw e;
		}

		if (outputLogger != null) {
			outputLogger.info("Execution of " + getClass().getSimpleName() + " finished successfully");
		}
	}

	protected abstract void execute()
			throws Exception;

	public ConditionnedActionExecutorInBatchBuilder onCondition(LogicalSearchCondition condition) {
		return new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, condition);
	}

	public boolean isSaveOutputAsTemporaryRecord() {
		return saveOutputAsTemporaryRecord;
	}
}
