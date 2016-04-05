package com.constellio.model.services.users;

import static com.constellio.model.services.users.UserCredentialsWriter.DN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.utils.Factory;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.XmlUserCredential;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.sdk.tests.ConstellioTest;

public class UserCredentialsWriterTest extends ConstellioTest {

	@Mock Factory<EncryptionServices> encryptionServicesFactory;

	private static final String COLLECTIONS = "collections";
	private static final String TOKENS = "tokens";
	private static final String GLOBAL_GROUP = "globalGroup";
	private static final String GLOBAL_GROUPS = "globalGroups";
	private static final String EMAIL = "email";
	private static final String LAST_NAME = "lastName";
	private static final String FIRST_NAME = "firstName";
	private static final String USERNAME = "username";
	private static final String STATUS = "status";
	private static final String DOMAIN = "domain";
	private static final String MS_EXCH_DELEGATE_LIST_BL = "msExchDelegateListBL";
	UserCredentialsWriter writer;
	Document document;
	UserCredential chuckUserCredential;
	UserCredential dakotaUserCredential;
	LocalDateTime endDate;

	Map<String, LocalDateTime> tokens;
	List<String> msExchDelegateListBL = new ArrayList<>();

	@Before
	public void setup()
			throws Exception {

		EncryptionServices encryptionServices = FakeEncryptionServicesUtils.create();
		when(encryptionServicesFactory.get()).thenReturn(encryptionServices);

		document = new Document();
		writer = new UserCredentialsWriter(document, encryptionServicesFactory);
		writer.createEmptyUserCredentials();

		msExchDelegateListBL.add("msExchDelegateListBL1");
		msExchDelegateListBL.add("msExchDelegateListBL2");

		tokens = new HashMap<String, LocalDateTime>();
		endDate = new LocalDateTime(2014, 11, 04, 10, 30);
		tokens.put("token1", endDate);
		chuckUserCredential = new XmlUserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", "serviceKeyChuck", false,
				Arrays.asList("group1"),
				Arrays.asList(zeCollection), tokens, UserCredentialStatus.ACTIVE, "", msExchDelegateListBL, "dnChuck");
		dakotaUserCredential = new XmlUserCredential("dakota", "Dakota", "Lindien", "dakota.lindien@gmail.com",
				Arrays.asList("group1"), Arrays.asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "",
				msExchDelegateListBL, "dakotaDN");
	}

	@Test
	public void whenCreateEmptyUserCredentialsThenItIsCreated()
			throws Exception {

		assertThat(document.getRootElement().getChildren()).isEmpty();
	}

	@Test
	public void whenAddUserCredentialThenItIsAdded()
			throws Exception {

		writer.addUpdate(chuckUserCredential);

		assertThat(document.getRootElement().getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChildren().get(0).getAttributeValue(USERNAME)).isEqualTo(
				chuckUserCredential.getUsername());
		assertThat(document.getRootElement().getChildren().get(0).getChild(FIRST_NAME).getText()).isEqualTo(
				chuckUserCredential.getFirstName());
		assertThat(document.getRootElement().getChildren().get(0).getChild(LAST_NAME).getText()).isEqualTo(
				chuckUserCredential.getLastName());
		assertThat(document.getRootElement().getChildren().get(0).getChild(EMAIL).getText()).isEqualTo(
				chuckUserCredential.getEmail());
		assertThat(document.getRootElement().getChildren().get(0).getChild(GLOBAL_GROUPS).getChild(GLOBAL_GROUP).getText())
				.isEqualTo(chuckUserCredential.getGlobalGroups().get(0));
		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren().get(0).getText()).isEqualTo(
				chuckUserCredential.getCollections().get(0));
		assertThat(document.getRootElement().getChildren().get(0).getChild(STATUS).getText()).isEqualTo(
				String.valueOf(chuckUserCredential.getStatus().getCode()));
		assertThat(document.getRootElement().getChildren().get(0).getChild(DOMAIN).getText()).isEqualTo(
				chuckUserCredential.getDomain());
		assertThat(document.getRootElement().getChildren().get(0).getChild(TOKENS).getChildren().get(0).getChildren().get(0)
				.getText()).isEqualTo(
				"$token1");
		assertThat(document.getRootElement().getChildren().get(0).getChild(TOKENS).getChildren().get(0).getChildren().get(1)
				.getText()).isEqualTo(
				endDate.toString());
		assertThat(
				document.getRootElement().getChildren().get(0).getChild(MS_EXCH_DELEGATE_LIST_BL).getChildren().get(0).getText())
				.isEqualTo(
						chuckUserCredential.getMsExchDelegateListBL().get(0));
	}

	@Test
	public void givenUserCredentialWhenUpdateUserCredentialThenItIsUpdated()
			throws Exception {

		writer.addUpdate(chuckUserCredential);

		chuckUserCredential = new XmlUserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", Arrays.asList("group1"),
				Arrays.asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "", msExchDelegateListBL, "chuckDN");

		writer.addUpdate(chuckUserCredential);
		assertThat(document.getRootElement().getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChildren().get(0).getAttributeValue(USERNAME)).isEqualTo(
				chuckUserCredential.getUsername());
		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren().get(0).getText()).isEqualTo(
				chuckUserCredential.getCollections().get(0));
		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren().get(1).getText()).isEqualTo(
				chuckUserCredential.getCollections().get(1));
	}

	@Test
	public void givenUserWhenRemoveCollectionThenItIsRemoved()
			throws Exception {

		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(dakotaUserCredential);

		writer.removeCollection(zeCollection);

		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren()).isEmpty();
		assertThat(document.getRootElement().getChildren().get(1).getChild(COLLECTIONS).getChildren().get(0).getText()).isEqualTo(
				"collection1");
	}

	@Test
	public void givenUserWhenRemoveTokenThenItIsRemoved()
			throws Exception {

		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(dakotaUserCredential);

		assertThat(document.getRootElement().getChildren().get(0).getChild(TOKENS).getChildren()).isNotEmpty();
		writer.removeToken("token1");

		assertThat(document.getRootElement().getChildren().get(0).getChild(TOKENS).getChildren()).isEmpty();
	}

	@Test
	public void givenUserWhenRemoveHimFromCollectionThenHeIsRemoved()
			throws Exception {

		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(dakotaUserCredential);

		writer.removeUserFromCollection(chuckUserCredential, zeCollection);

		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren()).isEmpty();
		assertThat(document.getRootElement().getChildren().get(1).getChild(COLLECTIONS).getChildren().get(0).getText()).isEqualTo(
				zeCollection);
		assertThat(document.getRootElement().getChildren().get(1).getChild(COLLECTIONS).getChildren().get(1).getText()).isEqualTo(
				"collection1");
	}

	@Test
	public void givenUsersWithGroupWhenRemoveGroupThenItIsRemoved()
			throws Exception {

		chuckUserCredential = new XmlUserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", Arrays.asList("group1",
				"group2"), Arrays.asList(zeCollection), UserCredentialStatus.ACTIVE, "", msExchDelegateListBL, "chuckDN");
		dakotaUserCredential = new XmlUserCredential("dakota", "Dakota", "Lindien", "dakota.lindien@gmail.com", Arrays.asList(
				"group1", "group2"), Arrays.asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "",
				msExchDelegateListBL, "dakotaDN");
		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(dakotaUserCredential);

		writer.removeGroup("group1");

		assertThat(document.getRootElement().getChildren().get(0).getChild(GLOBAL_GROUPS).getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChildren().get(0).getChild(GLOBAL_GROUPS).getChild(GLOBAL_GROUP).getText())
				.isEqualTo("group2");
		assertThat(document.getRootElement().getChildren().get(1).getChild(GLOBAL_GROUPS).getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChildren().get(1).getChild(GLOBAL_GROUPS).getChild(GLOBAL_GROUP).getText())
				.isEqualTo("group2");
		assertThat(document.getRootElement().getChildren().get(1).getChild(DN).getText())
				.isEqualTo("dakotaDN");
	}
}
