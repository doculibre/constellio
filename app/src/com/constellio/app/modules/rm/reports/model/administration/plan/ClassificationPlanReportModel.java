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
package com.constellio.app.modules.rm.reports.model.administration.plan;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.constellio.data.io.streamFactories.StreamFactory;

public class ClassificationPlanReportModel {

	private boolean detailed = false;

	private StreamFactory<InputStream> headerLogo;

	private List<ClassificationPlanReportModel_Category> rootCategories = new ArrayList<>();

	public StreamFactory<InputStream> getHeaderLogo() {
		return headerLogo;
	}

	public ClassificationPlanReportModel setHeaderLogo(StreamFactory<InputStream> headerLogo) {
		this.headerLogo = headerLogo;
		return this;
	}

	public List<ClassificationPlanReportModel_Category> getRootCategories() {
		return rootCategories;
	}

	public ClassificationPlanReportModel setRootCategories(List<ClassificationPlanReportModel_Category> rootCategories) {
		this.rootCategories = rootCategories;
		return this;
	}

	public boolean isDetailed() {
		return detailed;
	}

	public void setDetailed(boolean detailed) {
		this.detailed = detailed;
	}

	public String getTitle() {
		if (isDetailed()) {
			return "Plan de classification detaille";
		} else {
			return "Plan de classification";
		}
	}

	public static class ClassificationPlanReportModel_Category {

		private String code;

		private String label;

		private String description;

		private List<ClassificationPlanReportModel_Category> categories = new ArrayList<>();

		public String getCode() {
			return code;
		}

		public ClassificationPlanReportModel_Category setCode(String code) {
			this.code = code;
			return this;
		}

		public String getLabel() {
			return label;
		}

		public ClassificationPlanReportModel_Category setLabel(String label) {
			this.label = label;
			return this;
		}

		public String getDescription() {
			return description;
		}

		public ClassificationPlanReportModel_Category setDescription(String description) {
			this.description = description;
			return this;
		}

		public List<ClassificationPlanReportModel_Category> getCategories() {
			return categories;
		}

		public ClassificationPlanReportModel_Category setCategories(List<ClassificationPlanReportModel_Category> categories) {
			this.categories = categories;
			return this;
		}
	}

}
