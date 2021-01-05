/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By   Description
---  -------------  --------------  -----------  ----------   -------------------------------------
1.1  2019-08-29     xlzhu@ips.com                             common classs for SqlVisitor to hiding difference

*/

import java.util.HashMap;
import java.util.Map;

public class SqlVisitorFactory{
  private static SqlVisitorFactory factory = new SqlVisitorFactory();
  private SqlVisitorFactory(){
  }
  private static Map<String,SqlVisitor> visitorMap = new HashMap<String,SqlVisitor>();
  static{
    visitorMap.put(Database.MYSQL.toString(), new MySqlVisitor());
    //visitorMap.put(Database.ORACLE.toString(), new OracleVisitor());
    //add your code here
	
  }
  public SqlVisitor creator(String pDbType){
    return visitorMap.get(pDbType);
  }
  public static SqlVisitorFactory getInstance(){
    return factory;
  }	
}