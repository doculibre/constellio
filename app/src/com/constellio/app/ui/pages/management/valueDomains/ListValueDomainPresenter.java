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
package com.constellio.app.ui.pages.management.valueDomains;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ListValueDomainPresenter extends BasePresenter<ListValueDomainView> {

	private List<String> labels;

	public ListValueDomainPresenter(ListValueDomainView view) {
		super(view);
	}

	public void valueDomainCreationRequested(String valueDomain) {
		valueDomain = valueDomain.trim();
		boolean canCreate = canCreate(valueDomain);
		if (canCreate) {
			valueListServices().createValueDomain(valueDomain);
			view.refreshTable();
			labels.add(valueDomain);
		}
	}

	public void displayButtonClicked(MetadataSchemaTypeVO schemaType) {
		view.navigateTo().listSchemaRecords(schemaType.getCode() + "_default");
	}

	public void editButtonClicked(MetadataSchemaTypeVO schemaTypeVO, String newLabel) {
		if (!verifyIfExists(newLabel)) {
			MetadataSchemaTypesBuilder metadataSchemaTypesBuilder = modelLayerFactory.getMetadataSchemasManager()
					.modify(view.getCollection());
			metadataSchemaTypesBuilder.getSchemaType(schemaTypeVO.getCode()).setLabel(newLabel);
			try {
				modelLayerFactory.getMetadataSchemasManager().saveUpdateSchemaTypes(metadataSchemaTypesBuilder);
			} catch (OptimistickLocking optimistickLocking) {
				throw new RuntimeException(optimistickLocking);
			}
			view.refreshTable();
		} else {
			view.showErrorMessage("Un domaine de valeur avec ce titre existe déjà.");
		}
	}

	public List<MetadataSchemaTypeVO> getDomainValues() {
		labels = new ArrayList<>();
		MetadataSchemaTypeToVOBuilder builder = newMetadataSchemaTypeToVOBuilder();
		List<MetadataSchemaTypeVO> result = new ArrayList<>();
		for (MetadataSchemaType schemaType : valueListServices().getValueDomainTypes()) {
			result.add(builder.build(schemaType));
			labels.add(schemaType.getLabel().trim());
		}
		return result;
	}

	MetadataSchemaTypeToVOBuilder newMetadataSchemaTypeToVOBuilder() {
		return new MetadataSchemaTypeToVOBuilder();
	}

	ValueListServices valueListServices() {
		return new ValueListServices(appLayerFactory, view.getCollection());
	}

	boolean canCreate(String valueDomain) {
		valueDomain = valueDomain.trim();
		boolean canCreate = false;
		if (StringUtils.isNotBlank(valueDomain)) {
			boolean exist = verifyIfExists(valueDomain);
			canCreate = !exist;
		}
		return canCreate;
	}

	private boolean verifyIfExists(String valueDomain) {
		if (labels == null) {
			getDomainValues();
		}
		boolean exits = false;
		for (String label : labels) {
			if (label.equals(valueDomain)) {
				exits = true;
			}
		}
		return exits;
	}

	public void backButtonClicked() {
		view.navigateTo().adminModule();
	}

}
