package com.mojix.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojix.integration.BridgeFileGenerator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PoolConnection {
	
	private static final String PROPERTIES_FILE_NAME = "hikari.properties";
	private static final Logger logger = LoggerFactory.getLogger(PoolConnection.class);
	
	private static PoolConnection instance;
	private HikariDataSource dataSource;
	private HashMap<ResultSet, Connection> connections;
	
	private PoolConnection() {
		File currentFolder;
		Properties properties = new Properties();
		HikariConfig config = new HikariConfig();
		this.connections = new HashMap<ResultSet, Connection>();
		
		logger.info("Loading parameters from file {}", PROPERTIES_FILE_NAME);
		
		try {
			currentFolder = new File(
					PoolConnection.class.getProtectionDomain().getCodeSource().getLocation().toURI()
				);
			properties.load(new FileInputStream(currentFolder.getParent() + File.separator + PROPERTIES_FILE_NAME));
			//properties.load(ClassLoader.getSystemResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (FileNotFoundException e) {
			logger.warn("File {} not found", PROPERTIES_FILE_NAME, e);
			System.exit(4);
		} catch (IOException e) {
			logger.error("Cannot read file {}", PROPERTIES_FILE_NAME, e);
			System.exit(3);
		} catch (URISyntaxException e) {
			logger.error("Error trying to get the current jar's folder");
			System.exit(4);
		}
		
		config.setUsername(properties.getProperty("user"));
	    config.setPassword(properties.getProperty("password"));
	    config.setJdbcUrl(properties.getProperty("jdbcUrl"));
	    config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("maxPoolSize")));
	    config.setMaxLifetime(Integer.parseInt(properties.getProperty("maxLifeTime")));
	    
		this.dataSource = new HikariDataSource(config);
	}
	
	public static PoolConnection getInstance() {
		if(instance == null) {
			instance= new PoolConnection();
		}
		
		return instance;
	}
	
	public ResultSet execute(String query) throws SQLException {
		return execute(query, new Object[0]);
	}
	
	public ResultSet execute(String query, Object... objects) throws SQLException {
		ResultSet result = null;
		Connection connection = null;
		PreparedStatement statement;
		
		connection = this.dataSource.getConnection();
		statement = connection.prepareStatement(query);
		
		addParameters(statement, objects);
		
		result = statement.executeQuery();
		
		this.connections.put(result, connection);
		
		return result;
	}
	
	public void close(ResultSet resultSet) {
		Connection connection = this.connections.get(resultSet);
		
		this.connections.remove(resultSet);
		
		try {
			resultSet.close();
			connection.close();
		} catch (SQLException e) {
			System.out.println("Couldn't close connection.");
		}
	}
	
	private void addParameters(PreparedStatement statement,
							   Object[] objects) throws SQLException {
		int countParameters = 1;
		
		if(objects != null && objects.length > 0) {
			for(Object parameter : objects) {
				if(parameter instanceof Integer) {
					statement.setInt(countParameters, (Integer) parameter);
				} else if(parameter instanceof String) {
					statement.setString(countParameters, (String) parameter);
				} else if(parameter instanceof Long) {
					statement.setLong(countParameters, (Long) parameter);
				}
				
				countParameters++;
			}
		}
	}
	
}
