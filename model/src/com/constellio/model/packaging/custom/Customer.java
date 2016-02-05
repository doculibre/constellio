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
