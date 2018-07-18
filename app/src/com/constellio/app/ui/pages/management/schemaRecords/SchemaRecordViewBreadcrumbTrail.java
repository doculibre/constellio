package com.constellio.app.ui.pages.management.schemaRecords;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.services.records.RecordServices;

public abstract class SchemaRecordViewBreadcrumbTrail extends TitleBreadcrumbTrail {

	private RecordToVOBuilder voBuilder = new RecordToVOBuilder();

	public SchemaRecordViewBreadcrumbTrail(BaseView view, String viewTitle) {
		super(view, viewTitle);
	}

	@Override
	public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
		RecordServices recordServices = ConstellioFactories.getInstance().getModelLayerFactory().newRecordServices();
		List<IntermediateBreadCrumbTailItem> items = new ArrayList<>();
		items.add(new ListValueDomainsBreadcrumbItem());
		MetadataSchemaVO schemaVO = getSchemaVO();
		if (schemaVO != null) {
			items.add(new SchemaBreadcrumbItem(schemaVO));

			RecordVO recordVO = getCurrentRecordVO();
			while (recordVO != null) {
				items.add(0, new SchemaRecordBreadcrumbItem(recordVO));
				String parentId = recordVO.get(HierarchicalValueListItem.PARENT);
				Record parentRecord = parentId != null ? recordServices.getDocumentById(parentId) : null;
				recordVO = parentRecord != null ?
						voBuilder.build(parentRecord, VIEW_MODE.DISPLAY, getView().getSessionContext()) :
						null;
			}
		}
		return items;
	}

	protected abstract MetadataSchemaVO getSchemaVO();

	protected abstract RecordVO getCurrentRecordVO();

	private class ListValueDomainsBreadcrumbItem extends IntermediateBreadCrumbTailItem {

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public String getTitle() {
			return $("ListValueDomainView.viewTitle");
		}

		@Override
		public void activate(Navigation navigate) {
			getView().navigate().to().listValueDomains();
		}
	}

	private class SchemaBreadcrumbItem extends IntermediateBreadCrumbTailItem {

		private MetadataSchemaVO schemaVO;

		private SchemaBreadcrumbItem(MetadataSchemaVO schemaVO) {
			this.schemaVO = schemaVO;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public String getTitle() {
			return schemaVO.getLabel();
		}

		@Override
		public void activate(Navigation navigate) {
			getView().navigate().to().listSchemaRecords(schemaVO.getCode());
		}

	}

	private class SchemaRecordBreadcrumbItem extends IntermediateBreadCrumbTailItem {

		private RecordVO recordVO;

		private SchemaRecordBreadcrumbItem(RecordVO recordVO) {
			this.recordVO = recordVO;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public String getTitle() {
			return recordVO.getTitle();
		}

		@Override
		public void activate(Navigation navigate) {
			getView().navigate().to().displaySchemaRecord(recordVO.getId());
		}

	}

}
