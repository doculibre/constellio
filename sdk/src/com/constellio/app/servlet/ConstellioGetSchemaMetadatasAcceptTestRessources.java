package com.constellio.app.servlet;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;

import static java.util.Arrays.asList;
import static org.assertj.core.groups.Tuple.tuple;

public class ConstellioGetSchemaMetadatasAcceptTestRessources {

    static Tuple[] expectedFolderDefaultSchemaMetadatas() {
        return asList(
                tuple("language", "fr"), Assertions.tuple("code", "folder_default"),
                tuple("collection", "zeCollection"), Assertions.tuple("search-field", "search_txt_fr"))
                .toArray(new Tuple[] {});
    }

    static Tuple[] expectedDocumentDefaultSchemaMetadatas() {
        return asList(
                tuple("language", "fr"), tuple("code", "document_default"),
                tuple("collection", "zeCollection"), tuple("search-field", "search_txt_fr"))
                .toArray(new Tuple[] {});
    }

    static Tuple[] expectedDocumentEmailSchemaMetadatas() {
        return asList(
                tuple("language", "fr"), tuple("code", "document_email"),
                tuple("collection", "zeCollection"), tuple("search-field", "search_txt_fr"))
                .toArray(new Tuple[] {});
    }

    static Tuple[] expectedDdvDocumentMetadatas() {
        return asList(
                tuple("language", "fr"), tuple("code", "ddvDocumentType_default"),
                tuple("collection", "zeCollection"), tuple("search-field", "search_txt_fr"))
                .toArray(new Tuple[] {});
    }

    static Tuple[] expectedDdvContainerMetadatas() {
        return asList(
                tuple("language", "fr"), tuple("code", "ddvContainerRecordType_default"),
                tuple("collection", "zeCollection"), tuple("search-field", "search_txt_fr"))
                .toArray(new Tuple[] {});
    }

    static Tuple[] expectedKeywordMetadatas() {
        return asList(
                tuple("code", "keywords"), Assertions.tuple("title", "Mots-clés"), Assertions.tuple("type", "STRING"),
                tuple("multivalue", "true"), Assertions.tuple("solr-field", "keywords_ss"),
                tuple("solr-analyzed-field", "keywords_txt_fr"))
                .toArray(new Tuple[] {});
    }

    static Tuple[] expectedTypeMetadatas() {
        return asList(
                Assertions.tuple("code", "type"), Assertions.tuple("title", "Type"), Assertions.tuple("type", "REFERENCE"),
                Assertions.tuple("multivalue", "false"), Assertions.tuple("solr-field", "typeId_s"))
                .toArray(new Tuple[] {});
    }

    static Tuple[] expectedAllauthorizationsMetadatas() {
        return asList(
                tuple("code", "allauthorizations"), tuple("title", "Toutes les autorisations"), tuple("type", "STRING"),
                tuple("multivalue", "true"), tuple("solr-field", "allauthorizations_ss"))
                .toArray(new Tuple[] {});
    }
}