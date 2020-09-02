# Inbound-Generator

This project generates inbound files for testing porpuses.

### Generate jar file

Go to the folder where the pom.xml file is, then execute the following command:

        > mvn clean compile assembly:single 

### Upload oracle driver to local repository

To upload oracle driver to the local repository use the folloring command:

        > mvn install:install-file -Dfile=ojdbc7_12_1_2.jar -DgroupId=com.oracle -DartifactId=ojdbc7 -Dversion=12.1.2 -Dpackaging=jar

### Generate files

To generate the files run the following command in a command line where the jar is located

        > java -jar inbound-generator-1.4.1-SNAPSHOT-jar-with-dependencies.jar

The files will be generated in the same folder and according to the file parameters.properties, please review this file if you need to know all the options that can be customized. 

### Create single jar package
        > mvn clean compile assembly:single

### Data Base Connection

The application creates a connection to the database, which can be modified in the hikari.properties file

### Convert XLS files to text inbound files

The application can convert xls files with the correct format to inbound text files with the following command

    > java -jar inbound-generator-1.4.1-SNAPSHOT-jar-with-dependencies.jar -c [file-name]

or

    > java -jar inbound-generator-1.4.1-SNAPSHOT-jar-with-dependencies.jar --convert [file-name]

### Generate test data for Defect Categories

To generate test data for Defect Categories, execute the following command:

        > java -jar inbound-generator-1.4.1-SNAPSHOT-jar-with-dependencies.jar -g 100 RT

or

        > java -jar inbound-generator-1.4.1-SNAPSHOT-jar-with-dependencies.jar --generate 100 RT
        
This will generate a CSV file with 100 records for plant with code 'RT'