package com.constellio.app.entities.schemasDisplay.enums;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

public enum MetadataDisplayType {

    HORIZONTAL,
    VERTICAL;

    public static String getCaptionFor(MetadataDisplayType type) {
        String caption = "";

        switch (type) {
            case HORIZONTAL:
                caption = "MetadataDisplayType.horizontal";
                break;
            case VERTICAL:
                caption = "MetadataDisplayType.vertical";
                break;
        }

        return caption;
    }

    public static List<MetadataDisplayType> getAvailableMetadataDisplayTypesFor(MetadataValueType type, MetadataInputType input) {
        List<MetadataDisplayType> displayTypes = new ArrayList<>();


        if(type != null && type.equals(MetadataValueType.REFERENCE) &&
                input != null && (input.equals(MetadataInputType.RADIO_BUTTONS) || input.equals(MetadataInputType.CHECKBOXES))) {

            displayTypes.add(HORIZONTAL);
        }

        displayTypes.add(VERTICAL);

        return displayTypes;
    }
}