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
package com.constellio.app.modules.rm.reports.factories.labels;

import java.util.List;

import com.constellio.app.modules.rm.reports.builders.labels.LabelsReportBuilder;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportPresenter;
import com.constellio.app.reports.builders.administration.plan.ReportBuilder;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class LabelsReportFactory implements ReportBuilderFactory {
	private final List<String> recordIds;
	private final LabelConfiguration labelConfiguration;
	private final int startPosition;
	private final int numberOfCopies;
	//	private final String mode;

	public LabelsReportFactory(List<String> recordIds, LabelConfiguration labelConfiguration, int startPosition,
			int numberOfCopies) {
		this.recordIds = recordIds;
		this.labelConfiguration = labelConfiguration;
		this.startPosition = startPosition;
		this.numberOfCopies = numberOfCopies;
	}

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		LabelsReportPresenter presenter = new LabelsReportPresenter(collection, modelLayerFactory);
		return new LabelsReportBuilder(presenter.build(recordIds, startPosition, numberOfCopies, labelConfiguration.getKey()));
	}

	@Override
	public String getFilename() {
		return "labels.pdf";
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
