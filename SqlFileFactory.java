/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By   Description
---  -------------  --------------  -----------  ----------   -------------------------------------
1.1  2019-09-06     xlzhu@ips.com                             common classs for Sqlfile to hiding difference

*/

import java.util.HashMap;
import java.util.Map;

public class SqlFileFactory{
  private static SqlFileFactory factory = new SqlFileFactory();
  private SqlFileFactory(){
  }
  private static Map<String,DataSource> sqlFileMap = new HashMap<String,DataSource>();
  static{
    sqlFileMap.put(Database.MYSQL.toString(), new MySqlFile());
    //sqlFileMap.put(Database.ORACLE.toString(), new OracleFile());
    //add your code here
	
  }
  public DataSource creator(String pDbType){
    return sqlFileMap.get(pDbType);
  }
  public static SqlFileFactory getInstance(){
    return factory;
  }
}