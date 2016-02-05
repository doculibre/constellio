package com.constellio.app.entities.modules;

import java.io.Serializable;

public class ProgressInfo implements Serializable {
	
	private String task;
	
	private String progressMessage;
	
	private long currentState = 0;
	
	private long end;

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

	public void reset() {
		setTask(null);
		setEnd(0);
		setCurrentState(0);
		setProgressMessage(null);
	}
	
	public Float getProgress() {
		Float progress;
		if (end == 0) {
			progress = null;
		} else {
			progress = ((float) currentState / end); 
			if (progress > 1) {
				progress = 1f;
			}
		} 
		return progress;
	}

}
