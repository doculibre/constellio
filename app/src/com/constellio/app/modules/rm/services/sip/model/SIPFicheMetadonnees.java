package com.constellio.app.modules.rm.services.sip.model;

import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class SIPFicheMetadonnees {

	public static final String FOLDER_TYPE = "DOSSIER";
	public static final String DOCUMENT_TYPE = "DOCUMENT";
	public static final String CATEGORY_TYPE = "RUBRIQUE";

	private MetadataSchemasManager indexHelper = ConstellioFactories.getInstance().getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
	
	private RMObject rmObject;
	
	private List<String> metadataIds = new ArrayList<String>();
	
	private Map<String, String> metadataLabelsMap = new LinkedHashMap<>();
	
	private Map<String, List<String>> metadataValuesMap = new LinkedHashMap<>();
	
	public SIPFicheMetadonnees(RMObject rmObject, List<Metadata> metadatasOfSchema) {
		this.rmObject = rmObject;
		if (rmObject == null) {
			throw new NullPointerException("metadataSchema is null");
		}
		
		for (Metadata metadata : metadatasOfSchema) {
			String metadataId = metadata.getCode();
			String metadataLabel = metadata.getLabel(Language.French);
			Object metadataValue = rmObject.getWrappedRecord().get(metadata);
			if(metadataValue != null){
				String displayValue = rmObject.getWrappedRecord().get(metadata).toString();
				List<String> metadataValues = StringUtils.isNotBlank(displayValue) ? Arrays.asList(displayValue) : new ArrayList<String>();
				metadataIds.add(metadataId);
				metadataLabelsMap.put(metadataId, metadataLabel);
				metadataValuesMap.put(metadataId, metadataValues);
			} else {
				System.out.println("Métadonnée " + metadataId + " nulle pour fiche " + rmObject.getSchemaCode() + " " + rmObject.getId());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends RMObject> T getFicheMetadonnees() {
		return (T) rmObject;
	}

	public String getId() {
		return "" + rmObject.getId();
	}

	public List<String> getMetadataIds() {
		return metadataIds;
	}

	public String getMetadataLabel(String metadataId) {
		return metadataLabelsMap.get(metadataId);
	}

	public String getMetadataValue(String metadataId) {
		List<String> metadataValues = getMetadataValues(metadataId);
		return !metadataValues.isEmpty() ? metadataValues.get(0) : null;
	}

	public List<String> getMetadataValues(String metadataId) {
		return metadataValuesMap.get(metadataId);
	}

}
