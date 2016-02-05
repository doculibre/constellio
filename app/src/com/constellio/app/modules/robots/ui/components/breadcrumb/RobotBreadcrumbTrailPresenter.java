package com.constellio.app.modules.robots.ui.components.breadcrumb;

import java.io.Serializable;

import com.constellio.app.modules.robots.model.services.RobotsService;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbTrail;
import com.constellio.app.ui.util.SchemaCaptionUtils;

public class RobotBreadcrumbTrailPresenter implements Serializable {
	private final BreadcrumbTrail trail;
	private final String robotId;

	public RobotBreadcrumbTrailPresenter(BreadcrumbTrail trail, String robotId) {
		this.trail = trail;
		this.robotId = robotId;
	}

	public RobotBreadcrumbTrailPresenter createItems() {
		for (Robot robot : service().loadAncestors(robotId)) {
			String id = robot.getId();
			String caption = SchemaCaptionUtils.getCaptionForRecord(robot.getWrappedRecord());
			boolean current = robotId.equals(id);
			trail.addItem(new RobotBreadcrumbItem(id, caption, !current));
		}
		return this;
	}

	public void itemClicked(RobotBreadcrumbItem item) {
		trail.navigateTo().robotConfiguration(item.getId());
	}

	private RobotsService service() {
		return new RobotsService(
				trail.getSessionContext().getCurrentCollection(), trail.getConstellioFactories().getAppLayerFactory());
	}

	public static class RobotBreadcrumbItem implements BreadcrumbItem {
		private final String id;
		private final String label;
		private final boolean enabled;

		public RobotBreadcrumbItem(String id, String label, boolean enabled) {
			this.id = id;
			this.label = label;
			this.enabled = enabled;
		}

		public String getId() {
			return id;
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public boolean isEnabled() {
			return enabled;
		}
	}
}
