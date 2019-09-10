package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.ui.components.Dimensionnable;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Classe RM pour les Rapports.
 *
 * @author Nicolas D'Amours & Charles Blanchette.
 */
public class PrintableLabel extends Printable implements Dimensionnable {

	public PrintableLabel(Record record, MetadataSchemaTypes type) {
		super(record, type);
	}

	public static final String SCHEMA_LABEL = "label";
	public static final String TYPE_LABEL = "typelabel";
	public static final String COLONNE = "colonne";
	public static final String LIGNE = "ligne";
	public static final String SCHEMA_NAME = Printable.SCHEMA_TYPE + "_" + SCHEMA_LABEL;

	public static String getSchemaLabel() {
		return SCHEMA_LABEL;
	}

	public String getTypeLabel() {
		return get(TYPE_LABEL);
	}

	public int getColonne() {
		return ((Double) get(COLONNE)).intValue();
	}

	public int getLigne() {
		return ((Double) get(LIGNE)).intValue();
	}

	public PrintableLabel setLigne(int ligne) {
		set((LIGNE), ligne);
		return this;
	}

	public PrintableLabel setColonne(int colonne) {
		set(COLONNE, colonne);
		return this;
	}

	@Override
	public int getDimension() {
		return this.getColonne() * this.getLigne();
	}
}
