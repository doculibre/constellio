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
package com.constellio.app.conf;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.constellio.model.conf.ConstellioLicense;
import com.constellio.model.entities.modules.ConstellioPlugin;

public abstract class RegisteredLicense extends ConstellioLicense implements ConstellioPlugin {

	@Override
	public String getName() {
		return "License of " + getCustomerCode() + " (" + getCustomerName() + ")";
	}

	@Override
	public String getPublisher() {
		return DOCULIBRE;
	}

	//	@Override
	//	public void start(String collection, ModelLayerFactory modelLayerFactory,
	//			DataLayerFactory dataLayerFactory) {
	//		customerCode = getCustomerCode();
	//		customerName = getCustomerName();
	//		supportPlan = getSupportPlan();
	//		installationDate = parse(getInstallationDateYYYYMMDD());
	//		supportPlanStart = parse(getSupportPlanStartYYYYMMDD());
	//		supportPlanEnd = parse(getSupportPlanEndYYYYMMDD());
	//	}

	// private Date parse(String dateYYYYMMDD) {
	// if (dateYYYYMMDD == null) {
	// return null;
	// } else {
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	// try {
	// return sdf.parse(dateYYYYMMDD.replace("-", "").replace("_", "").replace("/", ""));
	// } catch (ParseException e) {
	// throw new RegisteredLicenseRuntimeException(e);
	// }
	// }
	// }

	private LocalDateTime parse(String dateYYYYMMDD) {
		if (dateYYYYMMDD == null) {
			return null;
		} else {
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
			return LocalDateTime.parse(dateYYYYMMDD.replace("-", "").replace("_", "").replace("/", ""), formatter);
		}
	}

	//	@Override
	//	public void stop(String collection, ModelLayerFactory modelLayerFactory,
	//			DataLayerFactory dataLayerFactory) {
	//		customerCode = null;
	//		customerName = null;
	//		supportPlan = null;
	//		installationDate = null;
	//		supportPlanStart = null;
	//		supportPlanEnd = null;
	//	}

	public abstract String getInstallationDateYYYYMMDD();

	public abstract String getSupportPlanStartYYYYMMDD();

	public abstract String getSupportPlanEndYYYYMMDD();

	public abstract String getCustomerName();

	public abstract String getCustomerCode();

	public abstract String getSupportPlan();

}
