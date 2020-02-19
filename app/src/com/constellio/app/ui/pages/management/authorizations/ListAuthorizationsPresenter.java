package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.AuthorizationToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;

public abstract class ListAuthorizationsPresenter extends BasePresenter<ListAuthorizationsView> {
	private static final String DISABLE = "AuthorizationsView.disable";
	private transient AuthorizationsServices authorizationsServices;
	private transient List<Authorization> authorizations;
	private transient List<AuthorizationReceivedFromMetadata> authorizationsReceivedFromMetadatas;
	protected String recordId;

	public ListAuthorizationsPresenter(ListAuthorizationsView view) {
		super(view);
	}

	public ListAuthorizationsPresenter forRequestParams(String parameters) {
		recordId = parameters;
		return this;
	}

	public ListAuthorizationsPresenter forCurrentUser() {
		recordId = getCurrentUser().getId();
		return this;
	}

	public String getRoleTitle(String roleCode) {
		return modelLayerFactory.getRolesManager().getRole(view.getCollection(), roleCode).getTitle();
	}

	public abstract void backButtonClicked(String schemaCode);

	public abstract boolean isDetacheable();

	public abstract boolean isAttached();

	public RecordVO getRecordVO() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public List<AuthorizationVO> getInheritedAuthorizations() {
		AuthorizationToVOBuilder builder = newAuthorizationToVOBuilder();

		List<AuthorizationVO> results = new ArrayList<>();
		for (Authorization authorization : getAllAuthorizations()) {
			if (!(isOwnAuthorization(authorization) || authorization.getPrincipals().isEmpty()) && isSameRoleType(
					authorization)) {
				results.add(builder.build(authorization));
			}
		}
		return results;
	}

	public List<AuthorizationVO> getInheritedAuthorizationsFromMetadatas() {
		AuthorizationToVOBuilder builder = newAuthorizationToVOBuilder();

		List<AuthorizationVO> results = new ArrayList<>();
		for (AuthorizationReceivedFromMetadata authorization : getAuthorizationsReceivedFromMetadatas()) {
			if (isSameRoleType(authorization.authorization)) {
				results.add(builder.build(authorization.authorization, authorization.metadata, authorization.receivedFrom,
						view.getSessionContext()));
			}
		}
		return results;
	}

	public List<AuthorizationVO> getOwnAuthorizations() {
		AuthorizationToVOBuilder builder = newAuthorizationToVOBuilder();

		List<AuthorizationVO> results = new ArrayList<>();
		for (Authorization authorization : getAllAuthorizations()) {
			if (isOwnAuthorization(authorization) && !authorization.getPrincipals().isEmpty() && isSameRoleType(
					authorization)) {
				results.add(builder.build(authorization));
			}
		}
		return results;
	}

	public List<AuthorizationVO> getSharedAuthorizations() {
		AuthorizationToVOBuilder builder = newAuthorizationToVOBuilder();

		List<AuthorizationVO> results = new ArrayList<>();
		for (Authorization authorization : getSharedAuthorizationsByCurrentUser()) {
			results.add(builder.build(authorization));
		}
		return results;
	}

	public void authorizationCreationRequested(AuthorizationVO authorizationVO) {
		AuthorizationAddRequest authorization = toNewAuthorization(authorizationVO);
		String id = authorizationsServices().add(authorization, getCurrentUser());
		authorizationVO.setAuthId(id);
		view.addAuthorization(authorizationVO);
	}

	public void share(AuthorizationVO authorizationVO) {

		authorizationVO.setSharedBy(getCurrentUser().getId());
		authorizationCreationRequested(authorizationVO);
	}


	public void authorizationModificationRequested(AuthorizationVO authorizationVO) {
		AuthorizationModificationRequest request = toAuthorizationModificationRequest(authorizationVO);
		authorizationsServices().execute(request);
		authorizations = null;
		view.refresh();
	}

	public void deleteButtonClicked(AuthorizationVO authorizationVO) {
		Authorization authorization = authorizationsServices().getAuthorization(
				view.getCollection(), authorizationVO.getAuthId());
		removeAuthorization(authorization);
		authorizations = null;
		view.removeAuthorization(authorizationVO);
	}

	public abstract List<String> getAllowedAccesses();

	public List<String> getAllowedRoles() {
		if (getCurrentUser().has(CorePermissions.MANAGE_SECURITY).globally()) {
			List<String> roles = new ArrayList<>();
			for (Role role : modelLayerFactory.getRolesManager().getAllRoles(view.getCollection())) {
				roles.add(role.getCode());
			}
			return roles;
		}

		return getCurrentUser().getUserRoles();
	}

	public void detachRequested() {
		Record record = recordServices().getDocumentById(recordId);
		authorizationsServices().detach(record);
		authorizations = null;
		view.refresh();
	}

	protected abstract boolean isOwnAuthorization(Authorization authorization);

	protected abstract void removeAuthorization(Authorization authorization);

	private AuthorizationAddRequest toNewAuthorization(AuthorizationVO authorizationVO) {

		ArrayList<String> roles = new ArrayList<>();
		roles.addAll(authorizationVO.getAccessRoles());

		for (String roleCode : authorizationVO.getUserRoles()) {
			roles.add(roleCode);
		}

		List<String> principals = new ArrayList<>();
		principals.addAll(authorizationVO.getUsers());
		principals.addAll(authorizationVO.getGroups());
		return AuthorizationAddRequest.authorizationInCollection(collection).giving(roles)
				.forPrincipalsIds(principals).on(authorizationVO.getRecord()).sharedBy(authorizationVO.getSharedBy())
				.startingOn(authorizationVO.getStartDate()).endingOn(authorizationVO.getEndDate())
				.andNegative($(DISABLE).equals(authorizationVO.getNegative()));
	}

	private AuthorizationModificationRequest toAuthorizationModificationRequest(AuthorizationVO authorizationVO) {
		String authId = authorizationVO.getAuthId();

		AuthorizationModificationRequest request = modifyAuthorizationOnRecord(authId, collection, recordId);
		request = request.withNewAccessAndRoles(authorizationVO.getAccessRoles());
		request = request.withNewStartDate(authorizationVO.getStartDate());
		request = request.withNewEndDate(authorizationVO.getEndDate());

		List<String> principals = new ArrayList<>();
		principals.addAll(authorizationVO.getUsers());
		principals.addAll(authorizationVO.getGroups());
		request = request.withNewPrincipalIds(principals);
		request = request.setExecutedBy(getCurrentUser());

		return request;

	}

	protected AuthorizationsServices authorizationsServices() {
		if (authorizationsServices == null) {
			authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		}
		return authorizationsServices;
	}

	protected List<Authorization> getAllAuthorizations() {
		//if (authorizations == null) {
		Record record = presenterService().getRecord(recordId);
		authorizations = authorizationsServices().getRecordAuthorizations(record);
		//}
		return authorizations;
	}

	protected List<Authorization> getSharedAuthorizationsByCurrentUser() {
		User user = getCurrentUser();
		authorizations = authorizationsServices().getAllAuthorizationUserShared(user);
		return authorizations;
	}

	public boolean isRecordNotATaxonomyConcept() {
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		Record record = presenterService().getRecord(recordId);
		return taxonomiesManager.getTaxonomyOf(record) == null;
	}

	public boolean hasManageSecurityPermission() {
		return getCurrentUser().has(CorePermissions.MANAGE_SECURITY).globally();
	}

	private static class AuthorizationReceivedFromMetadata {
		Authorization authorization;
		Metadata metadata;
		Record receivedFrom;
	}

	protected List<AuthorizationReceivedFromMetadata> getAuthorizationsReceivedFromMetadatas() {
		Record record = presenterService().getRecord(recordId);

		authorizationsReceivedFromMetadatas = new ArrayList<>();
		if (!record.getTypeCode().equals(User.SCHEMA_TYPE) && !record.getTypeCode().equals(Group.SCHEMA_TYPE)) {

			List<String> ancestorsAndSelf = new ArrayList<>(record.<String>getList(Schemas.ATTACHED_ANCESTORS));

			for (String ancestorId : ancestorsAndSelf) {
				if (!ancestorId.startsWith("-")) {
					Record ancestor = recordServices().getDocumentById(ancestorId);
					MetadataSchema metadataSchema = schema(ancestor.getSchemaCode());

					for (Metadata metadata : metadataSchema.getMetadatas().onlyWithType(MetadataValueType.REFERENCE)) {
						if (metadata.isRelationshipProvidingSecurity()) {
							for (String referenceId : ancestor.<String>getValues(metadata)) {
								Record reference = recordServices().getDocumentById(referenceId);
								List<Authorization> referenceAuthorizations = authorizationsServices()
										.getRecordAuthorizations(reference);
								for (Authorization authorization : referenceAuthorizations) {
									AuthorizationReceivedFromMetadata auth = new AuthorizationReceivedFromMetadata();
									auth.authorization = authorization;
									auth.metadata = metadata;
									auth.receivedFrom = reference;
									authorizationsReceivedFromMetadatas.add(auth);
								}
							}
						}
					}
				}
			}
		}

		return authorizationsReceivedFromMetadatas;
	}

	protected boolean hasOverridenSecurityFromMetadatas() {
		boolean hasOverridenSecurityFromMetadatas = false;

		for (AuthorizationReceivedFromMetadata authorization : getAuthorizationsReceivedFromMetadatas()) {
			hasOverridenSecurityFromMetadatas |= ((Authorization) authorization.authorization)
					.isOverrideInherited();
		}

		return hasOverridenSecurityFromMetadatas;
	}

	private boolean isSameRoleType(Authorization authorization) {
		return (seeAccessField() && isAccessAuthorization(authorization)) || (seeRolesField() && isRoleAuthorization(
				authorization));
	}

	protected boolean isAccessAuthorization(Authorization auth) {
		for (String role : auth.getRoles()) {
			if (isAccessRole(role)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isRoleAuthorization(Authorization auth) {
		for (String role : auth.getRoles()) {
			if (!isAccessRole(role)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAccessRole(String role) {
		return role.equals(Role.READ) || role.equals(Role.WRITE) || role.equals(Role.DELETE);
	}

	AuthorizationToVOBuilder newAuthorizationToVOBuilder() {
		return new AuthorizationToVOBuilder(modelLayerFactory);
	}

	public abstract boolean seeRolesField();

	public abstract boolean seeAccessField();

	public abstract boolean seeSharedBy();

	public Boolean hasUserAccess(String accessCode) {
		return getCurrentUser().hasCollectionAccess(accessCode);
	}

	public boolean isRMModuleActive() {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		return types.hasSchema(AdministrativeUnit.DEFAULT_SCHEMA);
	}

	public String getCurrentUserId() {
		return getCurrentUser().getId();
	}
}
