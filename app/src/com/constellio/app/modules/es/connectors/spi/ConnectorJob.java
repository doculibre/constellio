package com.constellio.app.modules.es.connectors.spi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ConnectorJob implements Runnable {

	private AtomicBoolean stopped = new AtomicBoolean(false);

	protected final String jobName;

	private String currentJobStep;

	protected final Connector connector;

	public ConnectorJob(Connector connector, String jobName) {
		this.connector = connector;
		this.jobName = jobName;
	}

	@Override
	public final void run() {

		try {
			execute(connector);

		} catch (ConnectorStoppedRuntimeException e) {
			logWarning("Connector job shutdown");

		} catch (Throwable t) {
			logError(t);
		}
	}

	public void logInfo(String message) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("jobName", jobName);
		parameters.put("currentJobStep", currentJobStep);

		String title = jobName + "-" + currentJobStep;
		String description = message;
		//connector.logger.info(title, description, parameters);
	}

	public void logWarning(String message) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("jobName", jobName);
		parameters.put("currentJobStep", currentJobStep);

		String title = jobName + "-" + currentJobStep;
		String description = message;
		connector.logger.error(title, description, parameters);
	}

	public void logError(Throwable t) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("jobName", jobName);
		parameters.put("currentJobStep", currentJobStep);

		String title = jobName + "-" + currentJobStep;
		String description = getStackTrace(t);
		connector.logger.error(title, description, parameters);
	}

	public void ensureNotStopped() {
		if (stopped.get()) {
			throw new ConnectorStoppedRuntimeException();
		}
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public abstract void execute(Connector connector);

	public void shutdown() {
		this.stopped.set(true);
	}

	public void setJobStep(String step) {
		this.currentJobStep = step;
		String title = "Step " + jobName + "-" + currentJobStep;
		connector.logger.info(step, "", new HashMap<String, String>());
	}
}
