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
package com.constellio.app.ui.pages.management.collections;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class AddEditCollectionViewImpl extends BaseViewImpl implements AddEditCollectionView {

	public static final String CODE_FIELD_STYLE = "seleniumCodeFieldStyle";
	public static final String NAME_FIELD_STYLE = "seleniumNameFieldStyle";
	public static final String BASE_FORM_STYLE = "seleniumBaseFormStyle";
	private AddEditCollectionPresenter presenter;

	private CollectionVO collectionVO;

	@PropertyId("code")
	private TextField codeField;

	@PropertyId("name")
	private TextField nameField;

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		setupParamsAndVO(event);
	}

	private void setupParamsAndVO(ViewChangeEvent event) {
		String parameters = event.getParameters();
		Map<String, String> paramsMap = ParamUtils.getParamsMap(parameters);
		String code = paramsMap.get("collectionCode");
		this.presenter = new AddEditCollectionPresenter(this, code);
		collectionVO = presenter.getCollectionVO();
	}

	@Override
	protected String getTitle() {
		return $("AddEditCollectionView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClick();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		codeField = new TextField();
		codeField.addStyleName(CODE_FIELD_STYLE);
		codeField.setCaption($("AddEditCollectionView.Code"));
		codeField.setRequired(true);
		codeField.setNullRepresentation("");
		codeField.setId("code");
		codeField.addStyleName("code");
		codeField.addStyleName("code-" + collectionVO.getCode());
		codeField.setEnabled(!presenter.getActionEdit());

		nameField = new TextField();
		nameField.addStyleName(NAME_FIELD_STYLE);
		nameField.setCaption($("AddEditCollectionView.Name"));
		nameField.setNullRepresentation("");
		nameField.setId("name");
		nameField.addStyleName("name");
		nameField.addStyleName("name-" + collectionVO.getName());

		BaseForm<CollectionVO> baseFormComponent = new BaseForm<CollectionVO>(collectionVO, this, codeField, nameField) {

			@Override
			protected void saveButtonClick(CollectionVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked(collectionVO);
			}

			@Override
			protected void cancelButtonClick(CollectionVO viewObject) {
				presenter.cancelButtonClicked();
			}

		};
		baseFormComponent.addStyleName(BASE_FORM_STYLE);
		return baseFormComponent;
	}
}
