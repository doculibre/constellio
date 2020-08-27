package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.users.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This script is separeted from CoreMigrationTo_9_2 to be called AFTER all scripts of version 9.2
 */
public class CoreMigrationTo_9_2_0_1 extends MigrationHelper implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_9_2_0_1.class);

	@Override
	public String getVersion() {
		return "9.2.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		//if (Collection.SYSTEM_COLLECTION.equals(collection)) {
		deleteUnusedUsers(appLayerFactory);
		//}
	}


	private void deleteUnusedUsers(AppLayerFactory appLayerFactory) {
		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();

		List<RecordDTO> userIdsToDelete = new ArrayList<>();

		userServices.streamUserInfos()
				.filter(u -> u.getCollections().isEmpty() && !u.getUsername().equals("admin"))
				.forEach(u -> {
					userIdsToDelete.add(userServices.getUserCredential(u.getUsername()).getWrappedRecord().getRecordDTO());
				});

		if (!userIdsToDelete.isEmpty()) {
			LOGGER.info("Deleting " + userIdsToDelete.size() + " user credentials");
			try {
				appLayerFactory.getModelLayerFactory().getDataLayerFactory().newRecordDao().execute(
						new TransactionDTO(RecordsFlushing.NOW()).withDeletedRecords(userIdsToDelete));

			} catch (OptimisticLocking e) {
				LOGGER.warn("Problem while deleting unused user, just cancelling", e);
			}

			appLayerFactory.getModelLayerFactory().getRecordsCaches().getCache(Collection.SYSTEM_COLLECTION)
					.removeFromAllCaches(userIdsToDelete.stream().map(RecordDTO::getId).collect(toList()));
		}
	}

}
