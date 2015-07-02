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
package com.constellio.app.modules.rm.ui.pages.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleFieldFactory;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleForm;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

import java.util.List;

public class AddEditRetentionRuleViewImpl extends BaseViewImpl implements AddEditRetentionRuleView {
	private final AddEditRetentionRulePresenter presenter;
	private RetentionRuleVO retentionRuleVO;

	private RetentionRuleFieldFactory fieldFactory;
	private RetentionRuleForm form;

	private Boolean delayedDisposalTypeVisibleForDocumentTypes;

	public AddEditRetentionRuleViewImpl() {
		presenter = new AddEditRetentionRulePresenter(this);
		fieldFactory = new RetentionRuleFieldFactory() {
			@Override
			protected List<VariableRetentionPeriodVO> getOpenPeriodsDDVList() {
				return presenter.getOpenPeriodsDDVList();
			}

			@Override
			protected void onDisposalTypeChange(CopyRetentionRule copyRetentionRule) {
				presenter.disposalTypeChanged(copyRetentionRule);
			}

		};
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setRetentionRule(RetentionRuleVO retentionRuleVO) {
		this.retentionRuleVO = retentionRuleVO;
	}

	@Override
	protected String getTitle() {
		String titleKey;
		if (presenter.isAddView()) {
			titleKey = "AddEditRetentionRuleView.addViewTitle";
		} else {
			titleKey = "AddEditRetentionRuleView.editViewTitle";
		}
		return $(titleKey);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		form = new RetentionRuleForm(retentionRuleVO, fieldFactory) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked();
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};
		if (delayedDisposalTypeVisibleForDocumentTypes != null) {
			setDisposalTypeVisibleForDocumentTypes(delayedDisposalTypeVisibleForDocumentTypes);
		}
		return form;
	}

	@Override
	public void setDisposalTypeVisibleForDocumentTypes(boolean visible) {
		if (form != null) {
			form.getDocumentTypesField().setDisposalTypeVisible(visible);
		} else {
			delayedDisposalTypeVisibleForDocumentTypes = visible;
		}
	}
}
