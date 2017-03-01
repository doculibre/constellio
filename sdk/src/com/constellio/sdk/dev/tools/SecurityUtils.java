package com.constellio.sdk.dev.tools;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SecurityUtils {

<<<<<<< HEAD
	public static String printSecurityReport(String collection, AppLayerFactory appLayerFactory) {

		List<User> usersInCollection = appLayerFactory.getModelLayerFactory().newUserServices()
				.getAllUsersInCollection(collection);

		Iterator<User> removeInvalids = usersInCollection.iterator();

		while (removeInvalids.hasNext()) {
			User user = removeInvalids.next();
			if (user == null || user.getUsername() == null) {
				removeInvalids.remove();
			}
		}

		Collections.sort(usersInCollection, new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return o1.getUsername().compareTo(o2.getUsername());
			}
		});

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(asList(rm.folderSchemaType(), rm.documentSchemaType(), tasks.userTask.schemaType()))
						.returnAll());

		Iterator<Record> recordsIterator = searchServices.recordsIterator(query, 10000);

		StringBuilder stringBuilder = new StringBuilder();
		while (recordsIterator.hasNext()) {
			Record record = recordsIterator.next();
			stringBuilder.append(record.getId());
			stringBuilder.append("  ");

			for (User user : usersInCollection) {
				String accesses = "";

				if (user.hasReadAccess().on(record)) {
					accesses += "R";
				}
				if (user.hasWriteAccess().on(record)) {
					accesses += "W";
				}
				if (user.hasDeleteAccess().on(record)) {
					accesses += "D";
				}

				if (!accesses.isEmpty()) {
					stringBuilder.append(user.getUsername() + ":" + accesses + "\t");
				}
			}

			stringBuilder.append("\n");
		}

		return stringBuilder.toString();
	}
=======
    public static String printSecurityReport(String collection, AppLayerFactory appLayerFactory) {

        List<User> usersInCollection = appLayerFactory.getModelLayerFactory().newUserServices()
                .getAllUsersInCollection(collection);

        Iterator<User> removeInvalids = usersInCollection.iterator();

        while (removeInvalids.hasNext()) {
            User user = removeInvalids.next();
            if (user == null || user.getUsername() == null) {
                removeInvalids.remove();
            }
        }

        Collections.sort(usersInCollection, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });

        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
        LogicalSearchQuery query = new LogicalSearchQuery(
                from(asList(rm.folderSchemaType(), rm.documentSchemaType(), tasks.userTask.schemaType()))
                        .returnAll());

        Iterator<Record> recordsIterator = searchServices.recordsIterator(query, 10000);

        StringBuilder stringBuilder = new StringBuilder();
        while (recordsIterator.hasNext()) {
            Record record = recordsIterator.next();
            stringBuilder.append(record.getId());
            stringBuilder.append("  ");

            for (User user : usersInCollection) {
                String accesses = "";

                if (user.hasReadAccess().on(record)) {
                    accesses += "R";
                }
                if (user.hasWriteAccess().on(record)) {
                    accesses += "W";
                }
                if (user.hasDeleteAccess().on(record)) {
                    accesses += "D";
                }

                if (!accesses.isEmpty()) {
                    stringBuilder.append(user.getUsername() + ":" + accesses + "\t");
                }
            }

            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
>>>>>>> importation-charles2

}
