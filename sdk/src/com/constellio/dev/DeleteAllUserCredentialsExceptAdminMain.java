package com.constellio.dev;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.SDKScriptUtils;

public class DeleteAllUserCredentialsExceptAdminMain {

	static AppLayerFactory appLayerFactory;

	private static void startBackend() {
		//TODO

		//Only enable this line to run in production
		//appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
		appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();

	}

	public static void main(String argv[])
			throws Exception {

		RecordPopulateServices.LOG_CONTENT_MISSING = false;
		startBackend();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		RolesManager rolesManager = modelLayerFactory.getRolesManager();
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();

		CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
		for (String collection : collectionsListManager.getCollections()) {
			Roles collectionRoles = rolesManager.getCollectionRoles(collection);
			MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
			MetadataSchema userSchema = types.getSchema(User.DEFAULT_SCHEMA);
			LogicalSearchQuery collectionUsersQuery = new LogicalSearchQuery().setCondition(from(userSchema).returnAll());
			List<Record> collectionUserRecords = searchServices.search(collectionUsersQuery);
			for (Record collectionUserRecord : collectionUserRecords) {
				User collectionUser = new User(collectionUserRecord, types, collectionRoles);
				String username = collectionUser.getUsername();
				if (!username.equalsIgnoreCase(User.ADMIN)) {
					recordServices.logicallyDelete(collectionUserRecord, User.GOD);
				}
			}
		}

		List<UserCredential> userCredentials = userServices.getAllUserCredentials();
		for (UserCredential userCredential : userCredentials) {
			String username = userCredential.getUsername();
			if (!username.equalsIgnoreCase(User.ADMIN)) {
				List<String> globalGroups = userCredential.getGlobalGroups();
				for (String globalGroup : globalGroups) {
					userServices.removeUserFromGlobalGroup(username, globalGroup);
				}
				globalGroups = new ArrayList<>();

				List<String> userCollections = userCredential.getCollections();
				for (String userCollection : userCollections) {
					userServices.removeUserFromCollection(userCredential, userCollection);
				}
				userCollections = new ArrayList<>();

				System.out.println("Deleting user " + userCredential.getUsername());
				UserCredential newUserCredential = userServices.createUserCredential(username, userCredential.getFirstName(),
						userCredential.getLastName(), userCredential.getEmail(), userCredential.getServiceKey(),
						userCredential.isSystemAdmin(), globalGroups, userCollections,
						userCredential.getAccessTokens(), UserCredentialStatus.DELETED,
						userCredential.getDomain(), Arrays.asList(""), null);
				userCredential = newUserCredential;
				userServices.removeUserCredentialAndUser(userCredential);
			}
		}

		ReindexingServices reindexingServices = modelLayerFactory.newReindexingServices();
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		File tempXmlFile = File.createTempFile(DeleteAllUserCredentialsExceptAdminMain.class.getSimpleName(), ".xml");

		ConfigManager configManager = modelLayerFactory.getDataLayerFactory().getConfigManager();

		String userCredentialsXmlFilePath = "/userCredentialsConfig.xml";
		Document userCredentialsDocument = configManager.getXML(userCredentialsXmlFilePath).getDocument();
		Element rootElement = userCredentialsDocument.getRootElement();
		for (Iterator<Element> it = rootElement.getChildren().iterator(); it.hasNext(); ) {
			Element userCredentialElement = it.next();
			String username = userCredentialElement.getAttributeValue("username");
			if (!username.equals(User.ADMIN)) {
				it.remove();
			}
		}

		XMLOutputter xmlOutput = new XMLOutputter();
		// display nice nice
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(userCredentialsDocument, new FileWriter(tempXmlFile));

		String hash = configManager.getBinary(userCredentialsXmlFilePath).getHash();
		InputStream tempXmlFileIn = new FileInputStream(tempXmlFile);
		configManager.update(userCredentialsXmlFilePath, hash, tempXmlFileIn);
		IOUtils.closeQuietly(tempXmlFileIn);

		System.out.println("End of script");
	}

}
