package com.mojix.writers;

import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.PartImportMGO;
import com.mojix.integration.gm.PartImportMGOImpl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSVFileWriter implements FileWriter {

    private static final String CSV_SUFFIX = ".csv";
    private static Logger logger = LoggerFactory.getLogger(CSVFileWriter.class);

    private List<IntegrationField> headers;
    private PartImportMGO partImportMGO;
    private CSVPrinter csvPrinter;

    public CSVFileWriter(PartImportMGO partImportMGO) {
        this.partImportMGO = partImportMGO;
        this.headers = this.partImportMGO.getHeaders();
    }

    @Override
    public void initTargetFile() throws IOException {
        this.csvPrinter = this.getCSVPrinter();

    }

    @Override
    public void writeRecords(List<Map<IntegrationField, String>> listRecords) {
        listRecords.stream().forEach(mapRecord -> {
            try {
                writeSingleLine(mapRecord);
            } catch (IOException e) {
                logger.warn("There was a error writing a line in the file", e);
            }
        });
    }

    @Override
    public void finishWriteFile() throws IOException {
        if (csvPrinter != null) {
            csvPrinter.flush();
            csvPrinter.close();
        }
    }

    @Override
    public String getFileName() {
        StringBuilder builder = new StringBuilder();

        builder.append(this.partImportMGO.getFileName());
        builder.append(CSV_SUFFIX);

        return builder.toString();
    }

    private void writeSingleLine(Map<IntegrationField, String> mapRecord) throws IOException {
        List<String> values;

        values = headers.stream().map(header -> mapRecord.get(header)).collect(Collectors.toList());

        this.csvPrinter.printRecord(values);
    }

    private CSVPrinter getCSVPrinter() throws IOException {
        CSVPrinter csvPrinter;
        List<String> columnNames;
        File file = new File(getPathToFile());
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

        columnNames = headers.stream().map(header -> header.getLabelName()).collect(Collectors.toList());
        csvPrinter = CSVFormat.EXCEL
                .withHeader(columnNames.toArray(new String[columnNames.size()]))
                .print(writer);
        csvPrinter.printComment(getFileName());

        return csvPrinter;
    }
}
