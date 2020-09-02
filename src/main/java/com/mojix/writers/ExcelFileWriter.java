package com.mojix.writers;

import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.PartImportMGOImpl;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

public class ExcelFileWriter implements FileWriter {

    public static final String FILE_NAME = "inbound_files.xls";
    private static final Logger logger = LoggerFactory.getLogger(ExcelFileWriter.class);

    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    private XSSFCellStyle headerCellStyle;
    private PartImportMGOImpl partImportMGO;

    public ExcelFileWriter(PartImportMGOImpl partImportMGO) {
        this.partImportMGO = partImportMGO;
        this.createNewSheet();

    }

    @Override
    public void initTargetFile() throws IOException {
        Row headerRow = this.sheet.createRow(0);

        for(int i=0;i<this.partImportMGO.getHeaders().size();i++) {
            IntegrationField header = this.partImportMGO.getHeaders().get(i);
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header.getLabelName());
            cell.setCellStyle(headerCellStyle);
        }
    }

    @Override
    public void writeRecords(List<Map<IntegrationField, String>> listRecords) {
        int rowNumber = this.sheet.getLastRowNum() + 1;
        FileOutputStream fileOutputStream = getOutputStreamToFile();
        XSSFCellStyle cellStyle = this.workbook.createCellStyle();

        cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));
        for(Map<IntegrationField, String> mapRecord : listRecords) {
            Cell cell;
            IntegrationField header;
            Row row = this.sheet.createRow(rowNumber++);

            for(int i=0;i<this.partImportMGO.getHeaders().size();i++) {
                header = this.partImportMGO.getHeaders().get(i);
                cell = row.createCell(i);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(mapRecord.get(header).trim());
            }
        }

        try {
            this.workbook.write(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            logger.error("Cannot write changes in file {}.", getFileName(), e);
            System.exit(98);
        }

        for(int j=0;j<this.partImportMGO.getHeaders().size();j++) {
            this.sheet.autoSizeColumn(j);
        }

        this.sheet.createFreezePane(0, 1);
    }

    @Override
    public void finishWriteFile() throws IOException {
        /*FileOutputStream fileOutputStream = this.getOutputStreamToFile();

        this.workbook.write(fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();*/
        this.workbook.close();
    }

    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    private void createNewSheet() {
        Font headerFont;
        Sheet existingSheet;
        File excelFile = new File(getPathToFile());
        String sheetName = this.getSheetName();

        if(excelFile.exists()) {
            logger.info("The file {} already exists, a new sheet {} will be added to the file", this.getFileName(), sheetName);
            try {
                this.workbook = XSSFWorkbookFactory.createWorkbook(new FileInputStream(excelFile));
            } catch (IOException | InvalidFormatException e) {
                logger.error("Couldn't create a new sheet in the file {}", getFileName(), e);
                System.exit(8);
            }
        } else {
            logger.info("The file {} will be created with the new sheet {}", this.getFileName(), sheetName);
            this.workbook = new XSSFWorkbook();
        }

        existingSheet = this.workbook.getSheet(sheetName);
        if(existingSheet != null) {
            logger.info("The sheet {} already exists, this sheet will be removed and a new one will be created", sheetName);
            this.workbook.removeSheetAt(
                    this.workbook.getSheetIndex(existingSheet)
            );
        }

        this.headerCellStyle = workbook.createCellStyle();
        this.sheet = workbook.createSheet(sheetName);

        headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints( (short) 12);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        headerCellStyle.setFont(headerFont);
    }

    private String getSheetName() {
        StringBuilder builder = new StringBuilder();

        builder.append(this.partImportMGO.getIntegrationParams().getPrefixFileNamePattern());
        builder.append(this.partImportMGO.getFileTypeIdentifier());

        return builder.toString();
    }

    private FileOutputStream getOutputStreamToFile() {
        File excelFile = new File(getPathToFile());
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(excelFile);
        } catch (FileNotFoundException e) {
            logger.error("Cannot find file {} to write changes.", getFileName(), e);
            System.exit(99);
        }

        return fileOutputStream;
    }

}
