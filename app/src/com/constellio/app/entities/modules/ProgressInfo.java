/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
