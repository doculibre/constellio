package com.constellio.model.services.users;

import static com.constellio.model.services.users.UserUtils.toCacheKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDateTime;

import com.constellio.data.utils.Factory;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.XmlUserCredential;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class UserCredentialsReader {

	private static final String COLLECTIONS = "collections";
	private static final String GLOBAL_GROUPS = "globalGroups";
	private static final String EMAIL = "email";
	private static final String LAST_NAME = "lastName";
	private static final String FIRST_NAME = "firstName";
	private static final String USERNAME = "username";
	private static final String SERVICE_KEY = "serviceKey";
	private static final String TOKENS = "tokens";
	private static final String TOKEN_ID = "tokenId";
	private static final String TOKEN_END_DATE = "tokenEndDate";
	private static final String SYSTEM_ADMIN = "systemAdmin";
	private static final String STATUS = "status";
	private static final String DOMAIN = "domain";
	private static final String MS_EXCH_DELEGATE_LIST_BL = "msExchDelegateListBL";
	private static final String MS_EXCH_DELEGATE_BL = "msExchDelegateBL";
	private static final String DN = UserCredentialsWriter.DN;
	Document document;
	int documentVersion;
	Factory<EncryptionServices> encryptionServicesFactory;

	public UserCredentialsReader(Document document, Factory<EncryptionServices> encryptionServicesFactory) {
		this.document = document;
		this.encryptionServicesFactory = encryptionServicesFactory;
	}

	public Map<String, UserCredential> readAll(List<String> collections) {
		readVersion();
		UserCredential userCredential;
		Map<String, UserCredential> usersCredentials = new HashMap<>();
		Element usersCredentialsElements = document.getRootElement();
		for (Element userCredentialElement : usersCredentialsElements.getChildren()) {
			userCredential = createUserCredentialObject(userCredentialElement, collections);

			usersCredentials.put(toCacheKey(userCredential.getUsername()), userCredential);
		}
		return usersCredentials;
	}

	private void readVersion() {
		String version = document.getRootElement().getAttributeValue("apiVersion");
		if (version == null) {
			documentVersion = 0;
		} else {
			documentVersion = Integer.valueOf(version);
		}
	}

	private UserCredential createUserCredentialObject(Element userCredentialElement, List<String> allCollections) {
		UserCredential userCredential;
		String username = userCredentialElement.getAttributeValue(USERNAME);
		String firstName = userCredentialElement.getChildText(FIRST_NAME);
		String lastName = userCredentialElement.getChildText(LAST_NAME);
		String email = userCredentialElement.getChildText(EMAIL);
		String serviceKey = userCredentialElement.getChildText(SERVICE_KEY);
		if ("null".equals(serviceKey)) {
			serviceKey = null;
		}
		Map<String, LocalDateTime> tokens = new HashMap<>();
		Element tokensElements = userCredentialElement.getChild(TOKENS);
		for (Element tokenElement : tokensElements.getChildren()) {
			String tokenId = tokenElement.getChildText(TOKEN_ID);

			if (documentVersion > 1) {
				tokenId = encryptionServicesFactory.get().decrypt(tokenId);
			}

			String tokenEndDate = tokenElement.getChildText(TOKEN_END_DATE);
			LocalDateTime endDateTime = LocalDateTime.parse(tokenEndDate);
			tokens.put(tokenId, endDateTime);
		}
		boolean systemAdmin = "true".equals(userCredentialElement.getChildText(SYSTEM_ADMIN));
		Element globalGroupsElements = userCredentialElement.getChild(GLOBAL_GROUPS);
		List<String> globalGroups = new ArrayList<>();
		for (Element globalGroupElement : globalGroupsElements.getChildren()) {
			globalGroups.add(globalGroupElement.getText());
		}
		Element collectionsElements = userCredentialElement.getChild(COLLECTIONS);
		List<String> collections = new ArrayList<>();
		for (Element collectionElement : collectionsElements.getChildren()) {
			if (allCollections.contains(collectionElement.getText())) {
				collections.add(collectionElement.getText());
			}
		}
		List<String> msExchDelegateListBL = new ArrayList<>();
		Element msExchDelegateListBLElements = userCredentialElement.getChild(MS_EXCH_DELEGATE_LIST_BL);
		if (msExchDelegateListBLElements != null) {
			for (Element msExchDelegateBLElement : msExchDelegateListBLElements.getChildren()) {
				msExchDelegateListBL.add(msExchDelegateBLElement.getText());
			}
		}

		String dn = userCredentialElement.getChildText(DN);
		if ("null".equals(dn)) {
			dn = null;
		}

		UserCredentialStatus status;
		String statusStr = userCredentialElement.getChildText(STATUS);
		if (statusStr != null) {
			status = (UserCredentialStatus) EnumWithSmallCodeUtils.toEnumWithSmallCode(UserCredentialStatus.class, statusStr);
		} else {
			status = UserCredentialStatus.ACTIVE;
		}
		String domain = userCredentialElement.getChildText(DOMAIN);
		userCredential = new XmlUserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups,
				collections, tokens, status, domain, msExchDelegateListBL, dn);
		return userCredential;
	}
}
