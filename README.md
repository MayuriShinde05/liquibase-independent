# Liquibase Implementation with MAVEN

Liquibase is an open-source database schema change management solution which enables you to manage revisions of your database changes easily. Liquibase makes it easy for anyone involved in the application release process to:

Eliminate errors and delays when releasing databases.
Deploy and roll back changes for specific versions without needing to know what has already been deployed.
Deploy database and application changes together so they always stay in sync.

## What was the use case of the Liquibase should separate project

As we know using Liquibase migration will execute on any database just need to add a db-related configuration and Execute through the command line. Now, we have to write a separate migration and this migration is not necessary to have Project dependencies. In current project structure not allow. The migration requires the Project dependencies. For example: We have one ABC Project in which the Product team right their own migration in the Liquibase project. Many clients share the ABC Project and if they have any other requirements for this we are not able to touch the ABC project. we have to create a new XYZ project and that will override the ABC project. So, when we have to execute the ABC project migration it has code dependencies. For this, we have to make a build for the ABC project and then make the XYZ project maven build then we have to make an independent changelog.xml file for the ABC and XYZ projects. This file we used in the master.xml file and executed the Liquibase migration.

The above problem needs to be overcome when we implement the Liquibase independent project. This is a maven project and we added many dependencies like different types of DB, Liquibase core, Spring-boot, etc.

## Use case of this Independent project:

Anyone can export the project write the migration and test it on their own machine. Only he has db(ORACLE, MSSQL, POSTGRES, etc) configuration details.

###Project structure
```
<project root>

/<project name>-liquibase
    /assembly
          liquibase.xml
	/src.main
		/java/com.example.liquibase.springbootProject
			ServletInitializer.java - initialize the SpringBootServlet
			SpringbootProjectApplication.java - main application class which start this whole application.
    /src.main
        /resources.liquibase
            /db
                changelog-back-end.xml - root migration file
                NNNN-WEB20-XXXX.xml - migration files for individual tasks.
            liquibase_CLI_start.sh (currently we don't have this file, will have plan to add to run using command line)
            liquibase_CLI_start_win.bat (currently we don't have this file, will have plan to add to run using command line)
        /resources
            liquibase_default.properties 
			application_mssql.properties
			application_oracle.properties
			application_postgres.properties
```

## Beginning of migration file:
1) Every migration must start with:
```
<?xml version="1.1" encoding="UTF-8" ?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd">
```
				   
2) The following may be a list of parameters required due to differences between the mssql and oracle DBMS:
```
<property name="__VARCHAR_100" value="nvarchar(100)" dbms="mssql"/>
<property name="__DATE_NOW" value="getdate()" dbms="mssql"/>

<property name="__VARCHAR_100" value="varchar2(100 CHAR)" dbms="oracle"/>
<property name="__DATE_NOW" value="sysdate" dbms="oracle"/>
```

name - parameter name
value - the parameter value that will be substituted during the migration
dbms - SUBD

### Usage example:
```
type="${__VARCHAR_100}"
valueDate="${__DATE_NOW}"
```

3) You must set the migration tag:

```
<changeSet logicalFilePath ="migration_file_name.xml" author ="author_name" id ="tag" >     
	<tagDatabase tag ="migration_file_name" /> 
</changeSet>           
```           

## Data types:
| Liquibase data type	| SQL Server data type						| Oracle data type |
|:--------------------: | :---------------------------------------: | :--------------: |
| bigint				| bigint									| number(38,0)     |
| blob					| varbinary(max)							| blob             |
| boolean				| bit										| number(1)        |
| char					| char										| char             |
| clob					| nvarchar(max)								| clob             |
| currency				| money										| number(19,4)     |
| datetime				| smalldatetime or datetime2				| timestamp        |
| date					| date or smalldatetime (version <= 2005)	| date             |
| decimal				| decimal									| decimal          |
| double				| float										| float(24)        |
| float					| float										| float            |
| int					| int										| number(10)       |
| mediumint				| mediumint									| mediumint        |
| nchar					| nchar										| nchar            |
| nvarchar				| nvarchar									| nvarchar2        |
| number				| numeric									| number           |
| smallint				| smallint									| number(5)        |
| time					| time or datetime (version <= 2005)		| date             |
| timestamp				| datetime									| timestamp        |
| tinyint				| tinyint									| number(3)        |
| uuid					| uniqueidentifier							| raw(16)          |
| varchar				| varchar									| varchar2         |

## ChangeSets:
ChangeSet - The set of changes that will be applied. There can be several changesets in one migration file; it is worth breaking them down according to the logic of actions.
```
<changeSet logicalFilePath ="migration_file_name.xml" 
		   author ="author_name" id ="table_name" >
        <rollback> 
			----rollback changes------- 
		</rollback> 
</changeSet>
```

## Pure sql
```
<changeSet logicalFilePath ="migration_file_name.xml"
            author ="author_name"
            id ="insert into table_name" >
            <sql>raw_sql</sql>
            <rollback>
                raw_sql_rollback
            </rollback>
</changeSet>
```

## String type column expansion:
Parameters from the issue in Jira:

1) Size_columns

2) Table_name

3) Column_name

```
<property name ="__nvarchar_Column_Size" value ="nvarchar(Column_Size)" dbms ="mssql" />

<property name ="__nvarchar_Column_Size " value ="varchar2(Column_Size)" dbms ="oracle" />


<changeSet logicalFilePath ="migration_file_name.xml"
           author ="author_name"
           id ="expand field Column_name from Table_name" >
        <modifyDataType tableName = "TableName" columnName = " ColumnName " newDataType = "${ __nvarchar_Column_Size }"
</changeSet>
```

##Adding an index:
Options:

1) Index_name

2) Table_name

3) Column_name

4) Column_type

```
<changeSet logicalFilePath ="migration_file_name.xml"
           author ="author_name"
           id ="add Index_name index for table_name table" >
      <preConditions onFail ="CONTINUE" > 
          <not> 
              <indexExists tableName ="Table_Name" indexName ="Index_Name" />           
		  </not>       
	  </preConditions>       
	  <createIndex tableName ="Table_Name" indexName ="Index_Name" >           
	      <column name = "Column_name" type ="Column_type"/ >       
	  </createIndex> 
</changeSet>                           
```

## Adding a primary key:
Options:

1) Key_name

2) Table_name

3) Column_name (separated by commas)

	<changeSet logicalFilePath ="migration_file_name.xml"
           author ="author_name"
           id ="add private key for table_name table" >

		<preConditions onFail ="CONTINUE" > 
			<not> 
				<primaryKeyExists tableName ="TableName" primaryKeyName ="KeyName" /> 
			</not> 
		</preConditions>
		<addPrimaryKey columnNames="Column_name" constraintName="Constraint_Name" tableName="Table_name"/>
	</changeSet>

## Adding a foreign key:
Options:

1) Key_name

2) Table_name

3) Column_name

4) Name_of_the_table

5) Name_of_the_column_to be linked

6) Action_on_removal ( 'CASCADE', 'SET NULL', 'SET DEFAULT', 'RESTRICT', 'NO ACTION')

7) Action_on_update ('CASCADE', 'SET NULL', 'SET DEFAULT', 'RESTRICT', 'NO ACTION')

```
<changeSet logicalFilePath ="migration_file_name.xml"
           author ="author_name"
           id ="add foreign key for table_name table" >   

			<preConditions onFail="CONTINUE">
				<not>
					<foreignKeyConstraintExists foreignKeyTableName ="TableName" foreignKeyName ="KeyName" />
				</not>
			</preConditions>
			<addForeignKeyConstraint baseColumnNames="Based_Column_Names"
                               baseTableName="Base_Table_Name"
                               constraintName="Constraint_Name"
                               onDelete="Action_on_deletion"
                               onUpdate="Action_on_update"
                               referencedColumnNames="Referenced_Column_Names"
                               referencedTableName="Referenced_Table_Name"/>
</changeSet>
```

## Creating a New Table:

```
<changeSet author="author_name" id="create_tbl"
		logicalFilePath="migration_file_name.xml" dbms="mssql">
		<preConditions onFail="MARK_RAN">
			<not>
				<tableExists tableName="table_name" />
			</not>
		</preConditions>
		<createTable tableName="table_name"remarks ="table_description" >             
			<column name="id" remarks="column_description" type="data_type or parameter"
              incrementBy="1"
              autoIncrement="true">
				<constraints primaryKey="true" nullable="false"/>
			</column>

			<column name ="column_name" 
				remarks ="column_description" 
				type ="data_type or parameter" > 
			<constraints ...... /> 
			</column>
			.....
		</createTable>
		<rollback>
			<dropTable tableName="table_name" />
		</rollback>
</changeSet>
```

## Adding a Column to an Existing Table:

```
<changeSet logicalFilePath ="migration_file_name.xml" 
           author ="author_name" 
           id ="Create table table_name" >
		<preConditions onFail ="CONTINUE" > 
			<not> 
				<columnExists tableName ="" columnName ="" /> 
			</not> 
		</preConditions> 

		<addColumn tableName ="table_name" > 
			<column name ="column_name" remarks ="column_description " type ="data_type or parameter" >            
				<constraints ...... />        
			</column>     
		</addColumn> 
</changeSet>  
```

## Inserting data into a table:

```
<changeSet logicalFilePath ="migration_file_name.xml"
           author ="author_name"
           id ="insert into table_name" >

			<preConditions onFail="CONTINUE">
				<sqlCheck expectedResult="0">select count(*) from название_таблицы where ...</sqlCheck>
			</preConditions>
            <insert tableName="table_name" >
				<column name="column_name" value*  = "..." />

                    ..........

			</insert>

            <rollback>
                <delete tableName="название_таблицы">
                    <where>......</where>
                </delete>
            </rollback>

</changeSet>
```

### value* - depending on the column data type:

<table class="properties">
	<tr>
		<td>value</td>
		<td>Value to set the column to. The value will be surrounded by quote marks and nested quote marks will be escaped.</td>
	</tr>
	<tr>
		<td>computed</td>
		<td>Used if the value in "name" isn't actually a column name but actually a function.</td>
	</tr>
	<tr>
		<td>valueNumeric</td>
		<td>Numeric value to set the column to. The value will not be escaped and will not be nested in quote marks.</td>
	</tr>
	<tr>
		<td>valueBoolean</td>
		<td>Boolean value to set the column to. The actual value string inserted will be dependent on the database implementation.</td>
	</tr>
	<tr>
		<td>valueDate</td>
		<td>Date and/or Time value to set the column to. The value is specified in one of the following forms: "YYYY-MM-DD", "hh:mm:ss" or "YYYY-MM-DDThh:mm:ss".</td>
	</tr>
	<tr>
		<td>valueComputed</td>
		<td>A value that is returned from a function or procedure call. This attribute will contain the function to call.</td>
	</tr>
	<tr>
		<td>valueBlobFile</td>
		<td>Path to a file, whose contents will be written as a BLOB (i.e. chunk of binary data). Must be either absolute or relative to the Change Log file location (absolute paths are, e.g.: /usr/local/somefile.dat on Unix or c:\Directory\somefile.dat on Windows. Please refer to java.io.File javadoc for the details of what to consider relative/absolute path).</td>
	</tr>
	<tr>
		<td>valueClobFile</td>
		<td>Path to a file, whose contents will be written as a CLOB (i.e. chunk of character data). Must be either absolute or relative to the Change Log file location (absolute paths are, e.g.: /usr/local/somefile.dat on Unix or c:\Directory\somefile.dat on Windows. Please refer to java.io.File javadoc for the details of what to consider relative/absolute path).</td>
	</tr>
</table>


## Update data into a table:

```
<changeSet author="author_name" id="update value into table"
		logicalFilePath="migration_file_name.xml">
		<preConditions>
			<dbms type="mssql" />
		</preConditions>
		<update tableName="table_name">
			<column name="column_name" value*="..." />
			<where>......</where>
		</update>
</changeSet>
```

## How to run:
This is Spring boot application.
Update the application_*.properties with correct details.
Configure the application properties using 
-Dspring.config.location=<path of application properties file>\application_*.properties
Start the the application.

## Documentation:
https://www.liquibase.org/documentation/