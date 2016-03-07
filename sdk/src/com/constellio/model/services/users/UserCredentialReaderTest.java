package com.constellio.model.services.users;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

public class UserCredentialReaderTest extends ConstellioTest {

	Document document;
	UserCredentialsWriter writer;
	UserCredentialsReader reader;
	UserCredential chuckUserCredential;
	UserCredential edouardUserCredential;

	LocalDateTime endDateTime;

	@Mock Factory<EncryptionServices> encryptionServicesFactory;
	List<String> msExchDelegateListBL = new ArrayList<>();

	@Before
	public void setup()
			throws Exception {

		EncryptionServices encryptionServices = FakeEncryptionServicesUtils.create();
		when(encryptionServicesFactory.get()).thenReturn(encryptionServices);

		msExchDelegateListBL = new ArrayList<>();
		msExchDelegateListBL.add("msExchDelegateListBL1");
		msExchDelegateListBL.add("msExchDelegateListBL2");

		newChuckUserCredential();
		newEdouardUserCredential();

		Document document = new Document();
		writer = new UserCredentialsWriter(document, encryptionServicesFactory);

		writer.createEmptyUserCredentials();
		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(edouardUserCredential);
		writer.addUpdate(chuckUserCredential);
		reader = new UserCredentialsReader(document, encryptionServicesFactory);
	}

	@Test
	public void givenTwoUsersCredentialsWhenReadAllThenGetThem()
			throws Exception {
		Map<String, UserCredential> usersCredentials = reader.readAll(asList("zeCollection"));

		assertThat(usersCredentials).hasSize(2);
		assertThat(usersCredentials.containsKey("chuck")).isTrue();
		assertThat(usersCredentials.containsKey("edouard")).isTrue();
		assertThat(usersCredentials.get("chuck").getMsExchDelegateListBL()).isEqualTo(msExchDelegateListBL);
		assertThat(usersCredentials.get("chuck").getDn()).isEqualTo("chuckDN");
	}

	private void newChuckUserCredential() {

		Map<String, LocalDateTime> tokens = new HashMap<String, LocalDateTime>();
		endDateTime = new LocalDateTime(2014, 11, 04, 10, 30);
		tokens.put("token1", endDateTime);
		chuckUserCredential = new XmlUserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", "serviceKeyChuck", false,
				asList("group1"), asList(zeCollection), tokens, UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, "chuckDN");
	}

	private void newEdouardUserCredential() {
		edouardUserCredential = new XmlUserCredential("edouard", "Edouard", "Lechat", "edouard.lechat@gmail.com",
				asList("group1"), asList("collection1"), UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);
	}
}
