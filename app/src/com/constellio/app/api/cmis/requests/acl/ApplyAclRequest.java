package com.constellio.app.api.cmis.requests.acl;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;

public class ApplyAclRequest extends CmisCollectionRequest<Acl> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplyAclRequest.class);
	private final String repositoryId;
	private final String objectId;
	private final Acl aces;
	private final Acl addAces;
	private final Acl removeAces;
	private final AclPropagation aclPropagation;
	private final ExtensionsData extension;
	private final CallContext context;

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
	}

	/**
	 * CMIS getACL.
	 */
	@Override
	public Acl process() {
		User user = (User) context.get(ConstellioCmisContextParameters.USER);

		if (addAces != null) {

			List<Authorization> authorizations = new ArrayList<>();
			for (Ace ace : addAces.getAces()) {
				List<String> permissions = toConstellioPermissions(ace.getPermissions());
				authorizations.add(new AuthorizationBuilder(repository.getCollection())
						.forPrincipalsIds(getPrincipalRecordId(ace.getPrincipalId())).on(objectId).giving(permissions));
			}

			for (Authorization authorization : authorizations) {
				modelLayerFactory.newAuthorizationsServices().add(authorization, user);
			}
		}

		if (aces != null) {

			List<Authorization> authorizations = new ArrayList<>();
			for (Ace ace : aces.getAces()) {
				List<String> permissions = toConstellioPermissions(ace.getPermissions());
				authorizations.add(new AuthorizationBuilder(repository.getCollection())
						.forPrincipalsIds(getPrincipalRecordId(ace.getPrincipalId())).on(objectId).giving(permissions));
			}

			for (Authorization authorization : authorizations) {
				modelLayerFactory.newAuthorizationsServices().add(authorization, user);
			}
		}

		GetAclUnsupportedRequest aclUnsupportedRequest = new GetAclUnsupportedRequest(repository, appLayerFactory, objectId);
		return aclUnsupportedRequest.process();
	}

	private List<String> toConstellioPermissions(List<String> permissions) {
		List<String> constellioPermissions = new ArrayList<>();

		if (permissions.contains("cmis:read")) {
			constellioPermissions.add(Role.READ);
		}

		if (permissions.contains("cmis:write")) {
			constellioPermissions.add(Role.WRITE);
		}

		return constellioPermissions;
	}

	private String getPrincipalRecordId(String principalId) {
		UserServices userServices = modelLayerFactory.newUserServices();
		try {
			return userServices.getUserInCollection(principalId, repository.getCollection()).getId();
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			return userServices.getGroupInCollection(principalId, repository.getCollection()).getId();
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
