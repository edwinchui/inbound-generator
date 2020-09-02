package com.mojix.readers;

import com.mojix.integration.BridgeFileGenerator;
import com.mojix.integration.PartImportTypeFactory;
import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.PartImportMGOImpl;
import com.mojix.integration.gm.mgoa.DiscretePartMgoA;
import com.mojix.integration.gm.mgoa.PartBomMgoA;
import com.mojix.integration.gm.mgoa.PartGroupMgoA;
import com.mojix.integration.gm.mgoc.DiscretePartMgoC;
import com.mojix.integration.gm.mgoc.PartBomMgoC;
import com.mojix.integration.gm.mgoc.PartGroupMgoC;
import com.mojix.writers.TextFileWriter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.mojix.integration.gm.PartImportMGO.*;

public class ExcelFileReader implements FileReader {

    private static final Logger logger = LoggerFactory.getLogger(ExcelFileReader.class);

    private Workbook workbook;

    public ExcelFileReader(String fileName) {
        File excelFile = new File(getPathToFile(fileName));

        if(!excelFile.exists()) {
            logger.error("The file {} doesn't exist", getPathToFile(fileName));
            System.exit(10);
        }

        try {
            this.workbook = XSSFWorkbookFactory.createWorkbook(new FileInputStream(excelFile));
        } catch (IOException | InvalidFormatException e) {
            logger.error("Couldn't open file {}", fileName, e);
            System.exit(11);
        }
    }


    public void convertSheetsToText() {
        Sheet sheet;
        XSSFRow row;
        List<IntegrationField> headers;
        List<Map<IntegrationField, String>> listRecords;
        final Properties paramProperties = BridgeFileGenerator.readPropertiesParams();

        for(Iterator<Sheet> iterator = this.workbook.sheetIterator();iterator.hasNext();) {
            sheet = iterator.next();
            headers = getHeaders(sheet);
            listRecords = new ArrayList<>();

            for(int i=1;i<sheet.getPhysicalNumberOfRows();i++) {
                row = (XSSFRow) sheet.getRow(i);

                if(row != null) {
                    listRecords.add(readDataIntoMap(headers, row));
                }
            }

            writeDataToTextFile(sheet.getSheetName(), listRecords, paramProperties);
        }
    }

    private void writeDataToTextFile(String sheetName,
                                     List<Map<IntegrationField, String>> listRecords,
                                     Properties paramProperties) {
        TextFileWriter textFileWriter = new TextFileWriter(
                (PartImportMGOImpl) PartImportTypeFactory.getPartImportBySheetName(sheetName, paramProperties)
        );

        try {
            textFileWriter.initTargetFile();
            textFileWriter.writeRecords(listRecords);
            textFileWriter.finishWriteFile();
        } catch (IOException e) {
            logger.error("An error occurred when generating text file {}", textFileWriter.getFileName(), e);
            System.exit(12);
        }
    }

    private Map<IntegrationField, String> readDataIntoMap(List<IntegrationField> headers, XSSFRow row) {
        Map<IntegrationField, String> mapRecord = new HashMap<>();

        for(int i=0;i<headers.size();i++) {
            XSSFCell cell = row.getCell(i);
            Optional.ofNullable(headers.get(i))
                    .ifPresent(header -> {
                        String value;

                        if(cell != null && cell.getCellType().compareTo(CellType.NUMERIC) == 0) {
                            value = Integer.toString( Double.valueOf(cell.getNumericCellValue()).intValue() );
                        } else if(cell != null && cell.getCellType().compareTo(CellType.STRING) == 0) {
                            value = cell.getStringCellValue();
                        } else {
                            value = "";
                            if(cell != null) {
                                logger.warn(
                                        "Type {} for cell [{}.{}] is not supported, the value will be set empty",
                                        cell.getCellType(),
                                        cell.getRowIndex(),
                                        cell.getColumnIndex()
                                );
                            }
                        }

                        mapRecord.put(header, header.getFormatedValue(value));
                    });

        }

        return mapRecord;
    }

    private List<IntegrationField> getHeaders(Sheet sheet) {
        XSSFCell cell;
        XSSFRow row = (XSSFRow) sheet.getRow(0);
        List<IntegrationField> headers = new ArrayList<>();

        for(int i=0;i<row.getPhysicalNumberOfCells();i++) {
            cell = row.getCell(i);

            headers.add( getEnumValue(cell.getStringCellValue(), sheet.getSheetName()) );
        }

        return headers;
    }

    private IntegrationField getEnumValue(String value,
                                          String sheetName) {
        if(sheetName.contains(MGOA)) {
            if(sheetName.contains(DISCRETE_PART_FILE_TYPE_IDENTIFIER)) {
                return DiscretePartMgoA.DiscreteFieldsMGOA.valueOf(value);
            } else if(sheetName.contains(GROUP_FILE_TYPE_IDENTIFIER)) {
                return PartGroupMgoA.PartGroupFieldsMGOA.valueOf(value);
            } else if(sheetName.contains(BOM_FILE_TYPE_IDENTIFIER)) {
                return PartBomMgoA.PartBomFieldsMGOA.valueOf(value);
            }
        } else if(sheetName.contains(MGOC)) {
            if(sheetName.contains(DISCRETE_PART_FILE_TYPE_IDENTIFIER)) {
                return DiscretePartMgoC.DiscreteFieldsMGOC.valueOf(value);
            } else if(sheetName.contains(DGO_FILE_TYPE_IDENTIFIER)) {
                return PartGroupMgoC.PartDGOFieldsMGOC.valueOf(value);
            } else if(sheetName.contains(BOM_FILE_TYPE_IDENTIFIER)) {
                return PartBomMgoC.PartBomFieldsMGOC.valueOf(value);
            }
        }

        return null;
    }
}
