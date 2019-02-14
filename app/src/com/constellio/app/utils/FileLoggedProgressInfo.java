package com.constellio.app.utils;

import com.constellio.app.entities.modules.ProgressInfo;
import com.jgoodies.common.collect.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;

public class FileLoggedProgressInfo extends ProgressInfo {

	File outputFile;

	private boolean newTask;
	private boolean newEnd;

	public FileLoggedProgressInfo(File outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public void setTask(String task) {
		newTask = true;
		super.setTask(task);
		if (newEnd) {
			logProgression();
		}
	}

	@Override
	public void setEnd(long end) {
		newEnd = true;
		super.setCurrentState(0);
		super.setEnd(end);
		if (newTask) {
			logProgression();
		}
	}

	@Override
	public void setCurrentState(long currentState) {
		super.setCurrentState(currentState);
		if (currentState != 0) {
			logProgression();
		}
	}

	@Override
	public void setProgressMessage(String progressMessage) {
		super.setProgressMessage(progressMessage);
	}

	@Override
	public void setErrorMessages(ObservableList<String> errorMessages) {
		super.setErrorMessages(errorMessages);
	}

	@Override
	public void setDone(boolean done) {
		super.setDone(done);

	}

	private void logProgression() {
		try {
			String status = this.getTask() + " - " + this.getCurrentState() + "/" + this.getEnd() + "\n";
			FileUtils.writeStringToFile(outputFile, status, "UTF-8", true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void logEndingException(Throwable t) {
		String errorMessage = ExceptionUtils.getStackTrace(t);
		try {
			FileUtils.writeStringToFile(outputFile, "Task encountered an exception : " + errorMessage + "\n", "UTF-8", true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
