package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class ListBorrowingsFolderTab extends ListBorrowingsTab {
	public ListBorrowingsFolderTab(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		super(appLayerFactory, sessionContext);
	}

	@Override
	protected MetadataSchemaType getSchemaType() {
		return recordsServices.folderSchemaType();
	}

	@Override
	protected LogicalSearchCondition getCheckedOutCondition() {
		return where(recordsServices.folder.borrowed()).isEqualTo(true);
	}

	@Override
	protected LogicalSearchCondition getAdministrativeUnitCondition(String administrativeUnit) {
		return where(recordsServices.folder.administrativeUnit()).isEqualTo(administrativeUnit);
	}

	@Override
	protected LogicalSearchCondition getOverdueCondition() {
		return where(recordsServices.folder.borrowPreviewReturnDate()).isLessThan(LocalDate.now());
	}

	@Override
	protected SelectionTable buildViewableRecordItemTable(RecordVODataProvider dataProvider) {
		return new FolderViewableRecordItemTable(dataProvider);
	}

	private class FolderViewableRecordItemTable extends ViewableRecordItemTable {
		private FolderViewableRecordItemTable(RecordVODataProvider dataProvider) {
			super(dataProvider);
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			RecordVO recordVO = ((RecordVOItem) source.getItem(itemId)).getRecord();

			switch ((String) columnId) {
				case BORROWING_USER:
					return new ReferenceDisplay(getBorrowingUserId(recordVO));
				case BORROWING_DATE:
					String convertedJodaDate = jodaDateTimeConverter
							.convertToPresentation(getBorrowingDate(recordVO), String.class, sessionContext.getCurrentLocale());
					return new Label(convertedJodaDate);
				case BORROWING_DUE_DATE:
					convertedJodaDate = jodaDateConverter
							.convertToPresentation(getBorrowingDueDate(recordVO), String.class, sessionContext.getCurrentLocale());
					return new Label(convertedJodaDate);
			}

			return null;
		}

		@Override
		protected boolean isOverdue(RecordVO recordVO) {
			return LocalDate.now().isAfter(getBorrowingDueDate(recordVO));
		}

		private String getBorrowingUserId(RecordVO recordVO) {
			return recordVO.get(recordsServices.folder.borrowUser());
		}

		private LocalDateTime getBorrowingDate(RecordVO recordVO) {
			return recordVO.get(recordsServices.folder.borrowDate());
		}

		private LocalDate getBorrowingDueDate(RecordVO recordVO) {
			return recordVO.get(recordsServices.folder.borrowPreviewReturnDate());
		}
	}
}
