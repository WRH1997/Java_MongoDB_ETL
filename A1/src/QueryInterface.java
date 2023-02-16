import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/*
Class to interface between user's SQL commands and the tables stored in the Tables.txt file. The class presents the user
with an MySQL CLI interface that accepts create, select, update, insert, and delete commands. It applies the user's SQL
commands on the tables then (if applicable) writes the updated/new values into the Tables.txt file.

@CITATION NOTE: The following tools were used to assist with creating and testing Regex patterns.
URL: https://regex-generator.olafneumann.org/
URL: https://www.regexplanet.com/advanced/java/index.html
Accessed: February 12, 2023

@Author: Waleed R. Alhindi (B00919848)
 */
public class QueryInterface {

    //Map to store data of each table in the Table.txt file where the key is the table's name and the value is
    //the Table object corresponding to that table name.
    private Map<String, Table> allTables;

    //Reference to Table.txt file
    File tableFile;


    /*
    QueryInterface constructor that first fetches the contents from the Table.txt file, then creates
    a Table object for each table in the content's file and stores it in the allTables map. Then, it will
    invoke the SQL_CLI() method to present the user with an SQL CLI.

    @Param no parameters
    @Return no return values
     */
    QueryInterface() throws Exception{
        allTables = new HashMap<>();
        String filePath = new File("Tables.txt").getAbsolutePath();
        tableFile = new File(filePath);
        if(!tableFile.exists()){
            throw new FileNotFoundException("Error: Tables File not found!");
        }
        if(!tableFile.canRead() || !tableFile.canWrite()){
            throw new FileNotFoundException("Error: Tables File found but cannot be accessed!");
        }
        String tableFileContent = "";
        Scanner reader;
        try{
            reader = new Scanner(tableFile);
            while(reader.hasNext()){
                tableFileContent += reader.nextLine();
            }
            reader.close();
        }
        catch(IOException e){
            throw new IOException("Error: Cannot read Tables File!");
        }
        if(!tableFileContent.trim().equals("")){
            String parsedTables[] = tableFileContent.split("!");
            for(String tableContent: parsedTables){
                if(tableContent.equals("")){
                    continue;
                }
                Table singleTable = new Table(tableContent);
                allTables.put(singleTable.getName(), singleTable);
            }
        }
        SQL_CLI();
    }



    /*
    Method to write updated/new tables into the Tables.txt file. This method is invoked during SQL create, insert,
    update, and delete commands, but not during select commands.

    @Param no parameters
    @Return no return values
     */
    private void writeTablesToFile() throws IOException{
        String writeToFileStr = "";
        for(Table table: allTables.values()){
            writeToFileStr += "!" + table.getName() + "\n<\n";
            for(Map.Entry<String, String> schema: table.getSchema().entrySet()){
                writeToFileStr += schema.getKey() + " " + schema.getValue() + ";\n";
            }
            writeToFileStr += "%\n" + table.getPrimaryKey() + ";\n^\n";
            for(Map.Entry<String, String> FK: table.getForeignKeys().entrySet()){
                writeToFileStr += FK.getKey() + " " + FK.getValue() + ";\n";
            }
            writeToFileStr += "$\n";
            for(List<String> row: table.getRows().values()){
                writeToFileStr += "#";
                String rowStr = "";
                for(String colVal: row){
                    rowStr += colVal + ",";
                }
                rowStr = rowStr.substring(0, rowStr.length()-1);
                writeToFileStr += rowStr + "\n";
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile));
            writer.append(writeToFileStr);
            writer.close();
        }
        catch(IOException e){
            throw new IOException("Error: Could not write to Tables File!");
        }
    }



    /*
    Method to present user with MySQL CLI that accepts SQL Commands. Based on the user's input, it will invoke either
    the create(), select(), update(), insert(), or delete() method. The SQL CLI will continue looping (continue accepting
    user SQL Commands) until the user inputs "exit", which will then terminate the program.

    @Param no parameters
    @Return no return values
     */
    private void SQL_CLI() throws Exception{
        boolean exitCheck = false;
        while(!exitCheck){
            System.out.println("SQL>");
            String input = InputReader.reader.nextLine();
            input = input.trim().toLowerCase();
            if(input.equals("exit")){
                System.out.println("Program terminated...");
                exitCheck = true;
                continue;
            }
            String SQLStatementType = input.split(" ")[0];
            if(SQLStatementType.equals("select")){
                String selectRes = select(input);
                String logStr = "User: ["+LogsInterface.currentUser+"]\nCommand: ["+input+"]\nResult: ["+selectRes+"]";
                LogsInterface.updateLog(logStr, "SQL Select");
            }
            else if(SQLStatementType.equals("insert")){
                String insertRes = insert(input);
                String logStr = "User: ["+LogsInterface.currentUser+"]\nCommand: ["+input+"]\nResult: ["+insertRes+"]";
                LogsInterface.updateLog(logStr, "SQL Insert");
            }
            else if(SQLStatementType.equals("delete")){
                String deleteRes = delete(input);
                String logStr = "User: ["+LogsInterface.currentUser+"]\nCommand: ["+input+"]\nResult: ["+deleteRes+"]";
                LogsInterface.updateLog(logStr, "SQL Delete");
            }
            else if(SQLStatementType.equals("update")){
                String updateRes = update(input);
                String logStr = "User: ["+LogsInterface.currentUser+"]\nCommand: ["+input+"]\nResult: ["+updateRes+"]";
                LogsInterface.updateLog(logStr, "SQL Update");
            }
            else if(SQLStatementType.equals("create")){
                String createRes = create(input);
                String logStr = "User: ["+LogsInterface.currentUser+"]\nCommand: ["+input+"]\nResult: ["+createRes+"]";
                LogsInterface.updateLog(logStr, "SQL Create Table");
            }
            else{
                System.out.println("SQL Syntax Error: ["+SQLStatementType+"] is not a recognized command!");
            }
        }
    }



    /*
    Method to select and display data based on the user's select command. It accepts the following select formats:
    -select * from [table];
    -select [comma-separated attribute names] from [table];
    -select * from [table] where [attribute name] = [where value];
    -select [comma-separated attribute names] from [table] where [attribute name] = [where value];
    Note that since this is a light-weight prototype DBMS, only one where clause is accepted during select statements.

    @Param String sqlCommand --> the select command the user inputted
    @Return String res --> the result of that select command
     */
    private String select(String sqlCommand){
        String res = "";
        sqlCommand = sqlCommand.replaceAll("  "," ");
        //select * from [table];
        Pattern pattern1 = Pattern.compile("select\\s+\\*\\s+from\\s+[A-Za-z0-9]+;", Pattern.CASE_INSENSITIVE);
        //select * from [table] where [attribute name] = [where value];
        Pattern pattern2 = Pattern.compile("select\\s+\\*\\s+from\\s+[A-Za-z0-9]+\\s+where\\s+[A-Za-z0-9]+=[A-Za-z0-9.\"]+;", Pattern.CASE_INSENSITIVE);
        //select [comma-separated attribute names] from [table];
        Pattern pattern3 = Pattern.compile("select\\s+[A-Za-z0-9,\\s]+\\s+from\\s+[A-Za-z0-9]+;", Pattern.CASE_INSENSITIVE);
        //select [comma-separated attribute names] from [table] where [attribute name] = [where value];
        Pattern pattern4 = Pattern.compile("select\\s+[A-Za-z0-9,\\s]+\\s+from\\s+[A-Za-z0-9]+ where\\s+[A-Za-z0-9]+=[A-Za-z0-9.\"]+;", Pattern.CASE_INSENSITIVE);
        if(!pattern1.matcher(sqlCommand).matches() && !pattern2.matcher(sqlCommand).matches() && !pattern3.matcher(sqlCommand).matches() && !pattern4.matcher(sqlCommand).matches()){
            System.out.println("SQL Select Syntax Error!");
            return "SQL Select Syntax Error!";
        }
        String selectedTable;
        if(sqlCommand.contains("where")){
            selectedTable = sqlCommand.split("from ")[1].split(" where")[0];
        }
        else{
            selectedTable = sqlCommand.split("from ")[1].split(";")[0];
        }
        if(!allTables.containsKey(selectedTable)){
            System.out.println("Select Error: Table ["+selectedTable+"] does not exist!");
            return "Select Error: Table ["+selectedTable+"] does not exist!";
        }
        Table targetTable = allTables.get(selectedTable);
        List<List<String>> targetRows = new ArrayList<>();
        if(sqlCommand.contains("where")){
            String whereClause = sqlCommand.split("where ")[1];
            String whereAttr = whereClause.split("=")[0];
            String whereVal = whereClause.split("=")[1].replace(";","");
            if(!targetTable.getSchema().containsKey(whereAttr)){
                System.out.println("Select Error: Table [" + targetTable.getName() + "] does not contain attribute ["+whereAttr+"]!");
                return "Select Error: Table [" + targetTable.getName() + "] does not contain attribute ["+whereAttr+"]!";
            }
            int whereAttIndex = 0;
            for(String schemaAtt: targetTable.getSchema().keySet()){
                if(schemaAtt.equals(whereAttr)){
                    break;
                }
                whereAttIndex++;
            }
            for(List<String> row: targetTable.getRows().values()){
                if(row.get(whereAttIndex).equals(whereVal)){
                    targetRows.add(row);
                }
            }
        }
        else{
            for(List<String> row: targetTable.getRows().values()){
                targetRows.add(row);
            }
        }
        if(sqlCommand.contains("*")){
            for(String schemaAtt: targetTable.getSchema().keySet()){
                res += schemaAtt + " || ";
            }
            res += "\n";
            if(!targetRows.isEmpty()){
                for(List<String> singleRow: targetRows){
                    for(String rowVal: singleRow){
                        res += rowVal + " || ";
                    }
                    res += "\n";
                }
            }
        }
        else{
            String selectedAtts[] = sqlCommand.split("select ")[1].split(" from")[0].split(",");
            List<Integer> attIndexes = new ArrayList<>();
            for(String selectedAtt: selectedAtts){
                selectedAtt = selectedAtt.trim();
                boolean attExists = false;
                int schemaAttIndex = 0;
                for(String schemaAtt: targetTable.getSchema().keySet()){
                    if(schemaAtt.equals(selectedAtt)){
                        attExists = true;
                        attIndexes.add(schemaAttIndex);
                        res += selectedAtt + " || ";
                    }
                    schemaAttIndex++;
                }
                if(!attExists){
                    System.out.println("Select Error: Attribute ["+selectedAtt+"] does not exist in table ["+targetTable.getName()+"]!");
                    return "Select Error: Attribute ["+selectedAtt+"] does not exist in table ["+targetTable.getName()+"]!";
                }
            }
            res += "\n";
            if(!targetRows.isEmpty()){
                for(List<String> rowValues: targetRows){
                    for(int index: attIndexes){
                        res += rowValues.get(index) + " || ";
                    }
                    res += "\n";
                }
            }
        }
        System.out.println(res);
        return res;
    }


    /*
    Method to insert values into a table. The method accepts the following insert format:
    -insert into [table] values([comma-separated values]);
    Note that since this is a light-weight prototype, the user must enter a value for all attributes defined in the table's schema (i.e., they cannot enter
    just some of the attributes, they must enter all).
    Additionally, before inserting the values into the table, each value is checked against the datatype defined in the table's schema. If one or more values do
    not match the datatype defined, then the row will not be inserted and the appropriate error message is displayed to the user.
    Furthermore, before inserting the values into the table, the primary key of the new row is checked against the existing rows' primary keys to check if it is
    a duplicate. If so, then the row will not be inserted and the appropriate error message is displayed to the user.

    Finally, if the row is successfully inserted, the corresponding table's Table object is updated, and the method writes the new row into the Tables.txt file.

    @Param String sqlCommand --> the insert statement the user inputted
    @Return String res --> the result of the command
     */
    private String insert(String sqlCommand) throws Exception{
        sqlCommand = sqlCommand.replaceAll("  ","");
        Pattern insertPattern = Pattern.compile("insert\\s+into\\s+[A-Za-z0-9]+\\s+values\\s?\\(([A-Za-z0-9.\"\\s+]+?,?)+\\);", Pattern.CASE_INSENSITIVE);
        if(!insertPattern.matcher(sqlCommand).matches()){
            System.out.println("SQL Insert Syntax Error!");
            return "SQL Insert Syntax Error!";
        }
        String insertTable = sqlCommand.split("into")[1].split("values")[0].trim();
        if(!allTables.containsKey(insertTable)){
            System.out.println("SQL Insert Error: Table [" + insertTable + "] does not exist!");
            return "SQL Insert Error: Table [" + insertTable + "] does not exist!";
        }
        Map<String, String> schema = allTables.get(insertTable).getSchema();
        String insertValues[] = sqlCommand.split("\\(")[1].split("\\)")[0].split(",");
        if(schema.size()!=insertValues.length){
            System.out.println("SQL Insert Error: Table rows require ["+schema.size()+"] values, but statement supplied [" + insertValues.length + "] values!");
            return "SQL Insert Error: Table rows require ["+schema.size()+"] values, but statement supplied [" + insertValues.length + "] values!";
        }
        int indexCounter = 0;
        String FK_Att = null;
        String FK_Table = null;
        for(Map.Entry<String, String> FK: allTables.get(insertTable).getForeignKeys().entrySet()){
            FK_Att = FK.getKey();
            FK_Table = FK.getValue().split("\\.")[0];
        }
        for(Map.Entry<String, String> schemaAtt: schema.entrySet()){
            insertValues[indexCounter] = insertValues[indexCounter].trim();
            if(schemaAtt.getValue().equals("int")){
                try{
                    Integer.parseInt(insertValues[indexCounter]);
                }
                catch(Exception e){
                    System.out.println("SQL Insert Error: Table [" + insertTable + "] schema defines ["+schemaAtt.getKey()+"] as int, but value supplied is not int!");
                    return "SQL Insert Error: Table [" + insertTable + "] schema defines ["+schemaAtt.getKey()+"] as int, but value supplied is not int!";
                }
            }
            else if(schemaAtt.getValue().equals("decimal")){
                try{
                    Float decimalCheck = Float.parseFloat(insertValues[indexCounter]);
                    String valStr = String.valueOf(decimalCheck);
                    insertValues[indexCounter] = valStr;
                }
                catch(Exception e){
                    System.out.println("SQL Insert Error: Table [" + insertTable + "] schema defines ["+schemaAtt.getKey()+"] as decimal, but value supplied is not decimal!");
                    return "SQL Insert Error: Table [" + insertTable + "] schema defines ["+schemaAtt.getKey()+"] as decimal, but value supplied is not decimal!";
                }
            }
            else if(schemaAtt.getValue().equals("string")){
                Pattern stringCheck = Pattern.compile("\"[A-Za-z0-9]+\"", Pattern.CASE_INSENSITIVE);
                if(!stringCheck.matcher(insertValues[indexCounter]).matches()){
                    System.out.println("SQL Insert Error: Table [" + insertTable + "] schema defines ["+schemaAtt.getKey()+"] as String, but value supplied is not String!");
                    return "SQL Insert Error: Table [" + insertTable + "] schema defines ["+schemaAtt.getKey()+"] as String, but value supplied is not String!";
                }
            }
            else if(schemaAtt.getValue().equals("boolean")){
                if(!insertValues[indexCounter].equals("true") && !insertValues[indexCounter].equals("false")){
                    System.out.println("SQL Insert Error: Table [" + insertTable + "] schema defines ["+schemaAtt.getKey()+"] as boolean, but value supplied is not boolean!");
                    return "SQL Insert Error: Table [" + insertTable + "] schema defines ["+schemaAtt.getKey()+"] as boolean, but value supplied is not boolean!";
                }
            }
            indexCounter++;
        }
        if(allTables.get(insertTable).getRows().containsKey(insertValues[allTables.get(insertTable).getPK_Index()])){
            System.out.println("SQL Insert Error: Primary key value [" + insertValues[allTables.get(insertTable).getPK_Index()] + "] already exists in table [" + insertTable + "]!");
            return "SQL Insert Error: Primary key value [" + insertValues[allTables.get(insertTable).getPK_Index()] + "] already exists in table [" + insertTable + "]!";
        }
        if(!allTables.get(insertTable).getForeignKeys().isEmpty()){
            int FK_Index = 0;
            for(String schemaAtt: allTables.get(insertTable).getSchema().keySet()){
                if(schemaAtt.equals(FK_Att)){
                    break;
                }
                else{
                    FK_Index++;
                }
            }
            if(!allTables.get(FK_Table).getRows().containsKey(insertValues[FK_Index])){
                System.out.println("SQL Insert Error: Foreign key value [" + insertValues[FK_Index] + "] is not a primary key in the reference table [" + FK_Table +"]!");
                return "SQL Insert Error: Foreign key value [" + insertValues[FK_Index] + "] is not a primary key in the reference table [" + FK_Table +"]!";
            }
        }
        allTables.get(insertTable).getRows().put(insertValues[allTables.get(insertTable).getPK_Index()], Arrays.asList(insertValues));
        System.out.println("Values inserted successfully into table [" + insertTable + "]!");
        writeTablesToFile();
        return "Values inserted successfully into table [" + insertTable + "]!";
    }


    /*
    Method to delete row(s) from an existing table. The method accepts the following delete formats:
    -delete from [table];
    -delete from [table] where [attribute name] = [where value];
    Note that since this is a light-weight prototype DBMS, only one where clause is accepted during delete statements.

    Before deleting any rows, the method first checks whether deleting the row would cause a foreign key error. In other words, if one or more rows
    targeted for deletion have another table referencing it as a foreign key, then no deletions will occur and the user is displayed with an appropriate error message.
    For example, if the row {1,1.0,"s"} is targeted for deletion where "s" is a FK being referenced by another table's row {true,"s",5}, then the deletion is rejected.

    Finally, if the deletion is successful, the table's Table object is updated and the method will write to the Tables.txt file to reflect changes.

    @Param String sqlCommand --> delete statement user inputted
    @Return String res --> outcome of that delete statement
     */
    private String delete(String sqlCommand) throws Exception{
        sqlCommand.replaceAll("  ", " ");
        //delete from [table];
        Pattern pattern1 = Pattern.compile("delete\\s+from\\s+[A-Za-z0-9]+\\s+where?\\s+[A-Za-z0-9]+=[A-Za-z0-9.\"]+;", Pattern.CASE_INSENSITIVE);
        //delete from [table] where [attribute name] = [where value];
        Pattern pattern2 = Pattern.compile("delete\\s+from\\s+[A-Za-z0-9]+;", Pattern.CASE_INSENSITIVE);
        if(!pattern1.matcher(sqlCommand).matches() && !pattern2.matcher(sqlCommand).matches()){
            System.out.println("SQL Delete Syntax Error");
            return "SQL Delete Syntax Error";
        }
        String deleteTable;
        if(sqlCommand.contains("where")){
            deleteTable = sqlCommand.split("from")[1].split("where")[0].trim();
        }
        else{
            deleteTable = sqlCommand.split("from")[1].split(";")[0].trim();
        }
        if(!allTables.containsKey(deleteTable)){
            System.out.println("SQL Delete Error: Table ["+deleteTable+"] does not exist!");
            return "SQL Delete Error: Table ["+deleteTable+"] does not exist!";
        }
        Table DT = allTables.get(deleteTable);
        List<String> deleteRowsKey = new ArrayList<>();
        if(sqlCommand.contains("where")){
            String whereAtt = sqlCommand.split("where")[1].split("=")[0].trim();
            if(!DT.getSchema().containsKey(whereAtt)){
                System.out.println("SQL Delete Error: Table [" + deleteTable + "] does not contain attribute ["+whereAtt+"]!");
                return "SQL Delete Error: Table [" + deleteTable + "] does not contain attribute ["+whereAtt+"]!";
            }
            int whereIndex = 0;
            for(String schemaAtt: DT.getSchema().keySet()){
                if(schemaAtt.equals(whereAtt)){
                    break;
                }
                whereIndex++;
            }
            String whereCheck = sqlCommand.split("=")[1].split(";")[0].trim();
            for(Map.Entry<String, List<String>> row: DT.getRows().entrySet()){
                if(row.getValue().get(whereIndex).equals(whereCheck)){
                    deleteRowsKey.add(row.getKey());
                }
            }
        }
        else{
            deleteRowsKey.addAll(DT.getRows().keySet());
        }
        if(deleteRowsKey.isEmpty()){
            System.out.println("SQL Delete Error: No rows affected! Either table ["+deleteTable+"] is empty, or the where clause affects no existing rows in that table!");
            return "SQL Delete Error: No rows affected! Either table ["+deleteTable+"] is empty, or the where clause affects no existing rows in that table!";
        }
        for(Table FK_Table: allTables.values()){
            for(Map.Entry<String, String> FKs: FK_Table.getForeignKeys().entrySet()){
                String referenceTable = FKs.getValue().split("\\.")[0].trim();
                if(!referenceTable.equals(deleteTable)){
                    continue;
                }
                String FKAtt = FKs.getKey();
                int FK_Index = 0;
                for(String schemaAtt: FK_Table.getSchema().keySet()){
                    if(schemaAtt.equals(FKAtt)){
                        break;
                    }
                    FK_Index++;
                }
                for(List<String> FK_Rows: FK_Table.getRows().values()){
                    if(deleteRowsKey.contains(FK_Rows.get(FK_Index))){
                        System.out.println("SQL Delete Error: Cannot delete row(s) because the table [" + FK_Table.getName() +"] has a foreign key reference to [" + deleteTable + "]'s row {" + DT.getRows().get(FK_Rows.get(FK_Index)) + "}!");
                        return "SQL Delete Error: Cannot delete row(s) because the table [" + FK_Table.getName() +"] has a foreign key reference to [" + deleteTable + "]'s row {" + DT.getRows().get(FK_Rows.get(FK_Index)) + "}!";
                    }
                }
            }
        }
        int numberOfDeletedRows = 0;
        for(String deleteRowPK: deleteRowsKey){
            DT.getRows().remove(deleteRowPK);
            numberOfDeletedRows++;
        }
        writeTablesToFile();
        System.out.println("Successfully deleted " + numberOfDeletedRows + " row(s) from table [" + deleteTable + "]!");
        return "Successfully deleted " + numberOfDeletedRows + " row(s) from table [" + deleteTable + "]!";
    }



    /*
    Method to update values in an existing table. The method accepts the following update formats:
    -update [table] set [comma-separated set clauses];
    -update [table] set [comma-separated set clauses] where [attribute name] = [where value];
    Note that since this is a light-weight prototype DBMS, only one where clause is accepted during update statements.

    Before applying any updates, the new values are checked against the data type of the attribute being updated in the table's schema. If one or more values do
    not match the defined data type, the update statement is rejected and an appropriate error message is displayed to the user.
    Additionally, the method checks whether updating the values specified by the user will create duplicate primary keys. If so, the update statement is
    rejected and an appropriate error message is displayed to the user.
    Furthermore, the method checks whether updating the row(s) would cause a foreign key error. If it would create such an error,
    then the update statement is rejected and the user is displayed with an appropriate error message.

    Upon successful updates, the method will update the table's Table object and write to the Tables.txt file to reflect changes.

    @Param String sqlCommand --> update command user inputted
    @Return String res --> result of that command
     */
    private String update(String sqlCommand) throws Exception{
        sqlCommand = sqlCommand.replaceAll("  "," ");
        //without where clause
        Pattern pattern1 = Pattern.compile("update\\s+[A-Za-z0-9]+\\s+set\\s+([A-Za-z0-9\\s*]+=[A-Za-z0-9.\"\\s*]+,?)+;", Pattern.CASE_INSENSITIVE);
        //with where clause
        Pattern pattern2 = Pattern.compile("update\\s+[A-Za-z0-9]+\\s+set\\s+([A-Za-z0-9\\s*]+=[A-Za-z0-9.\"\\s*]+,?)+\\s+where\\s+[A-Za-z0-9\\s*]+=[A-Za-z0-9.\"\\s*]+;", Pattern.CASE_INSENSITIVE);
        if(!pattern1.matcher(sqlCommand).matches() && !pattern2.matcher(sqlCommand).matches()){
            System.out.println("SQL Update Syntax Error");
            return "SQL Update Syntax Error";
        }
        String tableName = sqlCommand.split("update")[1].split("set")[0].trim();
        Table updateTable;
        if(!allTables.containsKey(tableName)){
            System.out.println("SQL Update Error: Table [" + tableName + "] does not exist!");
            return "SQL Update Error: Table [" + tableName + "] does not exist!";
        }
        else{
            updateTable = allTables.get(tableName);
        }
        Map<String, String> updateParams = new HashMap<>();
        String updateClauses[];
        if(sqlCommand.contains("where")){
            updateClauses = sqlCommand.split("set")[1].split("where")[0].trim().split(",");
        }
        else{
            updateClauses = sqlCommand.split("set")[1].split(";")[0].trim().split(",");
        }
        for(String clause: updateClauses){
            String updateAtt = clause.split("=")[0].trim();
            String updateVal = clause.split("=")[1].trim();
            if(!updateTable.getSchema().containsKey(updateAtt)){
                System.out.println("SQL Update Error: Table [" + tableName + "] does not contains attribute [" + updateAtt + "]!");
                return "SQL Update Error: Table [" + tableName + "] does not contains attribute [" + updateAtt + "]!";
            }
            if(updateTable.getSchema().get(updateAtt).equals("int")){
                try{
                    int intCheck = Integer.parseInt(updateVal);
                }
                catch(NumberFormatException e){
                    System.out.println("SQL Update Error: Table [" + tableName + "] defines attribute [" + updateAtt + "] as int, but value supplied is not!");
                    return "SQL Update Error: Table [" + tableName + "] defines attribute [" + updateAtt + "] as int, but value supplied is not!";
                }
            }
            else if(updateTable.getSchema().get(updateAtt).equals("decimal")){
                try{
                    float floatCheck = Float.parseFloat(updateVal);
                }
                catch(NumberFormatException e){
                    System.out.println("SQL Update Error: Table [" + tableName + "] defines attribute [" + updateAtt + "] as decimal, but value supplied is not!");
                    return "SQL Update Error: Table [" + tableName + "] defines attribute [" + updateAtt + "] as decimal, but value supplied is not!";
                }
                if(!updateVal.contains(".")){
                    updateVal += ".0";
                }
            }
            else if(updateTable.getSchema().get(updateAtt).equals("boolean")){
                if(!updateVal.equals("true") && ! updateVal.equals("false")){
                    System.out.println("SQL Update Error: Table [" + tableName + "] defines attribute [" + updateAtt + "] as boolean, but value supplied is not!");
                    return "SQL Update Error: Table [" + tableName + "] defines attribute [" + updateAtt + "] as boolean, but value supplied is not!";
                }
            }
            else if(updateTable.getSchema().get(updateAtt).equals("string")){
                Pattern stringCheck = Pattern.compile("\"[A-Za-z0-9]+\"", Pattern.CASE_INSENSITIVE);
                if(!stringCheck.matcher(updateVal).matches()){
                    System.out.println("SQL Update Error: Table [" + tableName + "] defines attribute [" + updateAtt + "] as string, but value supplied is not!");
                    return "SQL Update Error: Table [" + tableName + "] defines attribute [" + updateAtt + "] as string, but value supplied is not!";
                }
            }
            updateParams.put(updateAtt, updateVal);
        }
        List<String> updateRows = new ArrayList<>();
        if(sqlCommand.contains("where")){
            String whereAtt = sqlCommand.split("where")[1].split("=")[0].trim();
            String whereVal = sqlCommand.split("where")[1].split("=")[1].split(";")[0].trim();
            if(!updateTable.getSchema().containsKey(whereAtt)){
                System.out.println("SQL Update Error: Table [" + tableName + "] does not contain attribute [" + whereAtt + "]!");
                return "SQL Update Error: Table [" + tableName + "] does not contain attribute [" + whereAtt + "]!";
            }
            int whereIndex = 0;
            for(String schemaAtt: updateTable.getSchema().keySet()){
                if(schemaAtt.equals(whereAtt)){
                    break;
                }
                whereIndex++;
            }
            for(Map.Entry<String, List<String>> row: updateTable.getRows().entrySet()){
                if(row.getValue().get(whereIndex).equals(whereVal)){
                    updateRows.add(row.getKey());
                }
            }
        }
        else{
            for(String rowKey: updateTable.getRows().keySet()){
                updateRows.add(rowKey);
            }
        }
        if(updateRows.isEmpty()){
            System.out.println("SQL Update Error: No rows affected! Either table ["+tableName+"] is empty, or the where clause affects no existing rows in that table!");
            return "SQL Update Error: No rows affected! Either table ["+tableName+"] is empty, or the where clause affects no existing rows in that table!";
        }
        for(String updateAtt: updateParams.keySet()){
            if(updateTable.getPrimaryKey().equals(updateAtt)){
                for(Table FK_Tab: allTables.values()){
                    for(Map.Entry<String, String> FKs: FK_Tab.getForeignKeys().entrySet()){
                        String referenceTable = FKs.getValue().split("\\.")[0].trim();
                        if(!referenceTable.equals(updateTable.getName())){
                            continue;
                        }
                        String FKAtt = FKs.getKey();
                        int FK_Index = 0;
                        for(String schemaAtt: FK_Tab.getSchema().keySet()){
                            if(schemaAtt.equals(FKAtt)){
                                break;
                            }
                            FK_Index++;
                        }
                        for(List<String> FK_Rows: FK_Tab.getRows().values()){
                            if(updateRows.contains(FK_Rows.get(FK_Index)) && !FK_Rows.get(FK_Index).equals(updateParams.get(updateAtt))){
                                System.out.println("SQL Update Error: Attribute [" + updateAtt + "] in one or more rows cannot be updated because table [" + FK_Tab.getName() + "] has a foreign key reference to the current values!");
                                return "SQL Update Error: Attribute [" + updateAtt + "] in one or more rows cannot be updated because table [" + FK_Tab.getName() + "] has a foreign key reference to the current values!";
                            }
                        }
                    }
                }
            }
        }
        Map<String, List<String>> postUpdateRows = new HashMap<>();
        List<String> updateAttributes = new ArrayList<>();
        updateAttributes.addAll(updateParams.keySet());
        int updateCounter = 0;
        for(Map.Entry<String, List<String>> currentRow: updateTable.getRows().entrySet()){
            if(!updateRows.contains(currentRow.getKey())){
                if(postUpdateRows.containsKey(currentRow.getKey())){
                    System.out.println("SQL Update Error: Update will create duplicate primary keys for table [" + tableName + "]'s PK attribute [" + updateTable.getPrimaryKey() + "]!");
                    return "SQL Update Error: Update will create duplicate primary keys for table [" + tableName + "]'s PK attribute [" + updateTable.getPrimaryKey() + "]!";
                }
                postUpdateRows.put(currentRow.getKey(), currentRow.getValue());
            }
            else{
                List<String> updatedRow = currentRow.getValue();
                String updatedRowKey = currentRow.getKey();
                for(String updateKey: updateAttributes){
                    int updateIndex = 0;
                    for(String schemaAtt: updateTable.getSchema().keySet()){
                        if(updateKey.equals(schemaAtt)){
                            break;
                        }
                        updateIndex++;
                    }
                    updatedRow.set(updateIndex, updateParams.get(updateKey));
                    if(updateKey.equals(updateTable.getPrimaryKey())){
                        updatedRowKey = updateParams.get(updateKey);
                    }
                }
                if(postUpdateRows.containsKey(updatedRowKey)){
                    System.out.println("SQL Update Error: Update will create duplicate primary keys for table [" + tableName + "]'s PK attribute [" + updateTable.getPrimaryKey() + "]!");
                    return "SQL Update Error: Update will create duplicate primary keys for table [" + tableName + "]'s PK attribute [" + updateTable.getPrimaryKey() + "]!";
                }
                postUpdateRows.put(updatedRowKey, updatedRow);
                updateCounter++;
            }
        }
        updateTable.setRows(postUpdateRows);
        writeTablesToFile();
        System.out.println("Successfully updated " + updateCounter + " row(s) of table [" + tableName + "]!");
        return "Successfully updated " + updateCounter + " row(s) of table [" + tableName + "]!";
    }


    /*Method to create new tables. The method accepts the following create format:
    -create table [table name] ([comma-separated attribute names and data type], primary key ([attribute]));
    -create table [table name] ([comma-separated attribute names and data type], primary key ([attribute]), foreign key ([attribute]) reference [table]([attribute]));
    Note that since this is a light-weight prototype, a table can have only one primary key and up to one foreign key. Additionally, the foreign key must refer to table's
    primary key.

    Additionally, this program only accepts the following data types when defining attributes:
    -String
    -Int
    -Decimal
    -Boolean

    @Param String sqlCommand --> create statement user inputted
    @Return String res --> result of that statement
     */
    private String create(String sqlCommand) throws Exception{
        sqlCommand = sqlCommand.replaceAll("  "," ");
        Pattern createPattern = Pattern.compile("create\\s+table\\s+[A-Za-z0-9]+\\s?\\((\\s+[A-Za-z0-9]+\\s+[A-Za-z0-9]+,)+\\s+primary\\s?key\\s?\\([A-Za-z0-9]+\\)(\\s?,\\s+foreign key\\s+\\([A-Za-z0-9]+\\)\\s+references\\s+[A-Za-z0-9]+\\s?\\([A-Za-z0-9]+\\))?\\s?\\)\\s?;", Pattern.CASE_INSENSITIVE);
        if(!createPattern.matcher(sqlCommand).matches()){
            System.out.println("SQL Create Syntax Error!");
            return "SQL Create Syntax Error!";
        }
        String tableName = sqlCommand.split("table ")[1].split("\\(")[0].replace(" ","");
        if(allTables.containsKey(tableName)){
            System.out.println("SQL Create Error: Table ["+tableName+"] already exists!");
            return "SQL Create Error: Table ["+tableName+"] already exists!";
        }
        Map<String, String> schema = new HashMap<>();
        String attributes[] = sqlCommand.split("\\(")[1].split(" primary")[0].split(",");
        for(String attribute: attributes){
            attribute = attribute.trim();
            String attName = attribute.split(" ")[0];
            String attType = attribute.split(" ")[1];
            if(schema.containsKey(attName)){
                System.out.println("SQL Create Error: Duplicate Table Attribute Definitions ["+attName+"]!");
                return "SQL Create Error: Duplicate Table Attribute Definitions!";
            }
            if(!attType.equals("int") && !attType.equals("decimal") && !attType.equals("string") && !attType.equals("boolean")){
                System.out.println("SQL Create Error: Attribute type ["+attType+"] for attribute ["+attName+"] not recognized!");
                return "SQL Create Error: Attribute type ["+attType+"] for attribute ["+attName+"] not recognized!";
            }
            schema.put(attName, attType);
        }
        String PK = sqlCommand.split("primary key")[1];
        PK = PK.replaceAll(" ","").trim();
        PK = PK.split("\\(")[1].split("\\)")[0];
        if(!schema.containsKey(PK)){
            System.out.println("SQL Create Error: Primary Key ["+PK+"] is not a defined attribute in the table being created!");
            return "SQL Create Error: Primary Key ["+PK+"] is not a defined attribute in the table being created!";
        }
        //Only 1 FK per table since each table only has 1 PK, and FK must reference a PK
        String FK_Str = null;
        if(sqlCommand.contains("foreign key")){
            String FK = sqlCommand.split("foreign key")[1];
            String FK_Att = FK.split("\\(")[1].split("\\)")[0];
            if(!schema.containsKey(PK)) {
                System.out.println("SQL Create Error: Foreign Key [" + FK_Att + "] is not a defined attribute in the table being created!");
                return "SQL Create Error: Foreign Key [" + FK_Att + "] is not a defined attribute in the table being created!";
            }
            String FK_Table = FK.split("references ")[1].split("\\(")[0].replaceAll(" ","");
            if(!allTables.containsKey(FK_Table)){
                System.out.println("SQL Create Error: Foreign key reference table ["+FK_Table+"] does not exist!");
                return "SQL Create Error: Foreign key reference table ["+FK_Table+"] does not exist!";
            }
            String FK_ReferenceAtt = FK.split(FK_Table)[1].split("\\)")[0].replaceAll("\\(", "").replaceAll(" ","");
            if(!allTables.get(FK_Table).getPrimaryKey().equals(FK_ReferenceAtt)){
                System.out.println("SQL Create Error: Foreign key reference attribute ["+FK_ReferenceAtt+"] is not a primary key in reference table ["+FK_Table+"]!");
                return "SQL Create Error: Foreign key reference attribute ["+FK_ReferenceAtt+"] is not a primary key in reference table ["+FK_Table+"]!";
            }
            if(!schema.get(FK_Att).equals(allTables.get(FK_Table).getSchema().get(FK_ReferenceAtt))){
                System.out.println("SQL Create Error: Foreign key [" + FK_Att +"]'s type ["+schema.get(FK_Att)+"] does not match the referenced attribute [" + FK_ReferenceAtt + "]'s type of [" + allTables.get(FK_Table).getSchema().get(FK_ReferenceAtt) + "]!");
                return "SQL Create Error: Foreign key [" + FK_Att +"]'s type ["+schema.get(FK_Att)+"] does not match the referenced attribute [" + FK_ReferenceAtt + "]'s type of [" + allTables.get(FK_Table).getSchema().get(FK_ReferenceAtt) + "]!";
            }
            FK_Str = FK_Att + " " + FK_Table + "." + FK_ReferenceAtt + ";";
        }
        String tableContentStr = tableName+"<";
        for(Map.Entry<String, String> schemaAtt: schema.entrySet()){
            tableContentStr += schemaAtt.getKey()+" "+schemaAtt.getValue()+";";
        }
        tableContentStr += "%" + PK + ";^";
        if(FK_Str!=null){
            tableContentStr += FK_Str;
        }
        tableContentStr += "$";
        Table createdTable = new Table(tableContentStr);
        allTables.put(tableName, createdTable);
        writeTablesToFile();
        System.out.println("Table [" + tableName +"] was created successfully!");
        return "Table [" + tableName +"] was created successfully!";
    }
}
