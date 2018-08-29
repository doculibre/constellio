package com.constellio.app.ui.util;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import static com.constellio.app.ui.i18n.i18n.$;

public class TaxonomyCaptionUtils implements Serializable {

	public static String getCaptionForTaxonomyCode(String collection, String taxonomyCode, Language language) {
		String caption;
		if (StringUtils.isNotBlank(taxonomyCode)) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
			Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
			String taxonomyTitle = taxonomy.getTitle(language);

			String captionPrefixKey = "Taxonomy." + taxonomyCode + ".caption";
			String captionPrefix = $(captionPrefixKey);
			if (captionPrefixKey.equals(captionPrefix)) {
				captionPrefixKey = "Taxonomy.default.caption";
				captionPrefix = $(captionPrefixKey);
			}
			//			if (isRightToLeft()) {
			//				if (captionPrefixKey.equals(captionPrefix)) {
			//					captionPrefix = "";
			//				} else {
			//					captionPrefix = " " + captionPrefix;
			//				}
			//				caption = taxonomyTitle + captionPrefix;
			//			} else {
			if (captionPrefixKey.equals(captionPrefix)) {
				captionPrefix = "";
			} else {
				captionPrefix = captionPrefix + " ";
			}
			caption = captionPrefix + taxonomyTitle;
			//			}
		} else {
			caption = "";
		}
		return caption;
	}

}
