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
package com.constellio.app.modules.rm.reports.administration.plan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ClassificationPlanReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel.ClassificationPlanReportModel_Category;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportPresenter;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class ClassificationPlanReportPresenterManualAcceptTest extends ReportBuilderTestFramework {

	// Initiate Presenter Class
	RMTestRecords records = new RMTestRecords(zeCollection);
	ClassificationPlanReportPresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		presenter = new ClassificationPlanReportPresenter(zeCollection, getModelLayerFactory());
	}

	@Test
	public void whenSettingDetailedInPresenterThenGetDetailedInModel() {
		ClassificationPlanReportModel model = presenter.build();
		assertThat(model.isDetailed()).isFalse();

		presenter = new ClassificationPlanReportPresenter(zeCollection, getModelLayerFactory(), false);
		model = presenter.build();
		assertThat(model.isDetailed()).isFalse();

		presenter = new ClassificationPlanReportPresenter(zeCollection, getModelLayerFactory(), true);
		model = presenter.build();
		assertThat(model.isDetailed()).isTrue();
	}

	@Test
	public void givenACategoryTreeWhenBuildingModelThenGetAppropriateModelAndReport() {
		ClassificationPlanReportModel model = presenter.build();

		List<ClassificationPlanReportModel_Category> categories = model.getRootCategories();
		assertThat(categories).isNotEmpty();

		ClassificationPlanReportModel_Category xeCategory = categories.get(0);
		assertThat(xeCategory.getCode()).isEqualTo("X");
		assertThat(xeCategory.getDescription()).isEqualTo("Ze ultimate category X");
		assertThat(xeCategory.getLabel()).isEqualTo("Xe category");

		ClassificationPlanReportModel_Category x100Category = xeCategory.getCategories().get(0);
		assertThat(x100Category.getCode()).isEqualTo("X100");
		assertThat(x100Category.getDescription()).isEqualTo("Ze category X100");
		assertThat(x100Category.getLabel()).isEqualTo("X100");

		ClassificationPlanReportModel_Category x110Category = x100Category.getCategories().get(0);
		assertThat(x110Category.getCode()).isEqualTo("X110");
		assertThat(x110Category.getDescription()).isEqualTo("Ze category X110");
		assertThat(x110Category.getLabel()).isEqualTo("X110");
		assertThat(x110Category.getCategories()).isEmpty();

		ClassificationPlanReportModel_Category x120Category = x100Category.getCategories().get(1);
		assertThat(x120Category.getCode()).isEqualTo("X120");
		assertThat(x120Category.getDescription()).isEqualTo("Ze category X120");
		assertThat(x120Category.getLabel()).isEqualTo("X120");
		assertThat(x120Category.getCategories()).isEmpty();

		ClassificationPlanReportModel_Category x13Category = xeCategory.getCategories().get(1);
		assertThat(x13Category.getCode()).isEqualTo("X13");
		assertThat(x13Category.getDescription()).isEqualTo("218. Requiem pour un espion");
		assertThat(x13Category.getLabel()).isEqualTo("Agent Secreet");
		assertThat(x13Category.getCategories()).isEmpty();

		ClassificationPlanReportModel_Category zeCategory = categories.get(1);
		assertThat(zeCategory.getCode()).isEqualTo("Z");
		assertThat(zeCategory.getDescription()).isEqualTo("Ze ultimate category Z");
		assertThat(zeCategory.getLabel()).isEqualTo("Ze category");

		ClassificationPlanReportModel_Category z100Category = zeCategory.getCategories().get(0);
		assertThat(z100Category.getCode()).isEqualTo("Z100");
		assertThat(z100Category.getDescription()).isEqualTo("Ze category Z100");
		assertThat(z100Category.getLabel()).isEqualTo("Z100");

		ClassificationPlanReportModel_Category z110Category = z100Category.getCategories().get(0);
		assertThat(z110Category.getCode()).isEqualTo("Z110");
		assertThat(z110Category.getDescription()).isEqualTo("Ze category Z110");
		assertThat(z110Category.getLabel()).isEqualTo("Z110");

		ClassificationPlanReportModel_Category z111Category = z110Category.getCategories().get(0);
		assertThat(z111Category.getCode()).isEqualTo("Z111");
		assertThat(z111Category.getDescription()).isEqualTo("Ze category Z111");
		assertThat(z111Category.getLabel()).isEqualTo("Z111");
		assertThat(z111Category.getCategories()).isEmpty();

		ClassificationPlanReportModel_Category z112Category = z110Category.getCategories().get(1);
		assertThat(z112Category.getCode()).isEqualTo("Z112");
		assertThat(z112Category.getDescription()).isEqualTo("Ze category Z112");
		assertThat(z112Category.getLabel()).isEqualTo("Z112");
		assertThat(z112Category.getCategories()).isEmpty();

		ClassificationPlanReportModel_Category z120Category = z100Category.getCategories().get(1);
		assertThat(z120Category.getCode()).isEqualTo("Z120");
		assertThat(z120Category.getDescription()).isEqualTo("Ze category Z120");
		assertThat(z120Category.getLabel()).isEqualTo("Z120");
		assertThat(z120Category.getCategories()).isEmpty();

		ClassificationPlanReportModel_Category z200Category = zeCategory.getCategories().get(1);
		assertThat(z200Category.getCode()).isEqualTo("Z200");
		assertThat(z200Category.getDescription()).isEqualTo("Ze category Z200");
		assertThat(z200Category.getLabel()).isEqualTo("Z200");
		assertThat(z200Category.getCategories()).isEmpty();

		ClassificationPlanReportModel_Category z999Category = zeCategory.getCategories().get(2);
		assertThat(z999Category.getCode()).isEqualTo("Z999");
		assertThat(z999Category.getDescription()).isEqualTo("Ze category Z999");
		assertThat(z999Category.getLabel()).isEqualTo("Z999");
		assertThat(z999Category.getCategories()).isEmpty();

		ClassificationPlanReportModel_Category ze42Category = zeCategory.getCategories().get(3);
		assertThat(ze42Category.getCode()).isEqualTo("ZE42");
		assertThat(ze42Category.getDescription()).isEqualTo("Ze category 42");
		assertThat(ze42Category.getLabel()).isEqualTo("Ze 42");
		assertThat(ze42Category.getCategories()).isEmpty();

		build(new ClassificationPlanReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}
}