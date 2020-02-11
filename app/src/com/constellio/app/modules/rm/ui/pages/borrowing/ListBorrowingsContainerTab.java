package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import org.joda.time.LocalDate;

import java.util.Collections;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class ListBorrowingsContainerTab extends ListBorrowingsTab {
	public ListBorrowingsContainerTab(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		super(appLayerFactory, sessionContext);
	}

	@Override
	protected MetadataSchemaType getSchemaType() {
		return recordsServices.containerRecordSchemaType();
	}

	@Override
	protected LogicalSearchCondition getCheckedOutCondition() {
		return where(recordsServices.containerRecord.borrowed()).isEqualTo(true);
	}

	@Override
	protected LogicalSearchCondition getAdministrativeUnitCondition(String administrativeUnit) {
		return where(recordsServices.containerRecord.administrativeUnits()).isContaining(Collections.singletonList(administrativeUnit));
	}

	@Override
	protected LogicalSearchCondition getOverdueCondition() {
		return where(recordsServices.containerRecord.planifiedReturnDate()).isLessThan(LocalDate.now());
	}

	@Override
	protected SelectionTable buildViewableRecordItemTable(RecordVODataProvider dataProvider) {
		return new ContainerViewableRecordItemTable(dataProvider);
	}

	private class ContainerViewableRecordItemTable extends ViewableRecordItemTable {
		private ContainerViewableRecordItemTable(RecordVODataProvider dataProvider) {
			super(dataProvider);
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			RecordVO recordVO = ((RecordVOItem) source.getItem(itemId)).getRecord();

			switch ((String) columnId) {
				case BORROWING_DATE:
					String convertedJodaDate = jodaDateConverter
							.convertToPresentation(getBorrowingDate(recordVO), String.class, sessionContext.getCurrentLocale());
					return new Label(convertedJodaDate);
				case BORROWING_DUE_DATE:
					convertedJodaDate = jodaDateConverter
							.convertToPresentation(getBorrowingDueDate(recordVO), String.class, sessionContext.getCurrentLocale());
					return new Label(convertedJodaDate);
			}

			return super.generateCell(source, itemId, columnId);
		}

		@Override
		protected boolean isOverdue(RecordVO recordVO) {
			return LocalDate.now().isAfter(getBorrowingDueDate(recordVO));
		}

		@Override
		protected String getBorrowingUserId(RecordVO recordVO) {
			return recordVO.get(recordsServices.containerRecord.borrower());
		}

		private LocalDate getBorrowingDate(RecordVO recordVO) {
			return recordVO.get(recordsServices.containerRecord.borrowDate());
		}

		private LocalDate getBorrowingDueDate(RecordVO recordVO) {
			return recordVO.get(recordsServices.containerRecord.planifiedReturnDate());
		}
	}
}
