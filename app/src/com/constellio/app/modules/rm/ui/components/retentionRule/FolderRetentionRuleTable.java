package com.constellio.app.modules.rm.ui.components.retentionRule;

import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class FolderRetentionRuleTable extends CustomField<List<CopyRetentionRule>> {
	private Table table;

	@Override
	protected Component initContent() {
		BeanItemContainer<CopyRetentionRule> container = new BeanItemContainer<>(CopyRetentionRule.class, getInternalValue());
		table = new Table(null, container);
		table.setPageLength(Math.min(15, container.size()));
		table.setWidth("100%");
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				CopyRetentionRule rule = (CopyRetentionRule) event.getItemId();
				Window window = new BaseWindow("abc");
				window.setContent(new Label(rule.toString()));
				UI.getCurrent().addWindow(window);
			}
		});
		return table;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getType() {
		return List.class;
	}
}
