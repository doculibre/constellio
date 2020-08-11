package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.FolderDetailToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.FolderComponent;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.records.RecordServicesException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDecommissioningListPresenter extends BasePresenter<OrderDecommissioningListView> {
	private static Logger LOGGER = LoggerFactory.getLogger(OrderDecommissioningListPresenter.class);

	private Map<String, FolderDetailVO> folderDetailVOs;
	private List<String> codeTitles;
	private String recordId;
	private transient RMSchemasRecordsServices rmRecordsServices;
	private transient DecommissioningService decommissioningService;
	private transient DecommissioningList decommissioningList;
	private transient FolderDetailToVOBuilder folderDetailToVOBuilder;
	private TableType type;

	public enum TableType {
		TO_VALIDATE, PACKAGEABLE, PROCESSABLE, EXCLUDED
	}

	public OrderDecommissioningListPresenter(OrderDecommissioningListView view) {
		super(view);
		codeTitles = new ArrayList<>();
	}

	public OrderDecommissioningListPresenter forParams(String params) {
		if (StringUtils.isNotBlank(params)) {
			String[] parts = params.split("/", 2);
			String recordID = parts[0];
			String type = parts[1];
			forRecordId(recordID);
			forType(type);
			init();
		}
		return this;
	}

	public void forRecordId(String recordId) {
		this.recordId = recordId;
	}

	public void forType(String type) {
		this.type = TableType.valueOf(type);
	}

	public RecordVO getDecommissioningList() {
		return presenterService().getRecordVO(recordId, RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		folderDetailVOs = new HashMap<>();

		for (FolderDetailWithType folderDetail : getLimitedFolderDetailsWithType()) {
			FolderDetailVO folderDetailVO = folderDetailToVOBuilder().build(folderDetail, FolderComponent.NONE);
			folderDetailVOs.put(folderDetail.getFolderId(), folderDetailVO);
			codeTitles.add(folderDetail.getFolderId());
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.EDIT_DECOMMISSIONING_LIST).onSomething();
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigate().to(RMViews.class).displayDecommissioningList(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Record record = recordServices().getDocumentById(recordVO.getId());
		recordServices().logicallyDelete(record, User.GOD);
		recordServices().physicallyDelete(record, User.GOD);
		view.navigate().to().listFacetConfiguration();
	}

	public void cancelButtonClicked() {
		view.navigate().to(RMViews.class).displayDecommissioningList(decommissioningList().getId());
	}

	public void swap(String value, int offset) {
		int current = codeTitles.indexOf(value);
		try {
			Collections.swap(codeTitles, current, current + offset);
		} catch (Exception e) {
			//
		}
	}

	public void saveButtonClicked() {
		List<DecomListFolderDetail> result = getAllFolderDetailsWithType();
		List<DecomListFolderDetail> sortedResult = new ArrayList<>();
		for (String id : codeTitles) {
			for (DecomListFolderDetail folder : result) {
				if (folder.getFolderId().equals(id)) {
					sortedResult.add(folder);
					break;
				}
			}
		}
		for (DecomListFolderDetail folder : result) {
			if (!sortedResult.contains(folder)) {
				sortedResult.add(folder);
			}
		}
		try {
			recordServices().update(decommissioningList().setFolderDetails(sortedResult));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}

		view.navigate().to(RMViews.class).displayDecommissioningList(decommissioningList().getId());
	}

	public String getLabelForCode(String folderId) {
		return recordServices().getDocumentById(folderId).getTitle();
	}

	public Record toRecord(RecordVO recordVO) {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchema().getCode(),
				view.getConstellioFactories(), view.getSessionContext());
		try {
			return schemaPresenterUtils.toRecord(recordVO);
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
		return null;
	}

	public List<FolderDetailVO> getFolderDetails() {
		if (codeTitles == null) {
			FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
			List<FolderDetailVO> result = new ArrayList<>();
			for (FolderDetailWithType folder : getLimitedFolderDetailsWithType()) {
				result.add(builder.build(folder, FolderComponent.NONE));
			}
			return result;
		} else {
			FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
			List<FolderDetailVO> result = new ArrayList<>();
			for (FolderDetailWithType folder : getLimitedFolderDetailsWithType()) {
				result.add(builder.build(folder, FolderComponent.NONE));
			}

			List<FolderDetailVO> sortedResult = new ArrayList<>();
			for (String id : codeTitles) {
				for (FolderDetailVO folderDetailVO : result) {
					if (folderDetailVO.getFolderId().equals(id)) {
						sortedResult.add(folderDetailVO);
						break;
					}
				}
			}
			return sortedResult;
		}
	}

	FolderDetailToVOBuilder folderDetailToVOBuilder() {
		if (folderDetailToVOBuilder == null) {
			folderDetailToVOBuilder = new FolderDetailToVOBuilder(rmRecordsServices());
		}
		return folderDetailToVOBuilder;
	}

	public DecommissioningList decommissioningList() {
		if (decommissioningList == null) {
			decommissioningList = rmRecordsServices().getDecommissioningList(recordId);
		}
		return decommissioningList;
	}

	DecommissioningService decommissioningService() {
		if (decommissioningService == null) {
			decommissioningService = new DecommissioningService(view.getCollection(), appLayerFactory);
		}
		return decommissioningService;
	}

	RMSchemasRecordsServices rmRecordsServices() {
		if (rmRecordsServices == null) {
			rmRecordsServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return rmRecordsServices;
	}

	public List<DecomListValidation> getValidations() {
		return decommissioningList().getValidations();
	}

	private List<FolderDetailWithType> getFoldersToValidate() {
		List<FolderDetailWithType> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded()) {
				result.add(folder);
			}
		}
		return result;
	}

	private List<FolderDetailWithType> getPackageableFolders() {
		List<FolderDetailWithType> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && !decommissioningService().isFolderProcessable(decommissioningList(), folder) && !folder.getDetail().isPlacedInContainer()) {
				result.add(folder);
			}
		}
		return result;
	}

	private List<FolderDetailWithType> getProcessableFolders() {
		List<FolderDetailWithType> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && (decommissioningService().isFolderProcessable(decommissioningList(), folder) || folder.getDetail().isPlacedInContainer())) {
				result.add(folder);
			}
		}
		return result;
	}

	private List<FolderDetailWithType> getExcludedFolders() {
		List<FolderDetailWithType> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isExcluded()) {
				result.add(folder);
			}
		}
		return result;
	}

	private List<FolderDetailWithType> getLimitedFolderDetailsWithType() {
		switch (type) {
			case TO_VALIDATE:
				return getFoldersToValidate();
			case PACKAGEABLE:
				return getPackageableFolders();
			case PROCESSABLE:
				return getProcessableFolders();
			case EXCLUDED:
				return getExcludedFolders();
			default:
				return new ArrayList<>();
		}
	}

	private List<DecomListFolderDetail> getAllFolderDetailsWithType() {
		return decommissioningList().getFolderDetails();
	}
}
