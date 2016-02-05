package com.constellio.app.modules.robots.reports;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.constellio.app.modules.robots.model.DryRunRobotAction;
import com.constellio.app.utils.RecordMetadataValuePrinter;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DryRunReportPresenter {
	private final List<DryRunRobotAction> dryRunRobotActions;
	private RecordMetadataValuePrinter recordMetadataValuePrinter;

	public DryRunReportPresenter(ModelLayerFactory modelLayerFactory, List<DryRunRobotAction> dryRunRobotActions) {
		this.dryRunRobotActions = dryRunRobotActions;
		recordMetadataValuePrinter = new RecordMetadataValuePrinter(modelLayerFactory);
	}

	public DryRunReportModel buildModel() {
		DryRunReportModel model = new DryRunReportModel();

		model.addTitle($("DryRunReport.RecordId"));
		model.addTitle($("DryRunReport.RecordUrl"));
		model.addTitle($("DryRunReport.RecordTitle"));
		model.addTitle($("DryRunReport.RobotId"));
		model.addTitle($("DryRunReport.RobotCode"));
		model.addTitle($("DryRunReport.RobotTitle"));
		model.addTitle($("DryRunReport.RobotHierarchy"));
		model.addTitle($("DryRunReport.ActionTitle"));

		Map<Metadata, Object> treeMap = new TreeMap<>(
				new Comparator<Metadata>() {
					@Override
					public int compare(Metadata o1, Metadata o2) {
						return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
					}

				});
		for (DryRunRobotAction dryRunRobotAction : dryRunRobotActions) {
			treeMap.putAll(dryRunRobotAction.getActionParameters());
		}

		List<String> actionLabels = new ArrayList<>();
		for (Metadata metadata : treeMap.keySet()) {
			if (!actionLabels.contains(metadata.getLabel())) {
				model.addTitle(metadata.getLabel());
				actionLabels.add(metadata.getLabel());
			}
		}

		for (DryRunRobotAction dryRunRobotAction : dryRunRobotActions) {
			List<String> results = new ArrayList<>();
			results.add(dryRunRobotAction.getRecordId());
			results.add(dryRunRobotAction.getRecordUrl());
			results.add(dryRunRobotAction.getRecordTitle());
			results.add(dryRunRobotAction.getRobotId());
			results.add(dryRunRobotAction.getRobotCode());
			results.add(dryRunRobotAction.getRobotTitle());
			results.add(dryRunRobotAction.getRobotHierarchy());
			results.add($("robot.action." + dryRunRobotAction.getActionTitle()));
			for (String actionLabel : actionLabels) {
				boolean added = false;
				for (Entry<Metadata, Object> entry : dryRunRobotAction.getActionParameters().entrySet()) {
					if (actionLabel.equals(entry.getKey().getLabel())) {
						results.add(recordMetadataValuePrinter.convertForPrinting(entry.getKey(), entry.getValue()));
						added = true;
					}
				}
				if (!added) {
					results.add("");
				}
			}
			model.addLine(results);
		}
		return model;
	}
}