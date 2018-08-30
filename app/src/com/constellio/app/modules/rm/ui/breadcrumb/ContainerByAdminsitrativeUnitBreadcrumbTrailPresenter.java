package com.constellio.app.modules.rm.ui.breadcrumb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail.CurrentViewItem;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class ContainerByAdminsitrativeUnitBreadcrumbTrailPresenter implements Serializable {

	private String recordId;

	private String taxonomyCode;

	private String collection;


	private ContainerByAdministrativeUnitBreadcrumbTrail breadcrumbTrail;

	private transient TaxonomiesManager taxonomiesManager;

	private transient SchemaPresenterUtils folderPresenterUtils;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	private String tabName;

	private String fromAdministrativeUnit;

	public ContainerByAdminsitrativeUnitBreadcrumbTrailPresenter(String recordId, String fromAdministrativeUnit,
												  ContainerByAdministrativeUnitBreadcrumbTrail breadcrumbTrail, String tabName) {
		this.recordId = recordId;
		this.breadcrumbTrail = breadcrumbTrail;
		this.tabName = tabName;
		this.fromAdministrativeUnit = fromAdministrativeUnit;
		initTransientObjects();
		addBreadcrumbItems();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = breadcrumbTrail.getConstellioFactories();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		SessionContext sessionContext = breadcrumbTrail.getSessionContext();
		collection = sessionContext.getCurrentCollection();

		folderPresenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, breadcrumbTrail);
	}

	private void addBreadcrumbItems() {
		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();

		Record record = rmSchemasRecordsServices.get(recordId);
		String parentOrRoot = recordId;
		boolean first = true;

		if(record.getSchemaCode().startsWith(ContainerRecord.SCHEMA_TYPE)) {
			ContainerRecord containerRecord = rmSchemasRecordsServices.wrapContainerRecord(record);
			breadcrumbItems.add(new RecordCurrentViewItem(containerRecord.getId()));

			parentOrRoot = fromAdministrativeUnit;
			first = false;
		}

		AdministrativeUnit currentAdministrativeUnit = rmSchemasRecordsServices.getAdministrativeUnit(parentOrRoot);

		while (currentAdministrativeUnit != null) {

			BreadcrumbItem administrativeUnitBreadcrumbItem;

			if(!first) {
				administrativeUnitBreadcrumbItem = new AdministrativeUnitBreadcrumbItem(
						currentAdministrativeUnit.getId(), tabName) {
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			} else {
				final String administrativeUnitId = currentAdministrativeUnit.getId();
				administrativeUnitBreadcrumbItem = new RecordCurrentViewItem(administrativeUnitId);
				first = false;
			}
			breadcrumbItems.add(0, administrativeUnitBreadcrumbItem);

			final String parentId = currentAdministrativeUnit.getParent();

			if(parentId != null) {
				currentAdministrativeUnit = rmSchemasRecordsServices.getAdministrativeUnit(parentId);
			} else {
				currentAdministrativeUnit = null;
			}
		}

		for (BreadcrumbItem breadcrumbItem : breadcrumbItems) {
			breadcrumbTrail.addItem(breadcrumbItem);
		}
	}

	public boolean itemClicked(BreadcrumbItem item) {
		boolean handled;
		if (item instanceof AdministrativeUnitBreadcrumbItem) {
			handled = true;
			AdministrativeUnitBreadcrumbItem administrativeUnitBreadcrumbItem = (AdministrativeUnitBreadcrumbItem) item;

			breadcrumbTrail.navigate().to(RMViews.class).displayAdminUnitWithContainers(administrativeUnitBreadcrumbItem.getTabName(),
					administrativeUnitBreadcrumbItem.getId());
		}  else {
			handled = false;
		}
		return handled;
	}

	class AdministrativeUnitBreadcrumbItem implements BreadcrumbItem {

		private String id;
		private String tabName;

		AdministrativeUnitBreadcrumbItem(String id, String tabName) {
			this.id = id;
			this.tabName = tabName;
		}


		public String getTabName() {
			return tabName;
		}

		public String getId() {
			return id;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(id, breadcrumbTrail.getSessionContext().getCurrentLocale());
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

	}

	class RecordCurrentViewItem extends CurrentViewItem {

		private String recordId;

		public RecordCurrentViewItem(String recordId) {
			super(null);
			this.recordId = recordId;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(recordId, breadcrumbTrail.getSessionContext().getCurrentLocale());
		}

		public String getRecordId() {
			return recordId;
		}
	}
}
