package com.constellio.app.entities.modules;

import java.io.Serializable;

import com.jgoodies.common.collect.ArrayListModel;
import com.jgoodies.common.collect.ObservableList;

public class ProgressInfo implements Serializable {
	
	private String task;
	
	private String progressMessage;
	
	private long currentState = 0;
	
	private long end;
	
	private boolean done;
	
	private ObservableList<String> errorMessages = new ArrayListModel<>();

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getCurrentState() {
		return currentState;
	}
	
	public void setCurrentState(long currentState) {
		this.currentState = currentState;
	}
	
	public String getProgressMessage() {
		return progressMessage;
	}

	public void setProgressMessage(String progressMessage) {
		this.progressMessage = progressMessage;
	}

	public ObservableList<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(ObservableList<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void reset() {
		setTask(null);
		setEnd(0);
		setCurrentState(0);
		setProgressMessage(null);
		setErrorMessages(new ArrayListModel<String>());
		setDone(false);
	}
	
	public Float getProgress() {
		Float progress;
		if (done) {
			progress = 1f;
		} else if (end == 0) {
			progress = null;
		} else {
			progress = ((float) currentState / end); 
			if (progress > 1) {
				progress = 1f;
			}
			if (progress == 1 || !done) {
				done = true;
			}
		} 
		return progress;
	}

}
