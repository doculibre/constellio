package com.constellio.app.api.cmis.rm;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.chemistry.opencmis.commons.enums.PropertyType.DATETIME;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.STRING;
import static org.apache.chemistry.opencmis.commons.enums.Updatability.READONLY;
import static org.apache.chemistry.opencmis.commons.enums.Updatability.READWRITE;
import static org.assertj.core.api.Assertions.assertThat;

public class RMModuleTypesAcceptTest extends ConstellioTest {

	Session cmisSession;

	Users users = new Users();
	UserServices userServices;
	String chuckNorrisKey = "chuckNorris-key";
	String chuckNorrisToken;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule()
		);

		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);

		userServices.execute(
				userServices.addUpdate(chuckNorris).setServiceKey(chuckNorrisKey).setSystemAdminEnabled());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection(zeCollection));
		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@Test
	public void validateFolderType()
			throws Exception {

		ObjectType baseFolderType = cmisSession.getTypeDefinition("cmis:folder");
		Iterator<ObjectType> iterator = baseFolderType.getChildren().iterator();

		Map<String, PropertyDefinition<?>> folderTypeMetadatas = getFolderType("folder_default").getPropertyDefinitions();
		assertThat(folderTypeMetadatas.get(Folder.OPENING_DATE))
				.has(propertyType(DATETIME)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get(Folder.ACTUAL_TRANSFER_DATE))
				.has(propertyType(DATETIME)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get(Folder.RETENTION_RULE_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get(Folder.CATEGORY_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get(Folder.ADMINISTRATIVE_UNIT_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get(Folder.FILING_SPACE_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get(Folder.COPY_STATUS_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));

		assertThat(folderTypeMetadatas.get(Folder.EXPECTED_TRANSFER_DATE))
				.has(propertyType(DATETIME)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get(Folder.RETENTION_RULE))
				.has(propertyType(STRING)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get(Folder.CATEGORY))
				.has(propertyType(STRING)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get(Folder.ADMINISTRATIVE_UNIT))
				.has(propertyType(STRING)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get(Folder.FILING_SPACE))
				.has(propertyType(STRING)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get(Folder.COPY_STATUS))
				.has(propertyType(STRING)).has(updatability(READONLY));

		assertThat(getAllTypeIds()).containsOnly("document_email", "folder_default", "administrativeUnit_default", "taxonomy",
				"storageSpace_default", "document_default", "category_default", "collection_default", "containerRecord_default");

	}

	private Condition<? super PropertyDefinition<?>> updatability(final Updatability expectedValue) {
		return new Condition<PropertyDefinition<?>>() {
			@Override
			public boolean matches(PropertyDefinition<?> value) {
				assertThat(value.getUpdatability()).isEqualTo(expectedValue);
				return true;
			}
		};
	}

	private Condition<? super PropertyDefinition<?>> propertyType(final PropertyType expectedValue) {
		return new Condition<PropertyDefinition<?>>() {
			@Override
			public boolean matches(PropertyDefinition<?> value) {
				assertThat(value.getPropertyType().value()).isEqualTo(expectedValue.value());
				return true;
			}
		};
	}

	private List<String> getAllTypeIds() {
		List<String> ids = new ArrayList<>();
		ObjectType baseFolderType = cmisSession.getTypeDefinition("cmis:folder");
		Iterator<ObjectType> iterator = baseFolderType.getChildren().iterator();
		while (iterator.hasNext()) {
			ObjectType objectType = iterator.next();
			ids.add(objectType.getId());
		}
		return ids;
	}

	private ObjectType getFolderType(String id) {
		ObjectType baseFolderType = cmisSession.getTypeDefinition("cmis:folder");
		Iterator<ObjectType> iterator = baseFolderType.getChildren().iterator();
		while (iterator.hasNext()) {
			ObjectType objectType = iterator.next();
			if (id.equals(objectType.getId())) {
				return objectType;
			}
		}

		throw new RuntimeException("No such object type with id : " + id);
	}
}
