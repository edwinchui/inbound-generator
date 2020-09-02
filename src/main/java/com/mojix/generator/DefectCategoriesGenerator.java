package com.mojix.generator;

import com.mojix.dao.PoolConnection;
import com.mojix.integration.IntegrationParams;
import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.Item;
import com.mojix.integration.gm.PartImportMGOImpl;
import com.mojix.writers.CSVFileWriter;
import com.mojix.writers.FileWriter;
import org.fluttercode.datafactory.impl.DataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DefectCategoriesGenerator extends PartImportMGOImpl {

    private static Logger logger = LoggerFactory.getLogger(DefectCategoriesGenerator.class);
    private final List<String> types = Arrays.asList("Scrap", "Reject", "Obsolete");

    private enum DefectCategoriesFields implements IntegrationField {
        COMPANY_NAME("Company Name"),
        PLANT_NAME("Plant Name"),
        DEPARTMENT_NAME("Department Name"),
        ZONE_NAME("Zone Name"),
        TEAM_NAME("Team Name"),
        OPERATION_NAME("Operation Name"),
        TYPE("Defect Type"),
        PARENT_LEVEL_1("Defect Category (1)"),
        PARENT_LEVEL_2("Defect Category (2)"),
        PARENT_LEVEL_3("Defect Category (3)"),
        PARENT_LEVEL_4("Defect Category (4)"),
        PARENT_LEVEL_5("Defect Category (5)"),
        CODE("Code");

        private String labelName;

        DefectCategoriesFields(String labelName) {
            this.labelName = labelName;
        }

        @Override
        public int getBeginIndex() {
            return 0;
        }

        @Override
        public int getEndIndex() {
            return 0;
        }

        @Override
        public Item.Padding getPadding() {
            return null;
        }

        @Override
        public String getLabelName() {
            return this.labelName;
        }

    }

    private String plantCode;
    private Integer numberOfRecords;
    private IntegrationParams integrationParams;

    public DefectCategoriesGenerator(String plantCode, Integer numberOfRecords) {
        this.plantCode = plantCode;
        this.numberOfRecords = numberOfRecords;
    }

    @Override
    public String getFileTypeIdentifier() {
        return null;
    }

    @Override
    public void addConfigurationParams(IntegrationParams integrationParams) {
        this.integrationParams = integrationParams;
    }

    @Override
    public void generateFile() {
        FileWriter fileWriter = new CSVFileWriter(this);

        try {
            fileWriter.initTargetFile();

            fileWriter.writeRecords(buildLines(this.plantCode));

            fileWriter.finishWriteFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<IntegrationField> getHeaders() {
        return Arrays.asList(DefectCategoriesFields.values());
    }

    @Override
    public IntegrationParams getIntegrationParams() {
        return this.integrationParams;
    }

    @Override
    public String getTrailer() {
        return null;
    }

    @Override
    public String getHeader() {
        return null;
    }

    @Override
    public List<Map<IntegrationField, String>> buildLines(String plantCode) {
        Map<IntegrationField, String> record;
        DataFactory dataFactory = new DataFactory();
        List<Map<IntegrationField, String>> cacheHierarchyGroupNames;
        List<Map<IntegrationField, String>> data = new ArrayList<>();
        cacheHierarchyGroupNames = initCacheHierarchyGroupNames(plantCode);

        for(int i=0;i<this.numberOfRecords;i++) {
            record = new HashMap<>();
            record.putAll((Map<? extends IntegrationField, ? extends String>) getNextIntRandom(cacheHierarchyGroupNames));
            record.put(DefectCategoriesFields.TYPE, (String) getNextIntRandom(types));
            record.put(DefectCategoriesFields.PARENT_LEVEL_1, dataFactory.getName());
            record.put(DefectCategoriesFields.PARENT_LEVEL_2, "");
            record.put(DefectCategoriesFields.PARENT_LEVEL_3, "");
            record.put(DefectCategoriesFields.PARENT_LEVEL_4, "");
            record.put(DefectCategoriesFields.PARENT_LEVEL_5, "");
            record.put(DefectCategoriesFields.CODE, dataFactory.getName());

            data.add(record);
        }

        return data;
    }

    @Override
    public String getFileName() {
            return "DefectCategories";
    }

    private List<Map<IntegrationField, String>> initCacheHierarchyGroupNames(String plantCode) {
        ResultSet resultSet = null;
        Map<IntegrationField, String> hierarchyNames;
        List<Map<IntegrationField, String>> cacheGroupNames = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("select company.NAME0,\n");
        queryBuilder.append("        plant.NAME0,\n");
        queryBuilder.append("        department.NAME0,\n");
        queryBuilder.append("        zone.NAME0,\n");
        queryBuilder.append("        team.NAME0,\n");
        queryBuilder.append("        operation.NAME0\n");
        queryBuilder.append("  from GROUPJDOBASE g\n");
        queryBuilder.append("       left outer join GROUPJDOBASE company     on g.FIELD_01 = company.JDOID\n");
        queryBuilder.append("       left outer join GROUPJDOBASE plant       on g.FIELD_02 = plant.JDOID\n");
        queryBuilder.append("       left outer join GROUPJDOBASE department  on g.FIELD_03 = department.JDOID\n");
        queryBuilder.append("       left outer join GROUPJDOBASE zone        on g.FIELD_04 = zone.JDOID\n");
        queryBuilder.append("       left outer join GROUPJDOBASE team        on g.FIELD_05 = team.JDOID\n");
        queryBuilder.append("       left outer join GROUPJDOBASE operation   on g.FIELD_06 = operation.JDOID\n");
        queryBuilder.append(" where g.FIELD_02 = (select jdoid from GROUPJDOBASE where CODE = '" + plantCode + "')\n");
        queryBuilder.append("   and g.ARCHIVED = 0\n");
        queryBuilder.append("   and g.TYPE0 not in (select JDOID from TYPEJDOBASE where NAME0 in ('Plant', 'Company', 'root'))\n");

        try {
            resultSet = PoolConnection.getInstance().execute(queryBuilder.toString());

            while(resultSet.next()) {
                hierarchyNames = new HashMap<>();
                hierarchyNames.put(DefectCategoriesFields.COMPANY_NAME, resultSet.getString(1));
                hierarchyNames.put(DefectCategoriesFields.PLANT_NAME, resultSet.getString(2));
                hierarchyNames.put(DefectCategoriesFields.DEPARTMENT_NAME, resultSet.getString(3));
                hierarchyNames.put(DefectCategoriesFields.ZONE_NAME, resultSet.getString(4));
                hierarchyNames.put(DefectCategoriesFields.TEAM_NAME, resultSet.getString(5));
                hierarchyNames.put(DefectCategoriesFields.OPERATION_NAME, resultSet.getString(6));

                cacheGroupNames.add(hierarchyNames);
            }
        } catch (SQLException throwables) {
            logger.error("Couldn't execute query {}", queryBuilder.toString());
            System.exit(11);
        } finally {
            PoolConnection.getInstance().close(resultSet);
        }

        return cacheGroupNames;
    }
}


