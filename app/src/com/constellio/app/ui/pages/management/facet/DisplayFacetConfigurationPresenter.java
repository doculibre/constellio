package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.frameworks.validation.OptimisticLockException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayFacetConfigurationPresenter extends BasePresenter<DisplayFacetConfigurationView> {

	private RecordVO displayRecordVO;
	private RecordVO recordVO;
	FacetConfigurationPresenterService service;

	public DisplayFacetConfigurationPresenter(DisplayFacetConfigurationView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		service = new FacetConfigurationPresenterService(view.getConstellioFactories(), view.getSessionContext());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_FACETS).globally();
	}

	public void deleteButtonClicked() {
		Record record = recordServices().getDocumentById(recordVO.getId());
		recordServices().logicallyDelete(record, User.GOD);
		recordServices().physicallyDelete(record, User.GOD);
		view.navigate().to().listFacetConfiguration();
	}

	public void editButtonClicked() {
		view.navigate().to().editFacetConfiguration(displayRecordVO.getId());
	}

	public void setDisplayRecordVO(String id) {
		Record record = recordServices().getDocumentById(id);
		List<String> metadatas = new ArrayList<>();
		metadatas.add(Facet.TITLE);
		metadatas.add(Facet.FACET_TYPE);
		metadatas.add(Facet.ORDER_RESULT);
		metadatas.add(Facet.FIELD_VALUES_LABEL);
		metadatas.add(Facet.LIST_QUERIES);

		final MetadataSchemaVO facetDefaultVO = new MetadataSchemaToVOBuilder().build(schema(Facet.DEFAULT_SCHEMA),
				VIEW_MODE.TABLE, metadatas, view.getSessionContext());

		displayRecordVO = new RecordToVOBuilder().build(record, VIEW_MODE.DISPLAY, facetDefaultVO, view.getSessionContext());
		recordVO = new RecordToVOBuilder().build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public RecordVO getDisplayRecordVO() {
		return displayRecordVO;
	}

	public MapStringStringStructure getValues() {
		FacetType type = recordVO.get(Facet.FACET_TYPE);
		switch (type) {
			case FIELD:
				return recordVO.get(Facet.FIELD_VALUES_LABEL);
			case QUERY:
				return recordVO.get(Facet.LIST_QUERIES);
			default:
				throw new ImpossibleRuntimeException("Unknown type");
		}
	}

	public String getTypePostfix() {
		FacetType type = recordVO.get(Facet.FACET_TYPE);
		return type.getCode();
	}

	public void backButtonClicked() {
		view.navigate().to().listFacetConfiguration();
	}

	public Record toRecord(RecordVO recordVO) throws OptimisticLockException {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchema().getCode(),
				view.getConstellioFactories(), view.getSessionContext());
		return schemaPresenterUtils.toRecord(recordVO);
	}

	public void activate() {
		try {
			service.activate(recordVO.getId());
			view.navigate().to().displayFacetConfiguration(recordVO.getId());
		} catch (Exception e) {
			view.showErrorMessage($("DisplayFacetConfiguration.cannotActivateFacet", recordVO.getTitle()));
		}
	}

	public void deactivate() {
		try {
			service.deactivate(recordVO.getId());
			view.navigate().to().displayFacetConfiguration(recordVO.getId());
		} catch (Exception e) {
			view.showErrorMessage($("DisplayFacetConfiguration.cannotDeactivateFacet", recordVO.getTitle()));
		}
	}

	public boolean isActive() {
		return service.isActive(recordVO);
	}
}
