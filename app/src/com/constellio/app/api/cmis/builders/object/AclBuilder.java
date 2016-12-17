package com.constellio.app.api.cmis.builders.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepositoryInfoManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.security.AuthorizationsServices;

public class AclBuilder {

	public static final String CMIS_READ = "cmis:read";
	public static final String CMIS_WRITE = "cmis:write";
	public static final String CMIS_DELETE = "cmis:delete";
	public static final String CMIS_ALL = "cmis:all";

	private final ModelLayerFactory modelLayerFactory;

	private final ConstellioCollectionRepository repository;

	private final SchemasRecordsServices schemas;

	public AclBuilder(ConstellioCollectionRepository repository, ModelLayerFactory modelLayerFactory) {
		this.repository = repository;
		this.modelLayerFactory = modelLayerFactory;
		this.schemas = new SchemasRecordsServices(repository.getCollection(), modelLayerFactory);
	}

	//public static List<Ace> to

	/**
	 * Compiles the ACL for a file or folder.
	 */
	public Acl build(Record record) {

		AccessControlListImpl result = new AccessControlListImpl();
		result.setAces(new ArrayList<Ace>());
		result.setExact(false);
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		for (Authorization authorization : authorizationsServices.getRecordAuthorizations(record)) {
			List<String> cmisPermissions = new ArrayList<>();
			if (authorization.getDetail().getRoles().contains(Role.READ)) {
				cmisPermissions.add(CMIS_READ);
			}

			if (authorization.getDetail().getRoles().contains(Role.WRITE)) {
				cmisPermissions.add(CMIS_WRITE);
			}

			if (authorization.getDetail().getRoles().contains(Role.DELETE)) {
				cmisPermissions.add(CMIS_DELETE);
			}

			boolean direct = authorization.getGrantedOnRecords().contains(record.getId());
			for (String principalId : authorization.getGrantedToPrincipals()) {
				AccessControlPrincipalDataImpl principal = toPrincipal(principalId);
				AccessControlEntryImpl ace = new AccessControlEntryImpl(principal, cmisPermissions);
				ace.setDirect(direct);
				result.getAces().add(ace);
			}
		}

		return result;
	}

	private AccessControlPrincipalDataImpl toPrincipal(String principalId) {
		Record record = modelLayerFactory.newRecordServices().getDocumentById(principalId);
		if (record.getSchemaCode().startsWith(User.SCHEMA_TYPE)) {
			return new AccessControlPrincipalDataImpl(schemas.wrapUser(record).getUsername());
		} else {
			return new AccessControlPrincipalDataImpl(schemas.wrapGroup(record).getCode());
		}
	}

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

}
