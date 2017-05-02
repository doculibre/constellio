package com.constellio.app.extensions.api.scripts;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.AppLayerFactory;

public abstract class ScriptWithLogOutput extends Script {

	public ScriptWithLogOutput(AppLayerFactory appLayerFactory, String category, String name) {
		super(appLayerFactory, category, name);
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
		execute();

		if (outputLogger != null) {
			outputLogger.info("Execution of " + getClass().getSimpleName() + " finished successfully");
		}
	}

	protected abstract void execute()
			throws Exception;
}
