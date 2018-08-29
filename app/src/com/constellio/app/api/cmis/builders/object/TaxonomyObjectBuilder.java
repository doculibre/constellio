package com.constellio.app.api.cmis.builders.object;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaxonomyObjectBuilder {

	private CallContext context;

	public TaxonomyObjectBuilder(CallContext context) {
		this.context = context;
	}

	public static final String TAXONOMY_TYPE_ID = "taxonomy";

	public ObjectData build(Taxonomy taxonomy, ObjectInfoHandler objectInfoHandler, Language language) {
		ObjectDataImpl result = new ObjectDataImpl();
		ObjectInfoImpl objectInfo = new ObjectInfoImpl();

		setupObjectInfo(objectInfo);
		String id = taxonomy.getCode();

		PropertiesImpl properties = new PropertiesImpl();
		addPropertyId(properties, PropertyIds.OBJECT_ID, "taxo_" + id);
		objectInfo.setId(id);

		String title = taxonomy.getTitle(language);
		addPropertyString(properties, PropertyIds.NAME, title);
		objectInfo.setName(title);

		addPropertyId(properties, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
		addPropertyId(properties, PropertyIds.OBJECT_TYPE_ID, TAXONOMY_TYPE_ID);
		addPropertyString(properties, PropertyIds.PARENT_ID, taxonomy.getCollection());
		//		addPropertyString(properties, PropertyIds.PARENT_ID, null);
		addPropertyString(properties, PropertyIds.PATH, "/taxo_" + taxonomy.getCode());

		result.setProperties(properties);

		if (context.isObjectInfoRequired()) {
			objectInfo.setObject(result);
			objectInfoHandler.addObjectInfo(objectInfo);
		}

		Set<Action> actions = new HashSet<>();
		actions.add(Action.CAN_GET_PROPERTIES);
		actions.add(Action.CAN_GET_CHILDREN);

		AllowableActionsImpl allowableActions = new AllowableActionsImpl();
		allowableActions.setAllowableActions(actions);
		result.setAllowableActions(allowableActions);

		return result;
	}

	private void setupObjectInfo(ObjectInfoImpl objectInfo) {
		objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
		objectInfo.setTypeId(TAXONOMY_TYPE_ID);
		objectInfo.setContentType(null);
		objectInfo.setFileName(null);
		objectInfo.setHasAcl(false);
		objectInfo.setHasContent(false);
		objectInfo.setVersionSeriesId(null);
		objectInfo.setIsCurrentVersion(true);
		objectInfo.setRelationshipSourceIds(null);
		objectInfo.setRelationshipTargetIds(null);
		objectInfo.setRenditionInfos(null);
		objectInfo.setSupportsDescendants(true);
		objectInfo.setSupportsFolderTree(true);
		objectInfo.setSupportsPolicies(false);
		objectInfo.setSupportsRelationships(false);
		objectInfo.setWorkingCopyId(null);
		objectInfo.setWorkingCopyOriginalId(null);
		//		objectInfo.setHasParent(false);
	}

	private void addPropertyId(PropertiesImpl props, String id, String value) {
		props.addProperty(new PropertyIdImpl(id, value));
	}

	private void addPropertyIdList(PropertiesImpl props, String typeId, Set<String> filter, String id,
								   List<String> value) {

		props.addProperty(new PropertyIdImpl(id, value));
	}

	private void addPropertyString(PropertiesImpl props, String id, String value) {

		props.addProperty(new PropertyStringImpl(id, value));
	}

}
