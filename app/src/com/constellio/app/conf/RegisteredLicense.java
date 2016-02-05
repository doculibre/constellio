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
