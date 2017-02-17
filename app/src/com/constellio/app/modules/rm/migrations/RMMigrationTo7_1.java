package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

public class RMMigrationTo7_1 extends MigrationHelper implements MigrationScript {

    private RecordServices rs;
    private Map<String, Integer> map = new HashMap<>();
    private AppLayerFactory factory;
    private String collection;
    private UserServices userServices;

    @Override
    public String getVersion() {
        return "7.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
            throws Exception {
        givenNewPermissionsToRGDandADMRoles(collection, appLayerFactory.getModelLayerFactory());
        SchemaAlterationsFor6_7 s = new SchemaAlterationsFor6_7(collection, provider, appLayerFactory);
        s.migrate();
        SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        SchemaTypesDisplayTransactionBuilder transaction = displayManager.newTransactionBuilderFor(collection);

        transaction.add(displayManager.getSchema(collection, PrintableLabel.DEFAULT_SCHEMA)
                .withNewTableMetadatas(PrintableLabel.DEFAULT_SCHEMA + "_" + PrintableLabel.TITLE)
                .withRemovedDisplayMetadatas(PrintableLabel.DEFAULT_SCHEMA + "_" + PrintableLabel.ISDELETABLE)
                .withRemovedFormMetadatas(PrintableLabel.DEFAULT_SCHEMA + "_" + PrintableLabel.ISDELETABLE)
        );
        displayManager.execute(transaction.build());
        createDefaultLabel(collection, appLayerFactory, provider);
    }

    private void givenNewPermissionsToRGDandADMRoles(String collection, ModelLayerFactory modelLayerFactory) {
        Role rgdRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
        List<String> newRgdPermissions = new ArrayList<>();
        newRgdPermissions.add(CorePermissions.MANAGE_LABELS);
        modelLayerFactory.getRolesManager().updateRole(rgdRole.withNewPermissions(newRgdPermissions));
    }

    public void createDefaultLabel(String collection, AppLayerFactory factory, MigrationResourcesProvider provider) throws Exception {
        map.put("5159", 7);
        map.put("5161", 10);
        map.put("5162", 7);
        map.put("5163", 5);
        ModelLayerFactory model = factory.getModelLayerFactory();
        rs = model.newRecordServices();
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, factory);
        MetadataSchemaType metaBuilder = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(Printable.SCHEMA_TYPE);
        MetadataSchema typeBuilder = metaBuilder.getSchema(PrintableLabel.SCHEMA_LABEL);
        ContentManager contentManager = model.getContentManager();
        userServices = model.newUserServices();
        Transaction trans = new Transaction();
        File f = provider.getFile("defaultJasperFiles");
        List<File> files = getFolders(f, provider);
        for (File fi : files) {
            Record record = rs.newRecordWithSchema(metaBuilder.getSchema(PrintableLabel.SCHEMA_LABEL));
            String type = fi.getName().matches("(.)+_(Container.jasper)") ? ContainerRecord.SCHEMA_TYPE : Folder.SCHEMA_TYPE;
            String titre = provider.getDefaultLanguageString("FichierJasper.title") + " ";
            Matcher m = Pattern.compile("(.)+_(\\d{4})_(.)+").matcher(fi.getName());
            m.find();
            String format = m.group(2);
            record.set(typeBuilder.getMetadata(PrintableLabel.TYPE_LABEL), type);
            record.set(typeBuilder.getMetadata(PrintableLabel.LIGNE), map.get(format));

            if (type.equals(Folder.SCHEMA_TYPE)) {
                titre += provider.getDefaultLanguageString("Migration.typeSchemaDossier") + " " + (fi.getName().contains("_D_") ?
                        provider.getDefaultLanguageString("Migration.typeAveryDroite") : provider.getDefaultLanguageString("Migration.typeAveryGauche"));
            } else {
                titre += provider.getDefaultLanguageString("Migration.typeSchemaConteneur");
            }
            String etiquetteName = provider.getDefaultLanguageString("Migration.etiquetteName");
            String extension = provider.getDefaultLanguageString("Migration.fileExtension");
            titre += " (" + etiquetteName + " " + format + ")";
            record.set(typeBuilder.getMetadata(PrintableLabel.COLONNE), 2);
            record.set(typeBuilder.getMetadata(Printable.ISDELETABLE), false);
            ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(fi), etiquetteName + " " + format + " " + type);
            record.set(typeBuilder.getMetadata(Report.TITLE), titre);
            record.set(typeBuilder.getMetadata(Printable.JASPERFILE), contentManager.createFileSystem(etiquetteName + "-" + format + "-" + type + extension, upload));
            trans.add(record);
        }
        rs.execute(trans);
    }

    public List<File> getFolders(File file, MigrationResourcesProvider provider) {
        List<File> temp = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>(asList(file.listFiles()));
        String extension = provider.getDefaultLanguageString("Migration.fileExtension");
        for (File f : files) {
            if (f.isDirectory()) {
                temp.addAll(getFolders(f, provider));
            } else if (f.getName().endsWith(extension)) {
                temp.add(f);
            }
        }
        return temp;
    }


    public static class SchemaAlterationsFor6_7 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationsFor6_7(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
            super(collection, provider, factory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder builder = typesBuilder.getSchemaType(Printable.SCHEMA_TYPE).createCustomSchema(PrintableLabel.SCHEMA_LABEL);
            builder.create(PrintableLabel.TYPE_LABEL).setType(STRING).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
            builder.create(PrintableLabel.LIGNE).setType(MetadataValueType.NUMBER).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
            builder.create(PrintableLabel.COLONNE).setType(MetadataValueType.NUMBER).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
        }
    }
}