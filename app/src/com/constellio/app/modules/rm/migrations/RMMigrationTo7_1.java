package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
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

    public static final String MANAGE_LABELS_PERMISSION = "manageLabels";

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
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
            throws Exception {
        SchemaAlterationsFor6_7 s = new SchemaAlterationsFor6_7(collection, provider, factory);
        s.migrate();
        s.setupRoles(collection, factory.getModelLayerFactory().getRolesManager(), provider);
        SchemasDisplayManager displayManager = factory.getMetadataSchemasDisplayManager();
        SchemaTypesDisplayTransactionBuilder transaction = displayManager.newTransactionBuilderFor(collection);

        transaction.add(displayManager.getSchema(collection, PrintableLabel.DEFAULT_SCHEMA)
                .withNewTableMetadatas(PrintableLabel.DEFAULT_SCHEMA + "_" + PrintableLabel.TITLE)
                .withRemovedDisplayMetadatas(PrintableLabel.DEFAULT_SCHEMA + "_" + PrintableLabel.ISDELETABLE)
                .withRemovedFormMetadatas(PrintableLabel.DEFAULT_SCHEMA + "_" + PrintableLabel.ISDELETABLE)
        );
        displayManager.execute(transaction.build());
        createDefaultLabel(collection, factory, provider);
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
        List<File> files = getFolders(f);
        for (File fi : files) {
            Record record = rs.newRecordWithSchema(metaBuilder.getSchema(PrintableLabel.SCHEMA_LABEL));
            String type = fi.getName().matches("(.)+_(Container.jasper)") ? ContainerRecord.SCHEMA_TYPE : Folder.SCHEMA_TYPE;
            String titre = "Code de plan justifié ";
            Matcher m = Pattern.compile("(.)+_(\\d{4})_(.)+").matcher(fi.getName());
            m.find();
            String format = m.group(2);
            record.set(typeBuilder.getMetadata(PrintableLabel.TYPE_LABEL), type);
            record.set(typeBuilder.getMetadata(PrintableLabel.LIGNE), map.get(format));

            if (type.equals(Folder.SCHEMA_TYPE)) {
                titre += "de Dossier" + (fi.getName().contains("_D_") ? " à Droite" : " à Gauche");
            } else {
                titre += "de Conteneur";
            }
            titre += " (Avery " + format + ")";
            record.set(typeBuilder.getMetadata(PrintableLabel.COLONNE), 2);
            record.set(typeBuilder.getMetadata(Printable.ISDELETABLE), false);
            ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(fi), "Avery " + format + " " + type);
            record.set(typeBuilder.getMetadata(Report.TITLE), titre);
            record.set(typeBuilder.getMetadata(Printable.JASPERFILE), contentManager.createFileSystem("Avery-" + format + "-" + type + ".jasper", upload));
            trans.add(record);
        }
        rs.execute(trans);
    }

    public List<File> getFolders(File file) {
        List<File> temp = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>(asList(file.listFiles()));
        for (File f : files) {
            if (f.isDirectory()) {
                temp.addAll(getFolders(f));
            } else if (f.getName().endsWith(".jasper")) {
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

        private void setupRoles(String collection, RolesManager manager, MigrationResourcesProvider provider) {
            manager.updateRole(
                    manager.getRole(collection, RMRoles.MANAGER).withNewPermissions(asList(MANAGE_LABELS_PERMISSION)));
        }
    }

}