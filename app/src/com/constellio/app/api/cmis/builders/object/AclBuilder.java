/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		//			result.getAces().add(entry);
		//		}

		return result;
	}
}
