/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.schemas.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class AddEditMetadataPresenter extends SingleSchemaBasePresenter<AddEditMetadataView> {

	private Map<String, String> parameters;
	private String metadataCode;

	public AddEditMetadataPresenter(AddEditMetadataView view) {
		super(view);
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public void setMetadataCode(String metadataCode) {
		this.metadataCode = metadataCode;
	}

	public FormMetadataVO getFormMetadataVO() {
		FormMetadataVO found = null;

		if (metadataCode == null || metadataCode.isEmpty()) {
			return found;
		}

		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		if (types != null) {
			Metadata metadata = types.getMetadata(metadataCode);

			MetadataToFormVOBuilder voBuilder = new MetadataToFormVOBuilder();
			found = voBuilder.build(metadata, displayManager, parameters.get("schemaTypeCode"));
		}

		return found;
	}

	public boolean isInherited(String metadataCode) {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
		return metadata.inheritDefaultSchema();
	}

	public List<String> getMetadataTypesCode() {
		List<String> typeCode = new ArrayList<>();
		for (MetadataSchemaType type : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaTypes()) {
			if (this.isAllowedReferenceType(type)) {
				typeCode.add(type.getCode());
			}
		}

		return typeCode;
	}

	public String getMetadataTypesCaption(String code) {
		MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(code);
		return type.getLabel();
	}

	private boolean isAllowedReferenceType(MetadataSchemaType type) {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchemaType collectionType = types.getSchemaType(Collection.SCHEMA_TYPE);
		MetadataSchemaType eventType = types.getSchemaType(Event.SCHEMA_TYPE);

		return !(type.equals(collectionType) || type.equals(eventType));
	}

	public void saveButtonClicked(FormMetadataVO formMetadataVO, boolean editMode) {
		String schemaCode = getSchemaCode();
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);
		String code;

		MetadataBuilder builder;
		if (!editMode) {
			builder = types.getSchema(schemaCode).create("USR" + formMetadataVO.getLocalcode());
			builder.setMultivalue(formMetadataVO.isMultivalue());
			builder.setType(formMetadataVO.getValueType());
			builder.setSearchable(formMetadataVO.isSearchable());
			builder.setSortable(formMetadataVO.isSortable());
			builder.setSchemaAutocomplete(formMetadataVO.isAutocomplete());
			if (formMetadataVO.getValueType().equals(MetadataValueType.REFERENCE)) {
				MetadataSchemaTypeBuilder refBuilder = types.getSchemaType(formMetadataVO.getReference());
				Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager()
						.getTaxonomyFor(collection, formMetadataVO.getReference());
				if (taxonomy != null) {
					builder.defineTaxonomyRelationshipToType(refBuilder);
				} else {
					builder.defineReferencesTo(refBuilder);
				}
			}
			code = schemaCode + "_" + "USR" + formMetadataVO.getLocalcode();
		} else {
			builder = types.getSchema(schemaCode).get(formMetadataVO.getCode());
			code = formMetadataVO.getCode();
		}

		builder.setEnabled(formMetadataVO.isEnabled());
		builder.setLabel(formMetadataVO.getLabel());
		builder.setDefaultRequirement(formMetadataVO.isRequired());

		try {
			schemasManager.saveUpdateSchemaTypes(types);
		} catch (OptimistickLocking optimistickLocking) {
			// TODO exception gestion
			throw new RuntimeException(optimistickLocking);
		}

		saveDisplayConfig(formMetadataVO, code, schemasManager, editMode);

		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_METADATA, parameters);
		view.navigateTo().listSchemaMetadata(params);
	}

	private void saveDisplayConfig(FormMetadataVO formMetadataVO, String code, MetadataSchemasManager schemasManager,
			boolean editMode) {
		SchemasDisplayManager displayManager = schemasDisplayManager();
		MetadataInputType type = formMetadataVO.getInput();

		if (type == null) {
			type = MetadataInputType.FIELD;
		}

		MetadataDisplayConfig displayConfig;

		displayConfig = displayManager.getMetadata(collection, code);
		if (displayConfig == null) {
			displayConfig = new MetadataDisplayConfig(collection, code, formMetadataVO.isAdvancedSearch(),
					type, formMetadataVO.isHighlight(), formMetadataVO.getMetadataGroup());
		} else {
			displayConfig = displayConfig.withHighlightStatus(formMetadataVO.isHighlight())
					.withVisibleInAdvancedSearchStatus(formMetadataVO.isAdvancedSearch()).withInputType(type)
					.withMetadataGroup(formMetadataVO.getMetadataGroup());
		}

		displayManager.saveMetadata(displayConfig);

		this.saveFacetDisplay(schemasManager, displayManager, code, formMetadataVO.isFacet());
		if (!editMode) {
			this.saveSchemaDisplay(schemasManager, displayManager, code);
		}
	}

	private void saveSchemaDisplay(MetadataSchemasManager schemasManager, SchemasDisplayManager displayManager, String code) {
		SchemaDisplayConfig schemaConfig = displayManager.getSchema(collection, getSchemaCode());
		List<String> displayMetadata = new ArrayList<>(schemaConfig.getDisplayMetadataCodes());
		List<String> formMetadata = new ArrayList<>(schemaConfig.getFormMetadataCodes());

		displayMetadata.add(code);
		formMetadata.add(code);

		schemaConfig = schemaConfig.withDisplayMetadataCodes(displayMetadata);
		schemaConfig = schemaConfig.withFormMetadataCodes(formMetadata);

		displayManager.saveSchema(schemaConfig);
	}

	private void saveFacetDisplay(MetadataSchemasManager schemasManager, SchemasDisplayManager displayManager, String code,
			boolean isFacet) {
		Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(code);

		boolean isGlobal = false;
		for (Metadata global : Schemas.getAllGlobalMetadatas()) {
			if (metadata.getCode().equals(global.getCode())) {
				isGlobal = true;
				break;
			}
		}

		SchemaTypesDisplayConfig typesConfig = displayManager.getTypes(collection);
		List<String> facets = new ArrayList<>(typesConfig.getFacetMetadataCodes());

		if (isFacet) {
			if (isGlobal) {
				if (!facets.contains(metadata.getLocalCode()))
					facets.add(metadata.getLocalCode());
			} else {
				if (!facets.contains(metadata.getCode()))
					facets.add(metadata.getCode());
			}
		} else {
			if (facets.contains(metadata.getLocalCode())) {
				facets.remove(metadata.getLocalCode());
			} else if (facets.contains(metadata.getCode())) {
				facets.remove(metadata.getCode());
			}
		}

		typesConfig = typesConfig.withFacetMetadataCodes(facets);
		displayManager.saveTypes(typesConfig);
	}

	public List<String> getMetadataGroupList() {
		SchemaTypeDisplayConfig schemaConfig = schemasDisplayManager().getType(collection, parameters.get("schemaTypeCode"));
		return schemaConfig.getMetadataGroup();
	}

	public void cancelButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_SCHEMA_METADATA, parameters);
		view.navigateTo().listSchemaMetadata(params);
	}
}
