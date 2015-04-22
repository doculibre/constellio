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
package com.constellio.app.modules.rm.reports.decommissioning;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DocumentVersementReportBuilder;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class DocumentVersementReportPresenterManualAcceptTest extends ReportBuilderTestFramework {

	RMTestRecords records;
	ContainerRecordReportPresenter presenter;

	@Before
	public void setUp() throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();
		presenter = new ContainerRecordReportPresenter(zeCollection, getModelLayerFactory());

	}

	// TODO Benoit
	@Test
	public void givenContainerBac01WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_UserNameAndEmail_Sort_2_Folders_2000_2002_DM_PA() {
		DocumentReportModel model = presenter.build(records.getContainerBac01());

		build(new DocumentVersementReportBuilder(model, getIOLayerFactory().newIOServices(),
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void givenContainerBac05WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_Sort_6_Fodlers_2000_2002_DM_PA() {
		DocumentReportModel model = presenter.build(records.getContainerBac05());

		build(new DocumentVersementReportBuilder(model, getIOLayerFactory().newIOServices(),
				getModelLayerFactory().getFoldersLocator()));
	}
}