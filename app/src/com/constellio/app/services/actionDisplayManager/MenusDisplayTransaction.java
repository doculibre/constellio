package com.constellio.app.services.actionDisplayManager;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenusDisplayTransaction {
	public enum Action {
		ADD_UPDATE,
		REMOVE
	}

	private List<TransactionElement> transactionElements;

	@Getter
	static class TransactionElement {
		public TransactionElement(String schemaType, Action action, MenuDisplayItem menuDisplayItem,
								  MenuPositionActionOptions menuPositionActionOptions) {
			if (StringUtils.isBlank(schemaType) || menuDisplayItem == null || action == null) {
				throw new IllegalArgumentException("schemaType, menuDisplayItems and action parameters cannot be null");
			}

			if (action == Action.ADD_UPDATE && menuPositionActionOptions == null) {
				throw new IllegalArgumentException("menuPositionActionOptions cannot be null with action addUpdate");
			}

			this.schemaType = schemaType;
			this.action = action;
			this.menuDisplayItem = menuDisplayItem;
			this.menuPositionActionOptions = menuPositionActionOptions;
		}

		String schemaType;
		Action action;
		MenuDisplayItem menuDisplayItem;
		MenuPositionActionOptions menuPositionActionOptions;
	}

	public MenusDisplayTransaction() {
		transactionElements = new ArrayList<>();
	}

	public MenusDisplayTransaction addElement(Action action, String schemaType, MenuDisplayItem actionsDisplay,
											  MenuPositionActionOptions menuPositionActionOptions) {
		transactionElements.add(new TransactionElement(schemaType, action, actionsDisplay, menuPositionActionOptions));
		return this;
	}

	public List<TransactionElement> getTransactionElements() {
		return Collections.unmodifiableList(transactionElements);
	}
}
