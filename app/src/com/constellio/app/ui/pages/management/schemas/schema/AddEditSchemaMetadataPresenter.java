package com.constellio.app.ui.pages.management.schemas.schema;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.metadata.MetadataDeletionException;
import com.constellio.app.services.metadata.MetadataDeletionException.*;
import com.constellio.app.services.metadata.MetadataDeletionService;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.Schemas.*;

public class AddEditSchemaMetadataPresenter extends SingleSchemaBasePresenter<AddEditSchemaMetadataView> implements MetadataValueForProperty {
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

	public Object getValue(Object propertyIdObj, MetadataVO metadata) {
		String propertyId = (String) propertyIdObj;

		if (AddEditSchemaMetadataViewImpl.PROPERTY_ID_CAPTION.equals(propertyId)) {
			return metadata.getLabel();
		} else if (AddEditSchemaMetadataViewImpl.PROPERTY_ID_ENABLED_CAPTION.equals(propertyId)) {
			return $("AddEditSchemaMetadataView." + metadata.isEnabled());
		} else if (AddEditSchemaMetadataViewImpl.PROPERTY_ID_INPUT_CAPTION.equals(propertyId)) {
			return $(MetadataInputType.getCaptionFor(metadata.getMetadataInputType()));
		} else if (AddEditSchemaMetadataViewImpl.PROPERTY_ID_LOCAL_CODE.equals(propertyId)) {
			return StringUtils.defaultIfBlank(metadata.getCode(), "");
		} else if (AddEditSchemaMetadataViewImpl.PROPERTY_ID_REQUIRED_CAPTION.equals(propertyId)) {
			return $("AddEditSchemaMetadataView." + metadata.isRequired());
		} else if (AddEditSchemaMetadataViewImpl.PROPERTY_ID_VALUE_CAPTION.equals(propertyId)) {
			return $(MetadataValueType.getCaptionFor(metadata.getType()));
		} else {
			throw new ImpossibleRuntimeException("not implemented");
		}
	}

	public Locale getCurrentLocale() {
		return view.getSessionContext().getCurrentLocale();
	}

	public Map<String, MetadataVODataProvider> getDataProviders() {
		Map<String, MetadataVODataProvider> dataProviders = new LinkedHashMap<>();
		String schemaCode = getSchemaCode();

		MetadataVODataProvider custom = new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection,
				schemaCode, this) {
			@Override
			protected boolean isAccepted(Metadata metadata) {
				return metadata.getLocalCode().startsWith("USR")
					   && metadata.isEnabled()
					   && !isDDVOrTaxonomy(metadata)
					   && isNotAHiddenSystemReserved(metadata);
			}
		};

		MetadataVODataProvider system = new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection,
				schemaCode, this) {
			@Override
			protected boolean isAccepted(Metadata metadata) {
				return !metadata.getLocalCode().startsWith("USR")
					   && metadata.isEnabled()
					   && !isDDVOrTaxonomy(metadata)
					   && isNotAHiddenSystemReserved(metadata);
			}
		};

		MetadataVODataProvider ddvTaxonomies = new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory,
				collection, schemaCode, this) {
			@Override
			protected boolean isAccepted(Metadata metadata) {
				return metadata.isEnabled()
					   && isDDVOrTaxonomy(metadata)
					   && isNotAHiddenSystemReserved(metadata);
			}
		};

		MetadataVODataProvider disabled = new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection,
				schemaCode, this) {
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
			String referencedSchemaType = metadata.getReferencedSchemaTypeCode();
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
			schemaVO = new MetadataSchemaToFormVOBuilder().build(schema, schema.getLocalCode(), view.getSessionContext(), null, true);
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
			MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchema schema = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode());
			Metadata metadata = schema.getMetadata(entity.getCode());
			User user = getCurrentUser();

			appCollectionExtentions.metadataDeletedFromView(metadata, user);
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
