package com.constellio.app.modules.rm.ui.pages.shareManagement;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.containers.RecordVOContainer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ShareContentSelectionManager implements SelectionManager {
	private Set<Object> selectedItemIds = new HashSet<>();
	// Optimisation parce que sinon sa prend beaucoup de temps quand on a beaucoup d'élément de sélectionner.
	private Set<String> selectedItemRecordId = new HashSet<>();
	private Set<Object> deselectedItemIds = new HashSet<>();
	private boolean allItemsSelected;
	private RecordVOContainer recordVOContainer;

	public ShareContentSelectionManager(RecordVOContainer recordVOContainer) {
		this.recordVOContainer = recordVOContainer;
	}

	@Override
	public void selectionChanged(
			com.constellio.app.ui.framework.components.table.BaseTable.SelectionChangeEvent event) {
		if (event.getSelectedItemIds() != null) {
			List<Object> selectedItemIds = event.getSelectedItemIds();
			for (Object selectedItemId : selectedItemIds) {
				this.selectedItemIds.add(selectedItemId);
				deselectedItemIds.remove(selectedItemId);
				selectedItemRecordId.add(getRecordVO(selectedItemId).getId());
			}
		} else if (event.getDeselectedItemIds() != null) {
			List<Object> deselectedItemIds = event.getDeselectedItemIds();

			if (!deselectedItemIds.isEmpty()) {
				allItemsSelected = false;
			}

			for (Object deselectedItemId : deselectedItemIds) {
				this.selectedItemIds.remove(deselectedItemId);
				selectedItemRecordId.remove(getRecordVO(deselectedItemId).getId());
				this.deselectedItemIds.add(deselectedItemId);
			}
		} else if (event.isAllItemsSelected()) {

			List lRecordIdList = recordVOContainer.getItemIds().stream().map(itemId -> getRecordVO(itemId).getId()).collect(Collectors.toList());

			this.allItemsSelected = true;
			this.selectedItemIds.addAll(recordVOContainer.getItemIds());
			this.selectedItemRecordId.addAll(lRecordIdList);
			this.deselectedItemIds.clear();
		} else if (event.isAllItemsDeselected()) {
			this.allItemsSelected = false;
			selectedItemIds.clear();
			deselectedItemIds.clear();
		}
	}

	private RecordVO getRecordVO(Object selectedItemId) {
		return recordVOContainer.getRecordVO(selectedItemId);
	}

	@Override
	public List<Object> getAllSelectedItemIds() {
		List<Object> allSelectedItemIds;
		if (isAllItemsSelected()) {
			allSelectedItemIds = new ArrayList<>(recordVOContainer.getItemIds());
		} else {
			allSelectedItemIds = new ArrayList<>(selectedItemIds);
		}
		return allSelectedItemIds;
	}

	@Override
	public boolean isAllItemsSelected() {
		return allItemsSelected;
	}

	@Override
	public boolean isAllItemsDeselected() {
		return !allItemsSelected && selectedItemIds.isEmpty();
	}

	@Override
	public boolean isSelected(Object itemId) {
		RecordVO recordVO = getRecordVO(itemId);
		String recordId = recordVO.getId();
		return allItemsSelected || selectedItemRecordId.contains(recordId);
	}
}
