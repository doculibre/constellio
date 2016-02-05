package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.pages.management.extractors.builders.RegexConfigToVOBuilder;
import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.RegexConfig;

public class MetadataExtractorVO implements Serializable {

	private MetadataVO metadataVO;

	private RegexConfigToVOBuilder regexConfigToVOBuilder = new RegexConfigToVOBuilder();

	private List<String> styles = new ArrayList<>();

	private List<String> properties = new ArrayList<>();

	private List<RegexConfigVO> regexes = new ArrayList<>();

	public MetadataExtractorVO(MetadataVO metadataVO, MetadataPopulateConfigs metadataPopulateConfigs) {
		this.metadataVO = metadataVO;
		this.styles.addAll(metadataPopulateConfigs.getStyles());
		this.properties.addAll(metadataPopulateConfigs.getProperties());
		for (RegexConfig regexConfig : metadataPopulateConfigs.getRegexes()) {
			this.regexes.add(regexConfigToVOBuilder.build(regexConfig));
		}
	}

	public final MetadataVO getMetadataVO() {
		return metadataVO;
	}

	public final void setMetadataVO(MetadataVO metadataVO) {
		this.metadataVO = metadataVO;
	}

	public final List<String> getStyles() {
		return styles;
	}

	public final void setStyles(List<String> styles) {
		this.styles = styles;
	}

	public final List<String> getProperties() {
		return properties;
	}

	public final void setProperties(List<String> properties) {
		this.properties = properties;
	}

	public final List<RegexConfigVO> getRegexes() {
		return regexes;
	}

	public final void setRegexes(List<RegexConfigVO> regexes) {
		this.regexes = regexes;
	}

}
