package com.constellio.app.modules.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class CoreTypes {

	public static List<String> schemaTypesCodes;

	static {
		List<String> codesList = new ArrayList<>();
		codesList.add(Facet.SCHEMA_TYPE);
		codesList.add(Collection.SCHEMA_TYPE);
		codesList.add(TemporaryRecord.SCHEMA_TYPE);
		codesList.add(SavedSearch.SCHEMA_TYPE);
		codesList.add(Event.SCHEMA_TYPE);
		codesList.add(EmailToSend.SCHEMA_TYPE);
		codesList.add(Capsule.SCHEMA_TYPE);
		codesList.add(User.SCHEMA_TYPE);
		codesList.add(Group.SCHEMA_TYPE);
		codesList.add(UserDocument.SCHEMA_TYPE);
		codesList.add(UserFolder.SCHEMA_TYPE);
		codesList.add(Printable.SCHEMA_TYPE);
		codesList.add(SolrAuthorizationDetails.SCHEMA_TYPE);
		codesList.add(Report.SCHEMA_TYPE);

		//Deprecated types :
		codesList.add(WorkflowTask.SCHEMA_TYPE);

		schemaTypesCodes = Collections.unmodifiableList(codesList);

	}

	public static List<MetadataSchemaType> coreSchemaTypes(AppLayerFactory appLayerFactory, String collection) {
		return coreSchemaTypes(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public static List<MetadataSchemaType> coreSchemaTypes(MetadataSchemaTypes types) {

		List<MetadataSchemaType> schemaTypes = new ArrayList<>();

		for (String code : schemaTypesCodes) {
			if (types.hasType(code)) {
				schemaTypes.add(types.getSchemaType(code));
			}
		}

		return Collections.unmodifiableList(schemaTypes);
	}

	public static List<MetadataSchema> coreSchemas(AppLayerFactory appLayerFactory, String collection) {
		return coreSchemas(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public static List<MetadataSchema> coreSchemas(MetadataSchemaTypes types) {
		List<MetadataSchemaType> taskTypes = coreSchemaTypes(types);
		List<MetadataSchema> taskSchemas = new ArrayList<>();

		for (MetadataSchemaType schemaType : taskTypes) {
			taskSchemas.addAll(schemaType.getAllSchemas());
		}
		return taskSchemas;
	}

}
