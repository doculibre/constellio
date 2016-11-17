package com.constellio.app.entities.schemasDisplay.enums;

import java.util.ArrayList;
import java.util.List;

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

    public static List<MetadataDisplayType> getAvailableMetadataDisplayTypesFor(MetadataValueType type) {
        List<MetadataDisplayType> displayTypes = new ArrayList<>();

        switch (type) {
            case REFERENCE:
                displayTypes.add(VERTICAL);
                displayTypes.add(HORIZONTAL);
                break;
            default:
                displayTypes.add(VERTICAL);
        }

//        switch (type) {
//            case RADIO_BUTTONS:
//            case CHECKBOXES:
//                displayTypes.add(VERTICAL);
//                displayTypes.add(HORIZONTAL);
//                break;
//            default:
//                displayTypes.add(VERTICAL);
//                break;
//        }

        return displayTypes;
    }
}