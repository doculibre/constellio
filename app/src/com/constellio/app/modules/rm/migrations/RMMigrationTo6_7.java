package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.reports.wrapper.ReportConfig;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.users.UserServices;
import org.eclipse.jetty.deploy.App;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RMMigrationTo6_7 implements MigrationScript {

    private RecordServices rs;
    private Map<String, Integer> map = new HashMap<>();
    private AppLayerFactory factory;
    private String collection;
    private UserServices userServices;

    @Override
    public String getVersion() {
        return "6.7";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
            throws Exception {
        new SchemaAlterationsFor6_7(collection, provider, factory).migrate();
        createDefaultLabel(collection, factory);
    }

    public void createDefaultLabel(String collection, AppLayerFactory factory) throws Exception {
        map.put("5159", 7);
        map.put("5161", 10);
        map.put("5162", 7);
        map.put("5163", 5);
        ModelLayerFactory model = factory.getModelLayerFactory();
        rs = model.newRecordServices();
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, factory);
        MetadataSchemaType metaBuilder = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(ReportConfig.SCHEMA_TYPE);
        MetadataSchema typeBuilder = metaBuilder.getSchema(RMReport.SCHEMA_LABEL);
        ContentManager contentManager = model.getContentManager();
        userServices = model.newUserServices();
        Transaction trans = new Transaction();

        File f = new File("C:\\Users\\Marco\\Desktop\\Template_Etiquette\\");
        List<File> files = getFolders(f);
        for (File fi : files) {
            Record record = rs.newRecordWithSchema(metaBuilder.getSchema(RMReport.SCHEMA_LABEL));
            String type = fi.getName().matches("(.)+_(Container.jasper)") ? ContainerRecord.SCHEMA_TYPE : Folder.SCHEMA_TYPE;
            String titre = "Code de plan justifié ";
            System.out.println(fi.getName());
            Matcher m = Pattern.compile("(.)+_(\\d{4})_(.)+").matcher(fi.getName());
            m.find();
            String format = m.group(2);
            record.set(typeBuilder.getMetadata(RMReport.TYPE_LABEL), type);
            record.set(typeBuilder.getMetadata(RMReport.LIGNE), map.get(format) + "");

            if (type.equals(Folder.SCHEMA_TYPE)) {
                titre += "de Dossier" + (fi.getName().contains("_D_") ? " à Droite" : " à Gauche");
            } else {
                titre += "de Conteneur";
            }
            titre += " (Avery " + format + ")";
            record.set(typeBuilder.getMetadata(RMReport.COLONNE), "2");
            record.set(typeBuilder.getMetadata(ReportConfig.ISDELETABLE), false);
            ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(fi), "Avery " + format + " " + type);
            record.set(typeBuilder.getMetadata(Report.TITLE), titre);
            record.set(typeBuilder.getMetadata(ReportConfig.JASPERFILE), contentManager.createFileSystem("Avery-" + format + "-" + type + ".jasper", upload));
            trans.add(record);
        }
        rs.execute(trans);
    }

    public List<File> getFolders(File file) {
        List<File> temp = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>(Arrays.asList(file.listFiles()));
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
            Map<String, String> Lang = new HashMap<>();

            Lang.put("fr", "Étiquette");
            Lang.put("en", "Label");
            MetadataSchemaBuilder builder = typesBuilder.getSchemaType(ReportConfig.SCHEMA_TYPE).createCustomSchema(RMReport.SCHEMA_LABEL, Lang);
            builder.create(RMReport.WIDTH).setType(MetadataValueType.STRING).setUndeletable(true).defineDataEntry().asManual();
            builder.create(RMReport.HEIGHT).setType(MetadataValueType.STRING).setUndeletable(true).defineDataEntry().asManual();
            builder.create(RMReport.TYPE_LABEL).setType(MetadataValueType.STRING).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
            builder.create(RMReport.LIGNE).setType(MetadataValueType.STRING).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
            builder.create(RMReport.COLONNE).setType(MetadataValueType.STRING).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
//            try{
//                createDefaultLabel();
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
        }
    }

}