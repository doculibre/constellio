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
package com.constellio.app.ui.pages.management.taxonomy;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;

@SuppressWarnings("serial")
public class AddEditTaxonomyConceptPresenter extends SingleSchemaBasePresenter<AddEditTaxonomyConceptView> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditTaxonomyConceptPresenter.class);

	public static final String EDIT = "EDIT";
	public static final String ADD = "ADD";

	private String taxonomyCode;
	private String conceptId;
	private String operation;

	public AddEditTaxonomyConceptPresenter(AddEditTaxonomyConceptView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
	}

	public RecordVO getRecordVO() {
		Record record;
		if (operation.equals(EDIT)) {
			record = recordServices().getDocumentById(conceptId);
		} else {
			record = newRecord();
			if (conceptId != null) {
				Record parentRecord = recordServices().getDocumentById(conceptId);
				for (Metadata metadata : schema().getParentReferences()) {
					if (metadata.getAllowedReferences().isAllowed(schema(parentRecord.getSchemaCode()))) {
						record.set(metadata, parentRecord.getId());
					}
				}
			}
		}
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(record, VIEW_MODE.FORM, view.getSessionContext());
	}

	public void forElementInTaxonomy(String parameters) {
		String schemaCode = getSchemaCode();
		String[] splitParams = splitTaxonomyCodeAndElementId(parameters);
		operation = splitParams[0];
		taxonomyCode = splitParams[1];
		schemaCode = splitParams.length < 3 ? null : splitParams[2];
		conceptId = splitParams.length < 4 ? null : splitParams[3];
		setSchemaCode(schemaCode);
	}

	String[] splitTaxonomyCodeAndElementId(String parameters) {
		if (parameters == null || parameters.equals("")) {
			throw new RuntimeException("Replace with a 404 OR Invalid parameters");
		} else {
			return parameters.split("/", 4);
		}
	}

	public void saveButtonClicked(RecordVO recordVO) {
		try {
			Record record = toRecord(recordVO);
			addOrUpdateWithoutUser(record);
			view.navigateTo().taxonomyManagement(taxonomyCode, conceptId, null);
		} catch (Exception e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void cancelButtonClicked(RecordVO recordVO) {
		view.navigateTo().taxonomyManagement(taxonomyCode, conceptId, null);
	}

	public String getConceptId() {
		return conceptId;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		forElementInTaxonomy(params);
		return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user);
	}
}
