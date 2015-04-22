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
package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class AddEditSchemaViewImpl extends BaseViewImpl implements AddEditSchemaView {

	AddEditSchemaPresenter presenter;
	@PropertyId("localCode")
	private BaseTextField localCodeField;
	@PropertyId("label")
	private BaseTextField labelField;

	public AddEditSchemaViewImpl() {
		this.presenter = new AddEditSchemaPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("AddEditSchemaView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
		presenter.setParameters(paramsMap);
		presenter.setSchemaCode(paramsMap.get("schemaCode"));

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(buildForm());
		return viewLayout;
	}

	private Component buildForm() {
		FormMetadataSchemaVO schemaVO = presenter.getSchemaVO();

		final boolean editMode = schemaVO != null;
		if (!editMode) {
			schemaVO = new FormMetadataSchemaVO();
		}

		localCodeField = new BaseTextField($("AddEditSchemaView.localCode"));
		localCodeField.setId("localCode");
		localCodeField.addStyleName("localCode");
		localCodeField.setEnabled(!editMode);
		localCodeField.setRequired(true);

		labelField = new BaseTextField($("AddEditSchemaView.title"));
		labelField.setRequired(true);
		labelField.setId("label");
		labelField.addStyleName("label");

		return new BaseForm<FormMetadataSchemaVO>(schemaVO, this, localCodeField, labelField) {
			@Override
			protected void saveButtonClick(FormMetadataSchemaVO schemaVO)
					throws ValidationException {
				presenter.saveButtonClicked(schemaVO, editMode);
			}

			@Override
			protected void cancelButtonClick(FormMetadataSchemaVO metadataVO) {
				presenter.cancelButtonClicked();
			}
		};
	}
}
