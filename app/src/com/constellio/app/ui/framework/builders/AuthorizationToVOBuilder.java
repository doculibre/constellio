package com.constellio.app.ui.framework.builders;

import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.structures.NestedRecordAuthorizations.NestedRecordAuthorization;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.roles.RolesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.SchemaCaptionUtils.getCaptionForRecord;
import static com.constellio.model.entities.Language.withLocale;
import static java.util.Arrays.asList;

public class AuthorizationToVOBuilder extends RecordToVOBuilder implements Serializable {

	private static Logger LOGGER = LoggerFactory.getLogger(AuthorizationToVOBuilder.class);

	private static final String ENABLE = "AuthorizationsView.enable";
	private static final String DISABLE = "AuthorizationsView.disable";
	transient ModelLayerFactory modelLayerFactory;

	public AuthorizationToVOBuilder(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
	}

	public AuthorizationVO build(Authorization authorization) {
		return build(authorization, null, null, null);
	}

	public AuthorizationVO build(Authorization authorization, Metadata receivedFromMetadata, Record receivedFromValue,
								 SessionContext sessionContext) {
		List<String> principals = authorization.getPrincipals();
		List<String> records = asList(authorization.getTarget());
		List<String> roles = authorization.getRoles();

		List<String> users = new ArrayList<>();
		List<String> groups = new ArrayList<>();
		List<String> userRoles = new ArrayList<>();
		List<String> userRolesTitles = new ArrayList<>();
		List<String> accessRoles = new ArrayList<>();

		for (String roleCode : roles) {
			RolesManager rolesManager = modelLayerFactory.getRolesManager();
			Role role = rolesManager.getRole(authorization.getCollection(), roleCode);
			if (role.isContentPermissionRole()) {
				accessRoles.add(roleCode);
			} else {
				userRoles.add(roleCode);
				userRolesTitles.add(role.getTitle());
			}
		}

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		SchemasRecordsServices schemas = new SchemasRecordsServices(authorization.getCollection(), modelLayerFactory);
		List<Record> allUsers = searchServices.getAllRecords(schemas.userSchemaType());
		List<Record> allGroups = searchServices.getAllRecords(schemas.groupSchemaType());

		if (principals != null) {
			for (Record userRecord : allUsers) {
				if (userRecord != null && principals.contains(userRecord.getId())) {
					User user = schemas.wrapUser(userRecord);
					//if (user.getStatus() == UserCredentialStatus.ACTIVE) {
					users.add(userRecord.getId());
					//}
				}
			}
			for (Record groupRecord : allGroups) {
				if (groupRecord != null && principals.contains(groupRecord.getId())) {
					Group group = schemas.wrapGroup(groupRecord);
					//if (schemas.isGroupActive(group)) {
					groups.add(groupRecord.getId());
					//}
				}
			}
		}

		String metadataLabel = receivedFromMetadata == null ? null :
							   receivedFromMetadata.getLabel(withLocale(sessionContext.getCurrentLocale()));

		String recordCaption = receivedFromValue == null ? null : getCaptionForRecord(receivedFromValue,
				sessionContext.getCurrentLocale(), true);
		String authorizationType = authorization.isNegative() ? $(DISABLE) : $(ENABLE);
		String source = authorization.getSource() == null ? null : authorization.getSource().stringValue();
		boolean nested = authorization instanceof NestedRecordAuthorization;

		AuthorizationVO authorizationVO = new AuthorizationVO(users, groups, records, accessRoles, userRoles, userRolesTitles,
				authorization.getId(), authorization.getStartDate(), authorization.getEndDate(), authorization.getSharedBy(),
				authorization.isSynced(), metadataLabel, recordCaption, authorizationType, source, nested);

		return authorizationVO;
	}

	@Override
	public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO, SessionContext sessionContext) {
		String id = record.getId();
		String schemaCode = record.getSchemaCode();
		String collection = record.getCollection();
		boolean saved = record.isSaved();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);

		if (schemaVO == null) {
			schemaVO = new MetadataSchemaToVOBuilder().build(schema, viewMode, sessionContext);
		}

		ContentVersionToVOBuilder contentVersionVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);

		List<MetadataValueVO> metadataValueVOs = new ArrayList<MetadataValueVO>();
		List<MetadataVO> metadatas = schemaVO.getMetadatas();
		for (MetadataVO metadataVO : metadatas) {
			String metadataCode = metadataVO.getCode();
			Metadata metadata = schema.getMetadata(metadataCode);
			List<User> users = modelLayerFactory.newUserServices().getAllUsersInCollection(collection);
			List<Group> groups = modelLayerFactory.newUserServices().getAllGroupsInCollections(collection);

			Object recordVOValue = getValue(record, metadata);
			if ((RecordAuthorization.DEFAULT_SCHEMA + "_" + RecordAuthorization.PRINCIPALS).equals(metadataCode)) {
				List<Object> listRecordVOValue = new ArrayList<Object>();
				List<Object> listRecordValue = (List<Object>) recordVOValue;
				recordVOValue = listRecordVOValue;
				for (Iterator<Object> it = listRecordValue.iterator(); it.hasNext(); ) {
					Object listVOValueElement = it.next();
					if (listVOValueElement instanceof String) {
						Optional<User> user = users.stream().filter(x -> x.getId().equals((String) listVOValueElement))
								.findFirst();
						if (user.isPresent()) {
							listRecordVOValue.add(user.get().getUsername());
						} else {
							Optional<Group> group = groups.stream().filter(x -> x.getId().equals((String) listVOValueElement))
									.findFirst();
							if (group.isPresent()) {
								listRecordVOValue.add(group.get().getTitle());
							}
						}
					}
				}
			} else if ((RecordAuthorization.DEFAULT_SCHEMA + "_" + RecordAuthorization.SHARED_BY).equals(metadataCode)) {
				if (recordVOValue != null) {
					String userId = (String) recordVOValue;
					Optional<User> user = users.stream().filter(x -> x.getId().equals((String) userId))
							.findFirst();
					if (user.isPresent()) {
						recordVOValue = user.get().getUsername();
					}
				}
			} else if (recordVOValue instanceof Content) {
				recordVOValue = contentVersionVOBuilder.build((Content) recordVOValue, sessionContext);
			} else if (recordVOValue instanceof List) {
				List<Object> listRecordVOValue = new ArrayList<Object>();
				List<Object> listRecordValue = (List<Object>) recordVOValue;
				for (Iterator<Object> it = listRecordValue.iterator(); it.hasNext(); ) {
					Object listVOValueElement = it.next();
					if (listVOValueElement instanceof Content) {
						listVOValueElement = contentVersionVOBuilder.build((Content) listVOValueElement);
					}
					if (listVOValueElement != null) {
						listRecordVOValue.add(listVOValueElement);
					}
				}
				recordVOValue = listRecordVOValue;
			}
			MetadataValueVO metadataValueVO = new MetadataValueVO(metadataVO, recordVOValue);
			metadataValueVOs.add(metadataValueVO);
		}

		RecordVO recordVO = newRecordVO(id, metadataValueVOs, viewMode, new ArrayList<String>());
		recordVO.setSaved(saved);
		recordVO.setRecord(record);
		BuildRecordVOParams buildRecordVOParams = new BuildRecordVOParams(record, recordVO);
		constellioFactories.getAppLayerFactory().getExtensions()
				.forCollection(record.getCollection()).buildRecordVO(buildRecordVOParams);

		return recordVO;
	}
}
