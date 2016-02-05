package com.constellio.model.conf;

import org.joda.time.LocalDateTime;

//CHECKSTYLE:OFF
public class ConstellioLicense {

	protected static LocalDateTime installationDate;

	protected static LocalDateTime supportPlanStart;

	protected static LocalDateTime supportPlanEnd;

	protected static String customerName;

	protected static String customerCode;

	protected static String supportPlan;

	public static LocalDateTime getInstallationDate() {
		return installationDate;
	}

	public static LocalDateTime getSupportPlanStart() {
		return supportPlanStart;
	}

	public static LocalDateTime getSupportPlanEnd() {
		return supportPlanEnd;
	}

	public static String getLicenseClientName() {
		return customerName;
	}

	public static String getLicenseClientCode() {
		return customerCode;
	}

	public static String getSupportPlanName() {
		return supportPlan;
	}

}
// CHECKSTYLE:ON