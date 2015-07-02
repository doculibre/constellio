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
package com.constellio.app.modules.rm.ui.pages.containers.edit;

import com.constellio.app.modules.rm.ui.components.document.DocumentForm;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

import static com.constellio.app.ui.i18n.i18n.$;

public class EditContainerViewImpl extends BaseViewImpl implements EditContainerView{
    private RecordVO recordVO;

    private RecordForm recordForm;

    private EditContainerPresenter presenter;

    public EditContainerViewImpl() {
        presenter = new EditContainerPresenter(this);
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeEvent event) {
        recordVO = presenter.getContainerRecord(event.getParameters());
    }

    @Override
    protected String getTitle() {
        return $("EditContainerViewImpl.editViewTitle");
    }

    @Override
    protected Component buildMainComponent(ViewChangeEvent event) {
        return newForm();
    }

    private RecordForm newForm() {
        recordForm = new RecordForm(recordVO, new ContainerFieldFactory()) {
            @Override
            protected void saveButtonClick(RecordVO viewObject)
                    throws ValidationException {
                presenter.saveButtonClicked(recordVO);
            }

            @Override
            protected void cancelButtonClick(RecordVO viewObject) {
                presenter.cancelButtonClicked();
            }

        };

        return recordForm;
    }
}
