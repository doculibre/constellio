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
package com.constellio.app.ui.entities;

import java.io.Serializable;

import com.constellio.app.modules.rm.reports.factories.labels.LabelConfiguration;

public class LabelParametersVO implements Serializable {
	private LabelConfiguration labelConfiguration;
	private int startPosition;
	private int numberOfCopies;

	public LabelParametersVO(LabelConfiguration labelConfiguration) {
		this.labelConfiguration = labelConfiguration;
		startPosition = 1;
		numberOfCopies = 1;
	}

	public LabelConfiguration getLabelConfiguration() {
		return labelConfiguration;
	}

	public void setLabelConfiguration(LabelConfiguration labelConfiguration) {
		this.labelConfiguration = labelConfiguration;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public int getNumberOfCopies() {
		return numberOfCopies;
	}

	public void setNumberOfCopies(int numberOfCopies) {
		this.numberOfCopies = numberOfCopies;
	}
}
