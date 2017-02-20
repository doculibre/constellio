package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.FolderDetailToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;

import java.io.IOException;
import java.util.*;

public class OrderDecommissioningListPresenter extends BasePresenter<OrderDecommissioningListView> {
	private Map<String, FolderDetailVO> folderDetailVOs;
	private List<String> codeTitles;
	private String recordId;
	private transient RMSchemasRecordsServices rmRecordsServices;
	private transient DecommissioningService decommissioningService;
	private transient DecommissioningList decommissioningList;
	private transient FolderDetailToVOBuilder folderDetailToVOBuilder;

	public OrderDecommissioningListPresenter(OrderDecommissioningListView view) {
		super(view);
		codeTitles = new ArrayList<>();
	}

	public OrderDecommissioningListPresenter forRecordId(String recordId) {
		this.recordId = recordId;
		init();
		return this;
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
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(collection, modelLayerFactory);

		for (FolderDetailWithType folderDetail : decommissioningList().getFolderDetailsWithType()) {
			FolderDetailVO folderDetailVO = folderDetailToVOBuilder().build(folderDetail);
			folderDetailVOs.put(folderDetail.getFolderId(), folderDetailVO);
			codeTitles.add(folderDetail.getFolderId());
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.EDIT_DECOMMISSIONING_LIST).globally();
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
		view.navigate().to().listFacetConfiguration();
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
		List<DecomListFolderDetail> result = new ArrayList<>();
		for (DecomListFolderDetail folder :  decommissioningList().getFolderDetails()) {
			result.add(folder);
		}

		List<DecomListFolderDetail> sortedResult = new ArrayList<>();
		for(String id: codeTitles) {
			for(DecomListFolderDetail folder: result) {
				if(folder.getFolderId().equals(id)) {
					sortedResult.add(folder);
					break;
				}
			}
		}
		try {
			recordServices().update(decommissioningList().setFolderDetails(sortedResult));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}

		view.navigate().to(RMViews.class).displayDecommissioningList(decommissioningList().getId());
	}

//	public List<String> getFolderDetailsTitle() {
//		if (codeTitles == null) {
//			List<Entry<String, FolderDetailVO>> entries = new ArrayList<>(folderDetailVOs.entrySet());
//			Collections.sort(entries, new Comparator<Entry<String, FolderDetailVO>>() {
//				@Override
//				public int compare(Entry<String, FolderDetailVO> o1, Entry<String, FolderDetailVO> o2) {
//					return new Integer(o1.getValue().getOrder()).compareTo(o2.getValue().getOrder());
//				}
//			});
//
//			codeTitles = new ArrayList<>();
//			for (Map.Entry<String, FolderDetailVO> entry : entries) {
//				codeTitles.add(entry.getKey());
//			}
//		}
//		return codeTitles;
//		return null;
//	}

	public String getLabelForCode(FolderDetailVO folderDetailVO) {
		return recordServices().getDocumentById(folderDetailVO.getFolderId()).getTitle();
	}

	public Record toRecord(RecordVO recordVO) {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchema().getCode(),
				view.getConstellioFactories(), view.getSessionContext());
		return schemaPresenterUtils.toRecord(recordVO);
	}

	public List<FolderDetailVO> getProcessableFolders() {
		if (codeTitles == null) {
			FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
			List<FolderDetailVO> result = new ArrayList<>();
			for (FolderDetailWithType folder :  decommissioningList().getFolderDetailsWithType()) {
				result.add(builder.build(folder));
			}
			return result;
		} else {
			FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
			List<FolderDetailVO> result = new ArrayList<>();
			for (FolderDetailWithType folder :  decommissioningList().getFolderDetailsWithType()) {
				result.add(builder.build(folder));
			}

			List<FolderDetailVO> sortedResult = new ArrayList<>();
			for(String id: codeTitles) {
				for(FolderDetailVO folderDetailVO: result) {
					if(folderDetailVO.getFolderId().equals(id)) {
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

}
