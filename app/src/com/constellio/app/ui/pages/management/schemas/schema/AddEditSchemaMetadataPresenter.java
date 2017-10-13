package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.Schemas.CREATED_BY;
import static com.constellio.model.entities.schemas.Schemas.CREATED_ON;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.entities.schemas.Schemas.MODIFIED_BY;
import static com.constellio.model.entities.schemas.Schemas.MODIFIED_ON;
import static com.constellio.model.entities.schemas.Schemas.PATH;

import java.util.LinkedHashMap;
import java.util.Map;

import com.constellio.app.services.metadata.MetadataDeletionException;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CalculatedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CopiedMetadataReference;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CopiedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_ExtractedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_FacetMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_InheritedMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_PopulatedMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_SystemMetadata;
import com.constellio.app.services.metadata.MetadataDeletionService;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public class AddEditSchemaMetadataPresenter extends SingleSchemaBasePresenter<AddEditSchemaMetadataView> {
	private transient MetadataDeletionService metadataDeletionService;

	public AddEditSchemaMetadataPresenter(AddEditSchemaMetadataView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	private Map<String, String> parameters;

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public Map<String, MetadataVODataProvider> getDataProviders() {
		Map<String, MetadataVODataProvider> dataProviders = new LinkedHashMap<>();
		String schemaCode = getSchemaCode();

		MetadataVODataProvider custom = new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection,
				schemaCode) {
			@Override
			protected boolean isAccepted(Metadata metadata) {
				return metadata.getLocalCode().startsWith("USR")
						&& metadata.isEnabled()
						&& !isDDVOrTaxonomy(metadata)
						&& isNotAHiddenSystemReserved(metadata);
			}
		};

		MetadataVODataProvider system = new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection,
				schemaCode) {
			@Override
			protected boolean isAccepted(Metadata metadata) {
				return !metadata.getLocalCode().startsWith("USR")
						&& metadata.isEnabled()
						&& !isDDVOrTaxonomy(metadata)
						&& isNotAHiddenSystemReserved(metadata);
			}
		};

		MetadataVODataProvider ddvTaxonomies = new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory,
				collection, schemaCode) {
			@Override
			protected boolean isAccepted(Metadata metadata) {
				return metadata.isEnabled()
						&& isDDVOrTaxonomy(metadata)
						&& isNotAHiddenSystemReserved(metadata);
			}
		};

		MetadataVODataProvider disabled = new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection,
				schemaCode) {
			@Override
			protected boolean isAccepted(Metadata metadata) {
				return !metadata.isEnabled()
						&& isNotAHiddenSystemReserved(metadata);
			}
		};

		dataProviders.put($("AddEditSchemaMetadataView.tabs.custom", custom.size()), custom);
		dataProviders.put($("AddEditSchemaMetadataView.tabs.ddvsAndTaxonomies", ddvTaxonomies.size()), ddvTaxonomies);
		dataProviders.put($("AddEditSchemaMetadataView.tabs.system", system.size()), system);
		dataProviders.put($("AddEditSchemaMetadataView.tabs.disabled", disabled.size()), disabled);

		return dataProviders;
	}

	private boolean isDDVOrTaxonomy(Metadata metadata) {
		if (metadata.getType() == MetadataValueType.REFERENCE) {
			String referencedSchemaType = metadata.getReferencedSchemaType();
			return referencedSchemaType.startsWith("taxo")
					|| (metadata.getLocalCode().startsWith("USR") && referencedSchemaType.startsWith("ddv"));

		} else {
			return false;
		}

	}

	private boolean isNotAHiddenSystemReserved(Metadata metadata) {
		return !metadata.isSystemReserved() || metadata.isSameLocalCodeThanAny(
				IDENTIFIER, CREATED_BY, MODIFIED_BY, CREATED_ON, MODIFIED_ON, LEGACY_ID, PATH);
	}

	public FormMetadataSchemaVO getSchemaVO() {
		String schemaCode = getSchemaCode();
		FormMetadataSchemaVO schemaVO = null;
		if (schemaCode != null) {
			MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchema schema = manager.getSchemaTypes(collection).getSchema(schemaCode);
			schemaVO = new MetadataSchemaToFormVOBuilder().build(schema, view.getSessionContext());
		}

		return schemaVO;
	}

	public void addButtonClicked() {
		parameters.put("schemaCode", getSchemaCode());
		parameters.put("metadataCode", "");
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_METADATA, parameters);
		view.navigate().to().addMetadata(params);
	}

	public void editButtonClicked(MetadataVO metadataVO) {
		parameters.put("schemaCode", getSchemaCode());
		parameters.put("metadataCode", metadataVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_METADATA, parameters);
		view.navigate().to().editMetadata(params);
	}

	public void backButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_SCHEMA, parameters);
		view.navigate().to().listSchema(params);
	}

	public void deleteButtonClicked(MetadataVO entity) {
		try {
			metadataDeletionService().deleteMetadata(entity.getCode());
			String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_METADATA, parameters);
			view.navigate().to().listSchemaMetadata(params);

		} catch (MetadataDeletionException e) {
			view.showErrorMessage(getAppropriateMessage(e));
		}
	}

	private String getAppropriateMessage(MetadataDeletionException e) {
		if (e instanceof MetadataDeletionException_SystemMetadata) {
			return $("MetadataDeletionException_SystemMetadata");
		} else if (e instanceof MetadataDeletionException_PopulatedMetadata) {
			return $("MetadataDeletionException_PopulatedMetadata");
		} else if (e instanceof MetadataDeletionException_CopiedMetadataSource) {
			return $("MetadataDeletionException_CopiedMetadataSource");
		} else if (e instanceof MetadataDeletionException_CopiedMetadataReference) {
			return $("MetadataDeletionException_CopiedMetadataReference");
		} else if (e instanceof MetadataDeletionException_CalculatedMetadataSource) {
			return $("MetadataDeletionException_CalculatedMetadataSource");
		} else if (e instanceof MetadataDeletionException_ExtractedMetadataSource) {
			return $("MetadataDeletionException_ExtractedMetadataSource");
		} else if (e instanceof MetadataDeletionException_InheritedMetadata) {
			return $("MetadataDeletionException_InheritedMetadata");
		} else if (e instanceof MetadataDeletionException_FacetMetadata) {
			return $("MetadataDeletionException_FacetMetadata");
		}
		return null;
	}

	private MetadataDeletionService metadataDeletionService() {
		if (metadataDeletionService == null) {
			this.metadataDeletionService = new MetadataDeletionService(appLayerFactory, collection);
		}
		return metadataDeletionService;
	}

	public boolean isMetadataDeletable(MetadataVO entity) {
		return metadataDeletionService().isMetadataDeletable(entity.getCode());
	}
}
