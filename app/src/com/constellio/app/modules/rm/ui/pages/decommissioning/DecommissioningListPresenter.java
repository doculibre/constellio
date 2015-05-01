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
package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.FolderDetailToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;

public class DecommissioningListPresenter extends SingleSchemaBasePresenter<DecommissioningListView> {
	private transient RMSchemasRecordsServices rmRecordsServices;
	private transient DecommissioningService decommissioningService;
	private transient DecommissioningList decommissioningList;
	private transient FolderDetailToVOBuilder folderDetailToVOBuilder;
	String recordId;

	public DecommissioningListPresenter(DecommissioningListView view) {
		super(view, DecommissioningList.DEFAULT_SCHEMA);
	}

	public DecommissioningListPresenter forRecordId(String recordId) {
		this.recordId = recordId;
		return this;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).globally();
	}

	public RecordVO getDecommissioningList() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY);
	}

	public boolean isEditable() {
		return decommissioningService().isEditable(decommissioningList(), getCurrentUser());
	}

	public void editButtonClicked() {
		view.navigateTo().editDecommissioningList(recordId);
	}

	public boolean isDeletable() {
		return decommissioningService().isDeletable(decommissioningList(), getCurrentUser());
	}

	public void deleteButtonClicked() {
		delete(decommissioningList().getWrappedRecord());
		view.navigateTo().decommissioning();
	}

	public boolean isProcessable() {
		return decommissioningService().isProcessable(decommissioningList(), getCurrentUser());
	}

	public void processButtonClicked() {
		decommissioningService().decommission(decommissioningList(), getCurrentUser());
		view.showMessage($("DecommissioningListView.processed"));
		view.navigateTo().displayDecommissioningList(recordId);
	}

	public boolean isProcessed() {
		return decommissioningList().isProcessed();
	}

	public void containerCreationRequested() {
		view.navigateTo().createContainerForDecommissioningList(recordId);
	}

	public void containerSearchRequested() {
		view.navigateTo().searchContainerForDecommissioningList(recordId);
	}

	public void folderPlacedInContainer(FolderDetailVO folder, ContainerVO container) {
		folder.setContainerRecordId(container.getId());
		decommissioningList().getFolderDetail(folder.getFolderId()).setContainerRecordId(container.getId());
		addOrUpdate(decommissioningList().getWrappedRecord());

		view.setProcessable(folder);
		view.updateProcessButtonState(isProcessable());
	}

	public void folderSorted(FolderDetailVO folderVO, boolean value) {
		folderVO.setReversedSort(value);
		FolderDetailWithType folder = decommissioningList().getFolderDetailWithType(folderVO.getFolderId());
		folder.getDetail().setReversedSort(value);
		addOrUpdate(decommissioningList().getWrappedRecord());

		if (decommissioningService().isFolderProcessable(decommissioningList(), folder)) {
			folderVO.setPackageable(false);
			view.setProcessable(folderVO);
		} else {
			folderVO.setPackageable(true);
			view.setPackageable(folderVO);
		}
		view.updateProcessButtonState(isProcessable());
	}

	public void containerStatusChanged(DecomListContainerDetail detail, boolean full) {
		detail.setFull(full);
		addOrUpdate(decommissioningList().getWrappedRecord());
	}

	public List<FolderDetailVO> getPackageableFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (!decommissioningService().isFolderProcessable(decommissioningList(), folder)) {
				result.add(builder.build(folder));
			}
		}
		return result;
	}

	public List<FolderDetailVO> getProcessableFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (decommissioningService().isFolderProcessable(decommissioningList(), folder)) {
				result.add(builder.build(folder));
			}
		}
		return result;
	}

	public List<DecomListContainerDetail> getContainerDetails() {
		return decommissioningList().getContainerDetails();
	}

	public List<ContainerVO> getContainers() {
		List<ContainerVO> result = new ArrayList<>();
		if (decommissioningList().getContainers().isEmpty()) {
			return result;
		}
		for (Record record : recordServices().getRecordsById(view.getCollection(), decommissioningList().getContainers())) {
			ContainerVO containerVO = new ContainerVO(record.getId(), (String) record.get(Schemas.TITLE));
			result.add(containerVO);
		}
		return result;
	}

	public String getSortAction() {
		switch (decommissioningList().getDecommissioningListType()) {
		case FOLDERS_TO_DEPOSIT:
			return "destroy";
		case FOLDERS_TO_DESTROY:
			return "deposit";
		}
		return null;
	}

	public boolean shouldAllowContainerEditing() {
		return decommissioningService().canEditContainers(decommissioningList(), getCurrentUser());
	}

	public boolean shouldDisplayRetentionRuleInDetails() {
		return StringUtils.isBlank(decommissioningList().getUniformRule());
	}

	public boolean shouldDisplayCategoryInDetails() {
		return StringUtils.isBlank(decommissioningList().getUniformCategory());
	}

	public boolean shouldDisplaySort() {
		return decommissioningService().isSortable(decommissioningList());
	}

	DecommissioningService decommissioningService() {
		if (decommissioningService == null) {
			decommissioningService = new DecommissioningService(view.getCollection(), modelLayerFactory);
		}
		return decommissioningService;
	}

	RMSchemasRecordsServices rmRecordsServices() {
		if (rmRecordsServices == null) {
			rmRecordsServices = new RMSchemasRecordsServices(view.getCollection(), modelLayerFactory);
		}
		return rmRecordsServices;
	}

	FolderDetailToVOBuilder folderDetailToVOBuilder() {
		if (folderDetailToVOBuilder == null) {
			folderDetailToVOBuilder = new FolderDetailToVOBuilder(rmRecordsServices());
		}
		return folderDetailToVOBuilder;
	}

	private DecommissioningList decommissioningList() {
		if (decommissioningList == null) {
			decommissioningList = rmRecordsServices().getDecommissioningList(recordId);
		}
		return decommissioningList;
	}
}
