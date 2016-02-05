package com.constellio.app.api.cmis.builders.object;

import java.io.File;
import java.util.ArrayList;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;

public class AclBuilder {

	private final ConstellioCollectionRepository repository;

	public AclBuilder(ConstellioCollectionRepository repository) {
		this.repository = repository;
	}

	/**
	 * Compiles the ACL for a file or folder.
	 */
	public Acl build(File file) {
		AccessControlListImpl result = new AccessControlListImpl();
		result.setAces(new ArrayList<Ace>());

		//		for (Map.Entry<String, Boolean> ue : readWriteUserMap.entrySet()) {
		//			// create principal
		//			AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl();
		//			principal.setPrincipalId(ue.getKey());
		//
		//			// create ACE
		//			AccessControlEntryImpl entry = new AccessControlEntryImpl();
		//			entry.setPrincipal(principal);
		//			entry.setPermissions(new ArrayList<String>());
		//			entry.getPermissions().add(CMIS_READ);
		//			if (!ue.getValue().booleanValue() && file.canWrite()) {
		//				entry.getPermissions().add(CMIS_WRITE);
		//				entry.getPermissions().add(CMIS_ALL);
		//			}
		//
		//			entry.setDirect(true);
		//
		//			// add ACE
		//			builtProperties.getAces().add(entry);
		//		}

		return result;
	}
}
