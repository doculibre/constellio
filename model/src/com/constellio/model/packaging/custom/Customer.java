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
package com.constellio.model.packaging.custom;

import org.joda.time.LocalDateTime;

public class Customer {

	private String code;

	private String name;

	private String licensePackage;

	private LocalDateTime installationDate;

	private LocalDateTime supportPlanStart;

	private LocalDateTime supportPlanEnd;

	private String plan;

	public Customer() {

	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public String getLicensePackage() {
		return licensePackage;
	}

	public void setLicensePackage(String licensePackage) {
		this.licensePackage = licensePackage;
	}

	public LocalDateTime getInstallationDate() {
		return installationDate;
	}

	public void setInstallationDate(LocalDateTime installationDate) {
		this.installationDate = installationDate;
	}

	public LocalDateTime getSupportPlanStart() {
		return supportPlanStart;
	}

	public void setSupportPlanStart(LocalDateTime supportPlanStart) {
		this.supportPlanStart = supportPlanStart;
	}

	public LocalDateTime getSupportPlanEnd() {
		return supportPlanEnd;
	}

	public void setSupportPlanEnd(LocalDateTime supportPlanEnd) {
		this.supportPlanEnd = supportPlanEnd;
	}

}
