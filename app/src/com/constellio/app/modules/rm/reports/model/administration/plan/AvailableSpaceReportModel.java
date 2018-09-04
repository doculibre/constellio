package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.data.io.streamFactories.StreamFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Charles Blanchette on 2017-02-20.
 */
public class AvailableSpaceReportModel {

	public static final String UNIT = "cm";

	private Boolean showFullSpaces;

	private StreamFactory<InputStream> headerLogo;

	private List<AvailableSpaceReportModelNode> rootNodes = new ArrayList<>();

	List<AvailableSpaceReportModelNode> nodes = new ArrayList<AvailableSpaceReportModelNode>();

	public StreamFactory<InputStream> getHeaderLogo() {
		return headerLogo;
	}

	public AvailableSpaceReportModel setHeaderLogo(StreamFactory<InputStream> headerLogo) {
		this.headerLogo = headerLogo;
		return this;
	}

	public List<AvailableSpaceReportModelNode> getRootNodes() {
		return rootNodes;
	}

	public AvailableSpaceReportModel setRootNodes(List<AvailableSpaceReportModelNode> rootNodes) {
		this.rootNodes = rootNodes;
		return this;
	}

	public boolean isShowFullSpaces() {
		return showFullSpaces;
	}

	public void setShowFullSpaces(boolean showFullSpaces) {
		this.showFullSpaces = showFullSpaces;
	}

	public String getTitle() {
		return this.isShowFullSpaces() ? $("AvailableSpaceReport.All") : $("AvailableSpaceReport.Title");
	}

	public static class AvailableSpaceReportModelNode {

		private String code;

		private String title;

		private double availableSpace;

		private double capacity;

		private String image;

		private List<AvailableSpaceReportModelNode> childrenNodes = new ArrayList<>();

		public String getCode() {
			return code;
		}

		public AvailableSpaceReportModelNode setCode(String code) {
			this.code = code;
			return this;
		}

		public String getTitle() {
			return title;
		}

		public AvailableSpaceReportModelNode setTitle(String title) {
			this.title = title;
			return this;
		}

		public List<AvailableSpaceReportModelNode> getChildrenNodes() {
			return childrenNodes;
		}

		public AvailableSpaceReportModelNode setChildrenNodes(List<AvailableSpaceReportModelNode> childrenNodes) {
			this.childrenNodes = childrenNodes;
			return this;
		}

		public double getAvailableSpace() {
			return availableSpace;
		}

		public AvailableSpaceReportModelNode setAvailableSpace(double availableSpace) {
			this.availableSpace = availableSpace;
			return this;
		}

		public double getCapacity() {
			return capacity;
		}

		public AvailableSpaceReportModelNode setCapacity(double capacity) {
			this.capacity = capacity;
			return this;
		}

		public String getImage() {
			return image;
		}

		public AvailableSpaceReportModelNode setImage(String image) {
			this.image = image;
			return this;
		}
	}
}
