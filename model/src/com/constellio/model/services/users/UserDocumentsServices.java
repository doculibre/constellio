package com.constellio.model.services.users;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class UserDocumentsServices {

	private UserServices userServices;
	private SystemConfigurationsManager systemConfigurationsManager;

	public UserDocumentsServices(ModelLayerFactory modelLayerFactory) {
		userServices = modelLayerFactory.newUserServices();
		systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
	}

	public double getTotalSize(String username, String collection) {
		User user = userServices.getUserInCollection(username, collection);
		return user.get(User.USER_DOCUMENT_SIZE_SUM);
	}

	public boolean isSpaceLimitReached(String username, String collection, double userDocumentSize) {
		return isQuotaSpaceConfigActivated() &&
			   convertToMegaByte(userDocumentSize) > getAvailableSpaceInMegaBytes(username, collection);
	}

	public double getAvailableSpaceInMegaBytes(String username, String collection) {
		double usedSpace = getTotalSize(username, collection);
		double availableSpace = getSpaceQuota() - convertToMegaByte(usedSpace);
		return Math.max(0, availableSpace);
	}

	private boolean isQuotaSpaceConfigActivated() {
		return getSpaceQuota() >= 0;
	}

	private int getSpaceQuota() {
		ConstellioEIMConfigs configs = new ConstellioEIMConfigs(systemConfigurationsManager);
		return configs.getSpaceQuotaForUserDocuments();
	}

	private double convertToMegaByte(double valueInMegaBytes) {
		return (valueInMegaBytes * Math.pow(10, 6));
	}

}
