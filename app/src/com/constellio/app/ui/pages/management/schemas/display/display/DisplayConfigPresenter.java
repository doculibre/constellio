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
package com.constellio.app.ui.pages.management.schemas.display.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

public class DisplayConfigPresenter extends SingleSchemaBasePresenter<DisplayConfigView> {

	private Map<String, String> parameters;

	public DisplayConfigPresenter(DisplayConfigView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public MetadataVODataProvider getDataProvider() {
		return new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection, getSchemaCode());
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public List<FormMetadataVO> getMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataList list = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadatas();
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder();
		for (Metadata metadata : list) {
			if (this.isAllowedMetadata(metadata)) {
				formMetadataVOs.add(builder.build(metadata, displayManager, parameters.get("schemaTypeCode")));
			}
		}

		return formMetadataVOs;
	}

	public List<FormMetadataVO> getValueMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		SchemasDisplayManager displayManager = schemasDisplayManager();
		List<String> codeList = displayManager.getSchema(collection, getSchemaCode()).getDisplayMetadataCodes();

		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder();
		for (String metadataCode : codeList) {
			Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
			if (this.isAllowedMetadata(metadata)) {
				formMetadataVOs.add(builder.build(metadata, displayManager, parameters.get("schemaTypeCode")));
			}
		}

		return formMetadataVOs;
	}

	private boolean isAllowedMetadata(Metadata metadata) {
		List<Metadata> restrictedMetadata = Arrays.asList(Schemas.SCHEMA, Schemas.VERSION, Schemas.PATH, Schemas.PRINCIPAL_PATH,
				Schemas.PARENT_PATH, Schemas.AUTHORIZATIONS, Schemas.REMOVED_AUTHORIZATIONS, Schemas.INHERITED_AUTHORIZATIONS,
				Schemas.ALL_AUTHORIZATIONS, Schemas.IS_DETACHED_AUTHORIZATIONS, Schemas.TOKENS, Schemas.COLLECTION,
				Schemas.FOLLOWERS, Schemas.LOGICALLY_DELETED_STATUS);

		List<String> localCodes = new SchemaUtils().toMetadataLocalCodes(restrictedMetadata);

		return !localCodes.contains(metadata.getLocalCode());
	}

	public void saveButtonClicked(List<FormMetadataVO> schemaVOs) {
		SchemasDisplayManager manager = schemasDisplayManager();
		SchemaDisplayConfig config = schemasDisplayManager().getSchema(collection, getSchemaCode());

		List<String> metadataCode = new ArrayList<>();
		for (FormMetadataVO formMetadataVO : schemaVOs) {
			metadataCode.add(formMetadataVO.getCode());
		}

		config = config.withDisplayMetadataCodes(metadataCode);
		manager.saveSchema(config);

		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigateTo().listSchema(params);
	}

	public void cancelButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigateTo().listSchema(params);
	}
}
