package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.SimpleDateFormatSingleton;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Marco on 2017-01-30.
 */
public class ReportField {

    private MetadataValueType types;
    private String label, schema, code;
    private AppLayerFactory factory;

    public ReportField(MetadataValueType type, String label, String schema, String code, AppLayerFactory factory) {
        this.types = type;
        this.label = label;
        this.schema = schema;
        this.code = code;
        this.factory = factory;
    }

    public MetadataValueType getTypes() {
        return this.types;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getLabel() {
        return this.label;
    }

    public String getCode() {
        return this.code;
    }

    public String formatData(String value) throws Exception {
        String formattedData = value;
        if (value != null) {
            ConstellioEIMConfigs configs = new ConstellioEIMConfigs(factory.getModelLayerFactory().getSystemConfigurationsManager());
            if (this.types.equals(MetadataValueType.BOOLEAN)) {
                formattedData = $(value);
            } else if (this.types.equals(MetadataValueType.DATE)) {
                if (value.matches("\\[([0-9\\-])+\\]")) {
                    value.replace("\\[\\]", "");
                }
                try {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = df.parse(value);
                    formattedData = SimpleDateFormatSingleton.getSimpleDateFormat(configs.getDateFormat()).format(date);
                } catch (ParseException e) {
                    //System.err.println("Unable to parse date : " + value);
                    return value;
                }

            } else if (this.types.equals(MetadataValueType.DATE_TIME)) {
                try {
                    if (value.matches("\\[([0-9\\-T:\\.])+\\]")) {
                        value.replace("\\[\\]", "");
                    }
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    Date date = df.parse(value);
                    formattedData = SimpleDateFormatSingleton.getSimpleDateFormat(configs.getDateTimeFormat()).format(date);
                } catch (ParseException e) {
                    //System.err.println("Unable to parse date : " + value);
                    return value;
                }
            } else {
                formattedData = value;
            }
            formattedData = formattedData.replaceAll("[\\[\\]]", "");
        }
        return formattedData;
    }

}