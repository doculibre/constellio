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