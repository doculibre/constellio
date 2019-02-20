package com.constellio.app.utils;

import com.constellio.app.entities.modules.ProgressInfo;
import com.jgoodies.common.collect.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;

public class FileLoggedProgressInfo extends ProgressInfo {

	File outputFile;

	public FileLoggedProgressInfo(File outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public void setTask(String task) {
		super.setTask(task);
	}

	@Override
	public void setEnd(long end) {
		super.setEnd(end);
	}

	@Override
	public void setCurrentState(long currentState) {
		super.setCurrentState(currentState);
		logProgression();
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

			StringBuilder stringBuilder = new StringBuilder();
			if (getTask() != null) {
				stringBuilder.append(getTask()).append(" - ");
			}

			if (getProgressMessage() != null) {
				stringBuilder.append(getProgressMessage()).append(" - ");
			}

			stringBuilder.append(this.getCurrentState()).append("/").append(this.getEnd()).append("\n");

			FileUtils.writeStringToFile(outputFile, stringBuilder.toString(), "UTF-8", true);
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
