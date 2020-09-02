package com.mojix.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojix.pojo.PartSupplier;

public class CsvFilesReader {
	
	public enum SampleDataFiles{
		SUPPLIERS_CSV_FILE("suppliers.csv", "code", "name"),
		PART_NAMES_CSV_FILE("part_names_cost.csv", "cost", "name");
		
		private String fileName;
		private String[] header;
		
		private SampleDataFiles(String fileName, String... header) {
			this.fileName = fileName;
			this.header = header;
		}
		
		public BufferedReader getBufferedReader() {
			return new BufferedReader(new InputStreamReader(
					CsvFilesReader.class.getClassLoader().getResourceAsStream(fileName)
				));
		}
		
		public String[] getFileHeader() {
			return this.header;
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CsvFilesReader.class);
	private Random random = new Random(System.currentTimeMillis());
	private static final CsvFilesReader INSTANCE = new CsvFilesReader();
	private static final int MAX_NUMBER_OF_RECORDS = 3;
	
	private HashMap<SampleDataFiles, List<CSVRecord>> loadedFiles;
	
	private CsvFilesReader() {
		this.loadedFiles = new HashMap<>();
		
		loadFileRecordsInMemory(SampleDataFiles.SUPPLIERS_CSV_FILE);
		loadFileRecordsInMemory(SampleDataFiles.PART_NAMES_CSV_FILE);
	}
	
	private void loadFileRecordsInMemory(SampleDataFiles sampleDataFile) {
		CSVParser csvParser;
		
		try {
			csvParser = new CSVParser(
					sampleDataFile.getBufferedReader(),
					CSVFormat.DEFAULT
						.withHeader(sampleDataFile.getFileHeader())
						.withIgnoreHeaderCase()
				);
			this.loadedFiles.put(sampleDataFile, csvParser.getRecords());
			csvParser.close();
		} catch (IOException e) {
			logger.error("Reading data from file {}, failed", sampleDataFile.fileName);
		}
	}
	
	private int getNextIntRandom(int maxValue, int minValue) {
		return Math.abs(random.nextInt(maxValue - minValue) - minValue);
	}
	
	public static CsvFilesReader getInstance() {
		return INSTANCE;
	}
	
	public List<PartSupplier> getRandomListSuppliers(SampleDataFiles sampleDataFiles) {
		CSVRecord csvRecord;
		List<PartSupplier> listPartSuppliers = new ArrayList<>();
		int numberOfRecords = getNextIntRandom(MAX_NUMBER_OF_RECORDS, 0);
		
		if(numberOfRecords == 0) {
			listPartSuppliers.add(new PartSupplier("", "", ""));
		} else {
			for(int i=1;i<=numberOfRecords;i++) {
				csvRecord = getRandomRecord(sampleDataFiles);
				listPartSuppliers.add(new PartSupplier(
						csvRecord.get("code"),
						csvRecord.get("name"),
						i==numberOfRecords?"Y":""
						));
			}
		}
		
		return listPartSuppliers;
	}
	
	public CSVRecord getRandomRecord(SampleDataFiles sampleDataFile) {
		return getRecord(sampleDataFile, getNextIntRandom(getSize(sampleDataFile), 0));
	}
	
	public CSVRecord getRecord(SampleDataFiles sampleDataFile, int position) {
		if(this.loadedFiles.get(sampleDataFile).size() <= position ||
				position < 0) {
			logger.error(
					"Given position number is invalid. Size: {}",
					this.loadedFiles.get(sampleDataFile).size()
				);
			
			return null;
		}
		
		return this.loadedFiles.get(sampleDataFile).get(position);
	}
	
	public int getSize(SampleDataFiles sampleDataFile) {
		return this.loadedFiles.get(sampleDataFile).size();
	}
}
