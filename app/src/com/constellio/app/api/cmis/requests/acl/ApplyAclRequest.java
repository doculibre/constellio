package com.constellio.app.api.cmis.requests.acl;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.api.cmis.builders.object.AclBuilder.CMIS_DELETE;
import static com.constellio.app.api.cmis.builders.object.AclBuilder.CMIS_READ;
import static com.constellio.app.api.cmis.builders.object.AclBuilder.CMIS_WRITE;
import static com.constellio.data.utils.LangUtils.hasSameElementsNoMatterTheOrder;
import static com.constellio.data.utils.LangUtils.isEqual;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

/**
 * Limitations of this service :
 * - The propagation mode OBJECTONLY is not supported, only PROPAGATE
 * - Cannot remove  an inherited authorisation
 * - Apache chemistry automatically remove duplicate an ACE if there is another one with better permissions
 * - Delete access and roles (ex. user/manager) are not handled
 */
public class ApplyAclRequest extends CmisCollectionRequest<Acl> {

	private static final String REMOVE_INHERITANCE_COMMAND = "constellio:removeInheritance";

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplyAclRequest.class);
	private final String repositoryId;
	private final String objectId;
	private final Acl aces;
	private final Acl addAces;
	private final Acl removeAces;
	private final AclPropagation aclPropagation;
	private final ExtensionsData extension;
	private final AuthorizationsServices authorizationsServices;
	private final RecordServices recordServices;
	private final String collection;

	public ApplyAclRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
						   CallContext context,
						   String repositoryId, String objectId, Acl addAces, Acl removeAces,
						   AclPropagation aclPropagation,
						   ExtensionsData extension) {
		super(context, repository, appLayerFactory);
		this.repositoryId = repositoryId;
		this.objectId = objectId;
		this.aces = null;
		this.addAces = addAces;
		this.removeAces = removeAces;
		this.aclPropagation = aclPropagation;
		this.extension = extension;
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collection = repository.getCollection();
	}

	public ApplyAclRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
						   CallContext context,
						   String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
		super(context, repository, appLayerFactory);
		this.repositoryId = repositoryId;
		this.objectId = objectId;
		this.addAces = null;
		this.removeAces = null;
		this.aces = aces;
		this.aclPropagation = aclPropagation;
		this.extension = null;
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collection = repository.getCollection();
	}

	/**
	 * CMIS getACL.
	 */
	@Override
	public Acl process() {
		validateAces(aces);
		validateAces(addAces);
		validateAces(removeAces);

		Record record = recordServices.getDocumentById(objectId);
		ensureUserHasAllowableActionsOnRecord(record, Action.CAN_APPLY_ACL);
		if (hasCommandToRemoveAllInheritedAuthorizations()) {

			authorizationsServices.detach(record);
		}

		List<Ace> currentAces = new GetAclRequest(repository, appLayerFactory, callContext, objectId).process().getAces();

		List<Ace> acesToAdd = getAcesToAdd(currentAces);
		List<Ace> acesToRemove = getAcesToRemove(currentAces);

		createNewAuthorizations(acesToAdd);
		removeAuthorizations(acesToRemove);

		return new GetAclRequest(repository, appLayerFactory, callContext, objectId).process();
	}

	private void validateAces(Acl acl) {
		if (acl != null) {
			List<Ace> aces = acl.getAces();
			for (Ace ace : aces) {
				if (!REMOVE_INHERITANCE_COMMAND.equals(ace.getPrincipalId())) {
					if (StringUtils.isBlank(ace.getPrincipalId())) {
						throw new RuntimeException("An ace has no specified principal");
					}

					if (ace.getPermissions().isEmpty()) {
						throw new RuntimeException("An ace has no permission");
					}

					for (String permission : ace.getPermissions()) {
						if (!CMIS_READ.equals(permission) && !CMIS_WRITE.equals(permission) && !CMIS_DELETE.equals(permission)) {
							throw new RuntimeException("An ace has unsupported permission '" + permission
													   + "', only cmis:read/cmis:write/cmis:delete are allowed");
						}
					}

					try {
						getPrincipalRecord(ace.getPrincipalId());
					} catch (Exception e) {
						throw new RuntimeException(
								"An ace has invalid principal : No such user with username or"
								+ " group with code : '" + ace.getPrincipalId() + "'");
					}
				}
			}
		}
	}

	private boolean hasCommandToRemoveAllInheritedAuthorizations() {
		for (Ace ace : aces.getAces()) {
			if (REMOVE_INHERITANCE_COMMAND.equals(ace.getPrincipalId())) {
				return true;
			}
		}
		return false;
	}

	private void removeAuthorizations(List<Ace> acesToRemove) {
		Set<String> authorizationsPotentiallyEmpty = new HashSet<>();
		for (Ace ace : acesToRemove) {
			List<String> permissions = toConstellioPermissions(ace.getPermissions());
			Record principal = getPrincipalRecord(ace.getPrincipalId());
			for (Authorization authDetails : getObjectAuthorizationsWithPermission(objectId, permissions)) {

				Authorization authorization = authorizationsServices.getAuthorization(collection, authDetails.getId());
				if (authorization.getPrincipals().contains(principal.getId())) {
					if (authorization.getPrincipals().size() == 1) {
						authorizationsServices.execute(authorizationDeleteRequest(authDetails).setExecutedBy(user));

					} else {
						List<String> principals = new ArrayList<>(authorization.getPrincipals());
						principals.remove(principal.getId());

						authorizationsServices.execute(modifyAuthorizationOnRecord(authorization, objectId)
								.withNewPrincipalIds(principals).setExecutedBy(user));
					}
				}
				//authorizationsIds.remove(authDetails.getId());
				//authorizationsPotentiallyEmpty.add(authDetails.getId());
			}

			//			try {
			//				recordServices.updateAsync(principal.set(Schemas.AUTHORIZATIONS, authorizationsIds));
			//			} catch (RecordServicesException e) {
			//				throw new RuntimeException(e);
			//			}

		}
		//		for (String auth : authorizationsPotentiallyEmpty) {
		//			Authorization anAuthorization = authorizationsServices.getAuthorization(collection, auth);
		//			if (anAuthorization.getPrincipals().isEmpty()) {
		//				authorizationsServices.execute(authorizationDeleteRequest(anAuthorization).setExecutedBy(user));
		//			}
		//		}

	}

	private void createNewAuthorizations(List<Ace> acesToAdd) {
		for (Ace ace : acesToAdd) {
			if (!REMOVE_INHERITANCE_COMMAND.equals(ace.getPrincipalId())) {
				List<String> permissions = toConstellioPermissions(ace.getPermissions());
				Authorization authorizationDetails = getObjectAuthorizationWithPermission(objectId, permissions);
				Record principal = getPrincipalRecord(ace.getPrincipalId());
				if (authorizationDetails == null) {
					AuthorizationAddRequest auth = authorizationInCollection(collection)
							.forPrincipalsIds(principal.getId()).on(objectId).giving(permissions);
					authorizationsServices.add(auth, user);

				} else {

					Authorization authorization = authorizationsServices
							.getAuthorization(collection, authorizationDetails.getId());

					List<String> principals = new ArrayList<>(authorization.getPrincipals());
					principals.add(principal.getId());
					authorizationsServices.execute(modifyAuthorizationOnRecord(authorization, objectId)
							.withNewPrincipalIds(principals).setExecutedBy(user));

					//					List<String> authorizations = new ArrayList<>(principal.<String>getList(Schemas.AUTHORIZATIONS));
					//					authorizations.add(authorizationDetails.getId());
					//					principal.set(Schemas.AUTHORIZATIONS, authorizations);
					//					try {
					//						recordServices.updateAsync(principal);
					//					} catch (RecordServicesException e) {
					//						throw new RuntimeException(e);
					//					}
				}
			}
		}
	}

	Authorization getObjectAuthorizationWithPermission(String objectId, List<String> permissions) {
		List<Authorization> detailses = getObjectAuthorizationsWithPermission(objectId, permissions);
		return detailses.isEmpty() ? null : detailses.get(0);
	}

	List<Authorization> getObjectAuthorizationsWithPermission(String objectId, List<String> permissions) {
		Record record = recordServices.getDocumentById(objectId);
		//List<String> authorizations = record.get(Schemas.AUTHORIZATIONS);
		List<Authorization> detailses = new ArrayList<>();

		//		for (String authorization : authorizations) {
		//			AuthorizationDetails details = modelLayerFactory.getAuthorizationDetailsManager().get(collection, authorization);
		//
		//			if (details != null && hasSameElementsNoMatterTheOrder(details.getRoles(), permissions)) {
		//				detailses.add(details);
		//			}
		//		}

		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		for (Authorization authDetails : schemas
				.searchAuthorizations(where(schemas.authorizationDetails.target()).isEqualTo(objectId))) {

			if (authDetails != null && hasSameElementsNoMatterTheOrder(authDetails.getRoles(), permissions)) {
				detailses.add(authDetails);
			}

		}

		return detailses;
	}

	private List<Ace> getAcesToAdd(List<Ace> currentAces) {
		List<Ace> acesToAdd = new ArrayList<>();
		if (aces != null) {
			for (Ace ace : aces.getAces()) {
				boolean found = false;
				for (Ace newAce : currentAces) {
					if (areEquals(ace, newAce)) {
						found = true;
						break;
					}
				}
				if (!found) {
					acesToAdd.add(ace);
				}
			}
		} else {
			if (addAces != null) {
				acesToAdd.addAll(addAces.getAces());
			}
		}
		return acesToAdd;
	}

	private List<Ace> getAcesToRemove(List<Ace> currentAces) {
		List<Ace> acesToRemove = new ArrayList<>();
		if (aces != null) {
			for (Ace ace : currentAces) {
				if (ace.isDirect()) {
					boolean found = false;
					for (Ace newAce : aces.getAces()) {
						if (areEquals(ace, newAce)) {
							found = true;
							break;
						}
					}
					if (!found) {
						acesToRemove.add(ace);
					}
				}
			}

		} else {
			if (removeAces != null) {
				acesToRemove.addAll(removeAces.getAces());
			}
		}
		return acesToRemove;
	}

	private boolean areEquals(Ace ace1, Ace ace2) {
		return isEqual(ace1.getPrincipalId(), ace2.getPrincipalId())
			   && hasSameElementsNoMatterTheOrder(ace1.getPermissions(), ace2.getPermissions());
	}

	private List<String> toConstellioPermissions(List<String> permissions) {
		List<String> constellioPermissions = new ArrayList<>();

		if (permissions.contains(CMIS_READ)) {
			constellioPermissions.add(Role.READ);
		}

		if (permissions.contains(CMIS_WRITE)) {
			constellioPermissions.add(Role.WRITE);
		}

		if (permissions.contains(CMIS_DELETE)) {
			constellioPermissions.add(Role.DELETE);
		}

		return constellioPermissions;
	}

	private Record getPrincipalRecord(String principalId) {
		UserServices userServices = modelLayerFactory.newUserServices();
		try {
			return userServices.getUserInCollection(principalId, collection).getWrappedRecord();
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			return userServices.getGroupInCollection(principalId, collection).getWrappedRecord();
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
