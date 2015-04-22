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
package com.constellio.app.modules.rm.ui.pages.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsComponentPresenter;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DisplayDocumentPresenter extends DocumentActionsComponentPresenter<DisplayDocumentView> {

	private DisplayDocumentView view;

	public DisplayDocumentPresenter(DisplayDocumentView view) {
		super(view);
		this.view = view;
	}

	public void forParams(String params) {
		Record record = presenterUtils.getRecord(params);
		this.documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY);
		view.setRecordVO(documentVO);
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		User user = presenterUtils.getCurrentUser();
		modelLayerFactory.newLoggingServices().logRecordView(record, user);
	}

	public void viewAssembled() {
		updateActionsComponent();

	}

	@Override
	protected void updateActionsComponent() {
		super.updateActionsComponent();
		view.refreshMetadataDisplay();
		updateContentVersions();
	}

	private void updateContentVersions() {
		List<ContentVersionVO> contentVersionVOs = new ArrayList<ContentVersionVO>();
		Record record = presenterUtils.getRecord(documentVO.getId());
		Document document = new Document(record, presenterUtils.types());

		Content content = document.getContent();
		if (content != null) {
			for (ContentVersion contentVersion : content.getHistoryVersions()) {
				ContentVersionVO contentVersionVO = contentVersionVOBuilder.build(content, contentVersion);
				contentVersionVOs.add(contentVersionVO);
			}
			ContentVersion currentVersion = content.getCurrentVersionSeenBy(presenterUtils.getCurrentUser());
			ContentVersionVO currentVersionVO = contentVersionVOBuilder.build(content, currentVersion);
			contentVersionVOs.remove(currentVersionVO);
			contentVersionVOs.add(currentVersionVO);
		}
		Collections.reverse(contentVersionVOs);
		view.setContentVersions(contentVersionVOs);
	}

}
