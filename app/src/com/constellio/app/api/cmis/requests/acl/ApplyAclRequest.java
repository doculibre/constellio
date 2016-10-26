package com.constellio.app.api.cmis.requests.acl;

import static com.constellio.app.api.cmis.builders.object.AclBuilder.CMIS_DELETE;
import static com.constellio.app.api.cmis.builders.object.AclBuilder.CMIS_READ;
import static com.constellio.app.api.cmis.builders.object.AclBuilder.CMIS_WRITE;
import static com.constellio.data.utils.LangUtils.hasSameElementsNoMatterTheOrder;
import static com.constellio.data.utils.LangUtils.isEqual;
import static com.constellio.model.entities.security.CustomizedAuthorizationsBehavior.DETACH;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;

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
	private final CallContext context;
	private final AuthorizationsServices authorizationsServices;
	private final RecordServices recordServices;
	private final String collection;

	public ApplyAclRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory, CallContext context,
			String repositoryId, String objectId, Acl addAces, Acl removeAces, AclPropagation aclPropagation,
			ExtensionsData extension) {
		super(repository, appLayerFactory);
		this.context = context;
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

	public ApplyAclRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory, CallContext context,
			String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
		super(repository, appLayerFactory);
		this.context = context;
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
		User user = (User) context.get(ConstellioCmisContextParameters.USER);

		validateAces(aces);
		validateAces(addAces);
		validateAces(removeAces);

		if (hasCommandToRemoveAllInheritedAuthorizations()) {
			Record record = recordServices.getDocumentById(objectId);
			for (Authorization auth : getInheritedObjectAuthorizationsWithPermission(objectId)) {
				authorizationsServices.removeAuthorizationOnRecord(auth, record, DETACH);
			}

		}

		List<Ace> currentAces = new GetAclRequest(repository, appLayerFactory, objectId).process().getAces();

		List<Ace> acesToAdd = getAcesToAdd(currentAces);
		List<Ace> acesToRemove = getAcesToRemove(currentAces);

		createNewAuthorizations(user, acesToAdd);
		removeAuthorizations(user, acesToRemove);

		return new GetAclRequest(repository, appLayerFactory, objectId).process();
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

	private void removeAuthorizations(User user, List<Ace> acesToRemove) {
		Set<String> authorizationsPotentiallyEmpty = new HashSet<>();
		for (Ace ace : acesToRemove) {
			List<String> permissions = toConstellioPermissions(ace.getPermissions());
			Record principal = getPrincipalRecord(ace.getPrincipalId());
			List<String> authorizationsIds = new ArrayList<>(principal.<String>getList(Schemas.AUTHORIZATIONS));
			for (AuthorizationDetails authDetails : getObjectAuthorizationsWithPermission(objectId, permissions)) {
				authorizationsIds.remove(authDetails.getId());
				authorizationsPotentiallyEmpty.add(authDetails.getId());
			}

			try {
				recordServices.updateAsync(principal.set(Schemas.AUTHORIZATIONS, authorizationsIds));
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
		for (String auth : authorizationsPotentiallyEmpty) {
			Authorization authorization = authorizationsServices.getAuthorization(collection, auth);
			if (authorization.getGrantedToPrincipals().isEmpty()) {
				authorizationsServices.delete(authorization.getDetail(), user);
			}
		}
	}

	private void createNewAuthorizations(User user, List<Ace> acesToAdd) {
		for (Ace ace : acesToAdd) {
			if (!REMOVE_INHERITANCE_COMMAND.equals(ace.getPrincipalId())) {
				List<String> permissions = toConstellioPermissions(ace.getPermissions());
				AuthorizationDetails authorizationDetails = getObjectAuthorizationWithPermission(objectId, permissions);
				Record principal = getPrincipalRecord(ace.getPrincipalId());
				if (authorizationDetails == null) {
					Authorization auth = new AuthorizationBuilder(collection)
							.forPrincipalsIds(principal.getId()).on(objectId).giving(permissions);
					authorizationsServices.add(auth, user);

				} else {
					List<String> authorizations = new ArrayList<>(principal.<String>getList(Schemas.AUTHORIZATIONS));
					authorizations.add(authorizationDetails.getId());
					principal.set(Schemas.AUTHORIZATIONS, authorizations);
					try {
						modelLayerFactory.newRecordServices().updateAsync(principal);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	AuthorizationDetails getObjectAuthorizationWithPermission(String objectId, List<String> permissions) {
		List<AuthorizationDetails> detailses = getObjectAuthorizationsWithPermission(objectId, permissions);
		return detailses.isEmpty() ? null : detailses.get(0);
	}

	List<AuthorizationDetails> getObjectAuthorizationsWithPermission(String objectId, List<String> permissions) {
		Record record = recordServices.getDocumentById(objectId);
		List<String> authorizations = record.get(Schemas.AUTHORIZATIONS);
		List<AuthorizationDetails> detailses = new ArrayList<>();

		for (String authorization : authorizations) {
			AuthorizationDetails details = modelLayerFactory.getAuthorizationDetailsManager().get(collection, authorization);

			if (details != null && hasSameElementsNoMatterTheOrder(details.getRoles(), permissions)) {
				detailses.add(details);
			}
		}

		return detailses;
	}

	List<Authorization> getInheritedObjectAuthorizationsWithPermission(String objectId) {
		Record record = recordServices.getDocumentById(objectId);
		List<String> authorizations = record.get(Schemas.INHERITED_AUTHORIZATIONS);
		List<Authorization> detailses = new ArrayList<>();

		for (String authorization : authorizations) {
			Authorization details = authorizationsServices.getAuthorization(collection, authorization);

			if (details != null) {
				detailses.add(details);
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
