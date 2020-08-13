# SQL Insert Formatter
It happens that testdata required for unit tests is inserted into the database like this:
```
INSERT INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd');
INSERT INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd');
INSERT INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd');
INSERT INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd');
INSERT INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd');
```
When inserting multiple entries into the same table, there is no need to use multiple insert statements, instead use batch inserts like this:
```
INSERT INTO TABLEA (ID,AGE,NAME) VALUES 
(1,12,'asd'),
(1,12,'asd'),
(1,12,'asd'),
(1,12,'asd'),
(1,12,'asd');
```
Advantages:
* more reader-friendly
* faster execution time (see for example: https://stackoverflow.com/questions/1793169/which-is-faster-multiple-single-inserts-or-one-multiple-row-insert)

## Requirements
* Java 8

## Features
* SQL keywords (INSERT INTO, VALUES), the table name and the columns will be transformed to uppercase
* Whitespaces inside columns clause will be removed
* Option to specify the amounts of values for one insert statement
* Option to remove redundant whitespaces (outside quotes) inside the VALUES (...) clause

## Usage
* Clone repo & run a Maven build with `clean install` OR download the JAR from the Releases tab

* Formatting a SQL file (default amount of values for one insert is 100):
```
java -jar sql-insert-formatter.jar -file path/to/sql-file.sql [-split 100] [-formatspaces]
```
The `-split` and `-formatspaces` options are optional. The formatted file will be in the same directory as the input SQL file.

* Formatting a SQL file with specifying amount of values:
```
java -jar sql-insert-formatter.jar -file test.sql -split 25
```
Each insert statement will have 25 values.

* Formatting a SQL file with only removing whitespaces outside quotes inside the VALUES (...) clause:
```
java -jar sql-insert-formatter.jar -file test.sql -formatspaces
```
Example for formatted whitespaces:
```
INSERT INTO TABLEA (ID,AGE,NAME) VALUES (    1  ,    12 ,   '   a s  d   '      );
INSERT INTO TABLEA (ID,AGE,NAME) VALUES (    1  ,    12 ,   '   a s  d   '      );
INSERT INTO TABLEA (ID,AGE,NAME) VALUES (    1  ,    12 ,   '   a s  d   '      );
```
Output:
```
INSERT INTO TABLEA (ID,AGE,NAME) VALUES 
(1,12,'   a s  d   '),
(1,12,'   a s  d   '),
(1,12,'   a s  d   ');
```

## Used Libraries
* Apache Commons CLI (https://commons.apache.org/proper/commons-cli/) - for parsing command line arguments
