import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
This class encapsulates the data of a table. In other words, each table in the Tables.txt file is stored as
an instance of this class. In other words, tables stored in the Tables.txt file are fetched then parsed to extract their
meta-data and end-user data, which is then stored in an instance of this class. This is done so that
other classes have easier access to tables' data.

@Author: Waleed R. Alhindi (B00919848)
 */

public class Table {

    //Table's name
    private String name;

    //Table's schema where map's key is the attribute's name and the value is its time (Example: <"A1","Decimal">)
    private Map<String, String> schema;

    //Table's row values where map's key is the primary key value of that row and the value is a list of the row's values
    //For example, the row {1,1.0,"s",true} where "s" is the primary key is stored in the map as <"s",[1,1.0,"s",true]>
    private Map<String, List<String>> rows;

    //Table's primary key. Note that since this is a light-weight prototype DBMS, each table can only have 1 primary key
    private String primaryKey;

    //The index of the primary key in the row according to the schema. For example, if the schema defines rows
    //as {A1, A2, A3}, where A2 is the primary key, then PK_Index would be 1 since that would be its index in the
    //rows variable's List<String>
    private int PK_Index;

    //Table's foreign keys (if they exist). Foreign keys are stored as a map where the key is the foreign key attribute's
    //name and the value is the table and attribute that foreign key references
    //For example, if A1 references T2(B1), then it would be stored as <A1, T2.B1> in this map
    private Map<String, String> foreignKeys;

    /*
    Table constructor that is fed the information of one table from the Tables.txt file and parses that string
    based on the custom delimiters to extract the table's data and store in the instance's variables.

    @Param String tableContent --> the string of a single table fetched from the Tables.txt file
    @Return no return since it is a constructor
     */
    Table(String tableContent){
        name = tableContent.split("<")[0];
        primaryKey = tableContent.split("%")[1].split("\\^")[0].split(";")[0];
        schema = new HashMap<>();
        String parsedSchema[] = tableContent.split("<")[1].split("%")[0].split(";");
        int indexCounter = 0;
        for(String schemaAtt: parsedSchema){
            schema.put(schemaAtt.split(" ")[0], schemaAtt.split(" ")[1]);
            if(primaryKey.equals(schemaAtt.split(" ")[0])){
                PK_Index = indexCounter;
            }
            else{
                indexCounter++;
            }
        }
        foreignKeys = new HashMap<>();
        rows = new HashMap<>();
        String FKCheck[] = tableContent.split("\\^");
        if(!FKCheck[1].equals("$")){
            String parsedFKs = tableContent.split("\\^")[1].split("\\$")[0];
            foreignKeys = new HashMap<>();
            if(!parsedFKs.equals("")){
                String splitFKs[] = parsedFKs.split(";");
                for(String FK: splitFKs){
                    foreignKeys.put(FK.split(" ")[0], FK.split(" ")[1]);
                }
            }
        }
        rows = new HashMap<>();
        if(tableContent.contains("#")){
            String parsedRows[] = tableContent.split("\\$")[1].split("#");
            for(String row: parsedRows){
                if(row.equals("")){
                    continue;
                }
                String rowFields[] = row.split(",");
                for(int i=0; i< rowFields.length; i++){
                    if(i==PK_Index){
                        rows.put(rowFields[i], Arrays.asList(rowFields));
                        break;
                    }
                }
            }
        }
    }


    /*Setters and getters*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, String> schema) {
        this.schema = schema;
    }

    public Map<String, List<String>> getRows() {
        return rows;
    }

    public void setRows(Map<String, List<String>> rows) {
        this.rows = rows;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public int getPK_Index() {
        return PK_Index;
    }

    public void setPK_Index(int PK_Index) {
        this.PK_Index = PK_Index;
    }

    public Map<String, String> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(Map<String, String> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
