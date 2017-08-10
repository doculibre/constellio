package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.model.intelligid;

import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPObject;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.model.entities.schemas.Metadata;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class IntelliGIDSIPFicheMetadonnees implements SIPObject {
	
	private RMObject ficheMetadonnees;
	
	private List<String> metadataIds = new ArrayList<String>();
	
	private Map<String, String> metadataLabelsMap = new LinkedHashMap<String, String>();
	
	private Map<String, List<String>> metadataValuesMap = new LinkedHashMap<String, List<String>>();
	
	public IntelliGIDSIPFicheMetadonnees(RMObject ficheMetadonnees, List<Metadata> metadonneesFiche, List<Metadata> metadonneesDossier) {
		this.ficheMetadonnees = ficheMetadonnees;
		if (ficheMetadonnees == null) {
			throw new NullPointerException("La fiche de métadonnées est nulle");
		}
		
		for (Metadata metadonnee : metadonneesFiche) {
			String metadataId = metadonnee.getCode();
			String metadataLabel = metadonnee.getFrenchLabel();
			try {
				String displayValue = ficheMetadonnees.get(metadonnee);
				List<String> metadataValues = StringUtils.isNotBlank(displayValue) ? Arrays.asList(displayValue) : new ArrayList<String>();
				metadataIds.add(metadataId);
				metadataLabelsMap.put(metadataId, metadataLabel);
				metadataValuesMap.put(metadataId, metadataValues);
			} catch (NullPointerException e) {
				System.out.println("Métadonnée " + metadataId + " nulle pour fiche " + ficheMetadonnees.getSchemaCode() + " " + ficheMetadonnees.getId());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends RMObject> T getFicheMetadonnees() {
		return (T) ficheMetadonnees;
	}

	@Override
	public String getId() {
		return "" + ficheMetadonnees.getId();
	}

	@Override
	public List<String> getMetadataIds() {
		return metadataIds;
	}

	@Override
	public String getMetadataLabel(String metadataId) {
		return metadataLabelsMap.get(metadataId);
	}

	@Override
	public String getMetadataValue(String metadataId) {
		List<String> metadataValues = getMetadataValues(metadataId);
		return !metadataValues.isEmpty() ? metadataValues.get(0) : null;
	}

	@Override
	public List<String> getMetadataValues(String metadataId) {
		return metadataValuesMap.get(metadataId);
	}

}
