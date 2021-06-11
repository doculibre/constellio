package com.constellio.app.modules.tasks.extensions.ui;

import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table.ColumnGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TaskTableExtension {

	public void addExtraColumns(TaskTableColumnsExtensionParams params) {

	}

	public void addExtraComponents(TaskTableComponentsExtensionParams params) {

	}

	@Getter
	@AllArgsConstructor
	public static class TaskTableColumnsExtensionParams {
		BiConsumer<Object, ColumnGenerator> generatedColumnAdder;
		BiConsumer<Object, String> columnHeaderSetter;
		BiConsumer<Object, Integer> columnWidthSetter;
	}

	@Getter
	@AllArgsConstructor
	public static class TaskTableComponentsExtensionParams {
		Consumer<Component> componentAdder;
		RecordVO taskVO;
	}
}