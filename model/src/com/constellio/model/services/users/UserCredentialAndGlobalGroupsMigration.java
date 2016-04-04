package com.constellio.model.services.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;

public class UserCredentialAndGlobalGroupsMigration {

	XmlUserCredentialsManager oldUserManager;
	XmlGlobalGroupsManager oldGroupManager;
	RecordServices recordServices;
	SchemasRecordsServices schemasRecordsServices;

	public UserCredentialAndGlobalGroupsMigration(XmlUserCredentialsManager oldUserManager,
			XmlGlobalGroupsManager oldGroupManager, RecordServices recordServices,
			SchemasRecordsServices schemasRecordsServices) {
		this.oldUserManager = oldUserManager;
		this.oldGroupManager = oldGroupManager;
		this.recordServices = recordServices;
		this.schemasRecordsServices = schemasRecordsServices;
	}

	public void migrateUserAndGroups() {

		Map<String, SolrGlobalGroup> groupsCodeIdMap = new HashMap<>();
		Transaction transaction = new Transaction();
		for (GlobalGroup globalGroup : oldGroupManager.getAllGroups()) {
			SolrGlobalGroup newGroup = (SolrGlobalGroup) schemasRecordsServices.newGlobalGroup();
			groupsCodeIdMap.put(globalGroup.getCode(), newGroup);
			transaction.add(newGroup);
		}

		for (GlobalGroup oldGroup : oldGroupManager.getAllGroups()) {
			SolrGlobalGroup newGroup = groupsCodeIdMap.get(oldGroup.getCode());
			newGroup.setCode(oldGroup.getCode());
			newGroup.setName(oldGroup.getName());
			newGroup.setTitle(oldGroup.getName());
			newGroup.setStatus(oldGroup.getStatus());
			newGroup.setUsersAutomaticallyAddedToCollections(oldGroup.getUsersAutomaticallyAddedToCollections());
			if (oldGroup.getParent() != null) {
				newGroup.setParent(groupsCodeIdMap.get(oldGroup.getParent()).getCode());
			}
		}

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		Iterator<List<UserCredential>> userCredentialBatchesIterator = new BatchBuilderIterator<>(
				oldUserManager.getUserCredentials().iterator(), 100);

		while (userCredentialBatchesIterator.hasNext()) {
			transaction = new Transaction();
			for (UserCredential userCredential : userCredentialBatchesIterator.next()) {
				transaction.add(toSolrUserCredential(userCredential, groupsCodeIdMap));
			}

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private SolrUserCredential toSolrUserCredential(UserCredential userCredential, Map<String, SolrGlobalGroup> groupsCodeMap) {
		SolrUserCredential newUserCredential = (SolrUserCredential) schemasRecordsServices.newCredential();
		newUserCredential.setFirstName(userCredential.getFirstName());
		newUserCredential.setLastName(userCredential.getLastName());
		newUserCredential.setUsername(userCredential.getUsername());
		newUserCredential.setDn(userCredential.getDn());
		newUserCredential.setDomain(userCredential.getDomain());
		newUserCredential.setEmail(userCredential.getEmail());
		newUserCredential.setAccessTokens(userCredential.getAccessTokens());
		newUserCredential.setCollections(userCredential.getCollections());
		newUserCredential.setMsExchDelegateListBL(userCredential.getMsExchDelegateListBL());
		newUserCredential.setStatus(userCredential.getStatus());
		newUserCredential.setServiceKey(userCredential.getServiceKey());
		newUserCredential.setSystemAdmin(userCredential.isSystemAdmin());

		List<String> groupIds = new ArrayList<>();
		for (String groupCode : userCredential.getGlobalGroups()) {
			groupIds.add(groupsCodeMap.get(groupCode).getId());
		}
		newUserCredential.setGlobalGroups(groupIds);

		return newUserCredential;
	}

}
