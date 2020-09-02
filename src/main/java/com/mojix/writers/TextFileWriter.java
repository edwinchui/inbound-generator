package com.mojix.writers;

import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.PartImportMGOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class TextFileWriter implements FileWriter {

    private static Logger logger = LoggerFactory.getLogger(TextFileWriter.class);

    private Long timeStamp;
    private OutputStreamWriter writer;
    private PartImportMGOImpl partImportMGO;

    public TextFileWriter(PartImportMGOImpl partImportMGO) {
        this.partImportMGO = partImportMGO;
        this.timeStamp = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void initTargetFile() throws IOException {
        this.writer = this.getFileWriter();

        Optional.ofNullable(this.partImportMGO.getHeader()).ifPresent(header -> {
            try {
                this.writer.write(header);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.write("\r\n");
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
        Optional.ofNullable(this.partImportMGO.getTrailer()).ifPresent(
                trailer -> {
                    try {
                        writer.write(trailer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        try {
            writer.close();
        } catch (IOException e) {
            logger.error("Couldn't close stream writer", e);
        }
    }

    private void writeSingleLine(Map<IntegrationField, String> mapRecord) throws IOException {
        StringBuilder buildLine = new StringBuilder();

        partImportMGO.getHeaders().stream().forEach(header -> buildLine.append(mapRecord.get(header)));

        this.writer.write(buildLine.toString());
        this.writer.write("\r\n");
    }

    private OutputStreamWriter getFileWriter() {
        String pathToFile;
        OutputStreamWriter writer = null;

        try {
            pathToFile = this.getPathToFile();

            writer = new OutputStreamWriter(
                    new FileOutputStream(pathToFile),
                    FILE_ENCODING
            );
        } catch (Exception e) {
            logger.error("Cannot init stream writer", e);
            System.exit(1);
        }

        return writer;
    }

    @Override
    public String getFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
        StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(this.partImportMGO.getIntegrationParams().getPrefixFileNamePattern());
        nameBuilder.append(this.partImportMGO.getFileTypeIdentifier());
        nameBuilder.append(".");
        nameBuilder.append(dateFormat.format(new Date(this.timeStamp)));

        return nameBuilder.toString();
    }

}
