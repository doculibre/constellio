package com.constellio.app.api.cmis.binding.collection;

import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.*;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.server.CallContext;

import java.util.*;

import static com.constellio.app.api.cmis.builders.object.AclBuilder.*;

public class ConstellioCollectionRepositoryInfoManager {

	private static final String ROOT_ID = "@root@";

	private final String collection;
	private final String constellioVersion;

	private final RepositoryInfo repositoryInfoForStandardVersion1_0;
	private final RepositoryInfo repositoryInfoForStandardVersion1_1;

	public ConstellioCollectionRepositoryInfoManager(String collection, String constellioVersion) {
		this.collection = collection;
		this.constellioVersion = constellioVersion;
		repositoryInfoForStandardVersion1_0 = createRepositoryInfo(CmisVersion.CMIS_1_0);
		repositoryInfoForStandardVersion1_1 = createRepositoryInfo(CmisVersion.CMIS_1_1);
	}

	private RepositoryInfo createRepositoryInfo(CmisVersion cmisVersion) {
		assert cmisVersion != null;

		RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();

		repositoryInfo.setId(collection);
		repositoryInfo.setName(collection);
		repositoryInfo.setDescription(collection);

		repositoryInfo.setCmisVersionSupported(cmisVersion.value());

		repositoryInfo.setProductName("Constellio");
		repositoryInfo.setProductVersion(constellioVersion);
		repositoryInfo.setVendorName("DocuLibre");

		repositoryInfo.setRootFolder(ROOT_ID);

		repositoryInfo.setThinClientUri("");
		repositoryInfo.setChangesIncomplete(true);

		RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
		capabilities.setCapabilityAcl(CapabilityAcl.MANAGE);
		capabilities.setAllVersionsSearchable(false);
		capabilities.setCapabilityJoin(CapabilityJoin.NONE);
		capabilities.setSupportsMultifiling(false);
		capabilities.setSupportsUnfiling(false);
		capabilities.setSupportsVersionSpecificFiling(false);
		capabilities.setIsPwcSearchable(false);
		capabilities.setIsPwcUpdatable(false);
		capabilities.setCapabilityQuery(CapabilityQuery.METADATAONLY);
		capabilities.setCapabilityChanges(CapabilityChanges.NONE);
		capabilities
				.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
		capabilities.setSupportsGetDescendants(true);
		capabilities.setSupportsGetFolderTree(true);
		capabilities.setCapabilityRendition(CapabilityRenditions.NONE);

		if (cmisVersion != CmisVersion.CMIS_1_0) {
			//capabilities.setOrderByCapability(CapabilityOrderBy.NONE);

			NewTypeSettableAttributesImpl typeSetAttributes = new NewTypeSettableAttributesImpl();
			typeSetAttributes.setCanSetControllableAcl(true);
			typeSetAttributes.setCanSetControllablePolicy(false);
			typeSetAttributes.setCanSetCreatable(false);
			typeSetAttributes.setCanSetDescription(false);
			typeSetAttributes.setCanSetDisplayName(false);
			typeSetAttributes.setCanSetFileable(false);
			typeSetAttributes.setCanSetFulltextIndexed(false);
			typeSetAttributes.setCanSetId(false);
			typeSetAttributes.setCanSetIncludedInSupertypeQuery(false);
			typeSetAttributes.setCanSetLocalName(false);
			typeSetAttributes.setCanSetLocalNamespace(false);
			typeSetAttributes.setCanSetQueryable(false);
			typeSetAttributes.setCanSetQueryName(false);

			capabilities.setNewTypeSettableAttributes(typeSetAttributes);

			CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
			capabilities.setCreatablePropertyTypes(creatablePropertyTypes);
		}

		repositoryInfo.setCapabilities(capabilities);

		AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
		aclCapability.setSupportedPermissions(SupportedPermissions.BASIC);
		aclCapability.setAclPropagation(AclPropagation.REPOSITORYDETERMINED);

		// permissions
		List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();
		permissions.add(createPermission(CMIS_READ, "Read"));
		permissions.add(createPermission(CMIS_WRITE, "Write"));
		permissions.add(createPermission(CMIS_DELETE, "Delete"));
		permissions.add(createPermission(CMIS_ALL, "All"));
		aclCapability.setPermissionDefinitionData(permissions);

		// mapping
		List<PermissionMapping> list = new ArrayList<PermissionMapping>();
		list.add(createMapping(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_CREATE_FOLDER_FOLDER, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, CMIS_WRITE));
		list.add(createMapping(PermissionMapping.CAN_DELETE_OBJECT, CMIS_ALL));
		list.add(createMapping(PermissionMapping.CAN_DELETE_TREE_FOLDER, CMIS_ALL));
		list.add(createMapping(PermissionMapping.CAN_GET_ACL_OBJECT, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_APPLY_ACL_OBJECT, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_GET_CHILDREN_FOLDER, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_GET_PARENTS_FOLDER, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_MOVE_OBJECT, CMIS_WRITE));
		list.add(createMapping(PermissionMapping.CAN_MOVE_SOURCE, CMIS_READ));
		list.add(createMapping(PermissionMapping.CAN_MOVE_TARGET, CMIS_WRITE));
		list.add(createMapping(PermissionMapping.CAN_SET_CONTENT_DOCUMENT, CMIS_WRITE));
		list.add(createMapping(PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, CMIS_WRITE));
		list.add(createMapping(PermissionMapping.CAN_VIEW_CONTENT_OBJECT, CMIS_READ));
		Map<String, PermissionMapping> map = new LinkedHashMap<String, PermissionMapping>();
		for (PermissionMapping pm : list) {
			map.put(pm.getKey(), pm);
		}
		aclCapability.setPermissionMappingData(map);

		repositoryInfo.setAclCapabilities(aclCapability);

		return repositoryInfo;
	}

	private PermissionDefinition createPermission(String permission,
												  String description) {
		PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
		pd.setId(permission);
		pd.setDescription(description);

		return pd;
	}

	private PermissionMapping createMapping(String key, String permission) {
		PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
		pm.setKey(key);
		pm.setPermissions(Collections.singletonList(permission));
		return pm;
	}

	/**
	 * CMIS getRepositoryInfo.
	 */
	public RepositoryInfo getRepositoryInfo(CallContext context) {

		if (context.getCmisVersion() == CmisVersion.CMIS_1_0) {
			return repositoryInfoForStandardVersion1_0;
		} else {
			return repositoryInfoForStandardVersion1_1;
		}
	}
}
