package com.mojix.integration.gm.mgoa;

import com.mojix.dao.PoolConnection;
import com.mojix.integration.PlantParams;
import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.Item.Padding;
import com.mojix.integration.gm.PartImportMGOImpl;
import com.mojix.pojo.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mojix.integration.gm.mgoa.PartGroupMgoA.PartGroupFieldsMGOA.*;

public class PartGroupMgoA extends PartImportMGOImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(PartImportMGOImpl.class);
	
	public enum PartGroupFieldsMGOA implements IntegrationField {
		SEQUENCE_NUMBER	( 0,  5, Padding.ZEROS_LEFT),
		PLANT_CODE 		( 6,  7, Padding.ZEROS_LEFT),
		FILLER_01       ( 8,  9, Padding.BLANK_LEFT),
		GROUP_NAME 		(10, 17, Padding.BLANK_RIGHT),
		FILLER_02       (18, 19, Padding.BLANK_LEFT),
		PART_NUMBER 	(20, 27, Padding.ZEROS_LEFT);
		
		private int beginIndex;
		private int endIndex;
		private Padding padding;
		
		PartGroupFieldsMGOA	(int beginIndex, int endIndex, Padding padding) {
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.padding = padding;
		}
		
		@Override
		public Padding getPadding() {
			return this.padding;
		}

		@Override
		public String getLabelName() {
			return name();
		}

		@Override
		public int getBeginIndex() {
			return this.beginIndex;
		}

		@Override
		public int getEndIndex() {
			return this.endIndex;
		}
	}
	
	public List<Map<IntegrationField, String>> buildLines(String plantCode) {
		logger.info("Begin part-group relation process for plant code {}", plantCode);
		int lineCount = 0;
		StringBuilder builder = new StringBuilder();

		Map<IntegrationField, String> mapRecord;
		List<Map<IntegrationField, String>> listRecords = new ArrayList<>();
		List<String> cacheOperations = initCacheOperations(plantCode);
		List<Part> partNumbersPerPlant = this.getPartNumbersPerPlant(plantCode);
		PlantParams processingPlantParams = this.getIntegrationParams().getPlantParams(plantCode);
		
		if(cacheOperations.isEmpty()) {
			logger.warn("There are no operations to assign. Please verify the name length is lower than 8 characters.");
			logger.warn("Data will not be generated for {} plant", plantCode);
			
			return null;
		}
		
		if(partNumbersPerPlant.size() > processingPlantParams.getPartAssignmentRecords()) {
			partNumbersPerPlant = partNumbersPerPlant.subList(0, processingPlantParams.getPartAssignmentRecords());
		}
		do {
			for(Part partNumber : partNumbersPerPlant) {
				mapRecord = new LinkedHashMap<>();

				mapRecord.put(SEQUENCE_NUMBER, SEQUENCE_NUMBER.getFormatedValue(super.getSequenceNumber()));
				mapRecord.put(PLANT_CODE, PLANT_CODE.getFormatedValue(plantCode));
				mapRecord.put(FILLER_01, FILLER_01.getFormatedValue(""));
				mapRecord.put(GROUP_NAME, GROUP_NAME.getFormatedValue(this.getOperationName(cacheOperations)));
				mapRecord.put(FILLER_02, FILLER_02.getFormatedValue(""));
				mapRecord.put(PART_NUMBER, PART_NUMBER.getFormatedValue(partNumber.getPartNumberBase()));

				listRecords.add(mapRecord);

				lineCount++;
			}
			if(lineCount < processingPlantParams.getPartAssignmentRecords()) {
				partNumbersPerPlant = partNumbersPerPlant.subList(
							0,
							lineCount - processingPlantParams.getPartAssignmentRecords()
						);
			}
			
		} while(lineCount < processingPlantParams.getPartAssignmentRecords());
		
		
		return listRecords;
	}
	
	public String getOperationName(List<String> cacheOperations) {
		int randomIndex = super.getNextIntRandom(cacheOperations.size()-1, 0);
		
		return cacheOperations.get(randomIndex);
	}
	
	private List<String> initCacheOperations(String plantCode) {
		ResultSet resultSet = null;
		StringBuilder queryBuilder = new StringBuilder();
		List<String> cacheOperations = new ArrayList<String>();
		
		queryBuilder.append("select distinct g.NAME0 ");
		queryBuilder.append("  from groupjdobase g ");
		queryBuilder.append("  join groupjdobase gg ");
		queryBuilder.append("    on g.field_02 = gg.jdoid ");
		queryBuilder.append(" where g.type0 = 5");
		queryBuilder.append("   and g.archived = 0");
		queryBuilder.append("   and length(g.name0) <= 7");
		queryBuilder.append("   and gg.CODE = ?");
		
		try {
			resultSet = PoolConnection.getInstance().execute(queryBuilder.toString(), plantCode);
			while(resultSet.next()) {
				cacheOperations.add(resultSet.getString(1));
			}
		} catch (SQLException e) {
			logger.error("Couldn't execute the query {}", queryBuilder.toString());
		} finally {
			PoolConnection.getInstance().close(resultSet);
		}
		
		logger.info("{} groups of type Operation where recovered from plant code {}", cacheOperations.size(), plantCode);
		
		return cacheOperations;
	}

	@Override
	public String getFileTypeIdentifier() {
		return GROUP_FILE_TYPE_IDENTIFIER;
	}

	@Override
	public List<IntegrationField> getHeaders() {
		return Arrays.asList(PartGroupFieldsMGOA.values());
	}

	@Override
	public String getHeader() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_A_DATE_FORMAT);
		
		return String.format(
				"HEADER  %s",
				dateFormat.format(new Date())
			);
	}

	@Override
	public String getTrailer() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_A_DATE_FORMAT);
		
		return String.format(
				"TRAILER  %s",
				dateFormat.format(new Date())
			);
	}
	
}
