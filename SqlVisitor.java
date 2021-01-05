/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By   Description
---  -------------  --------------  -----------  ----------   -------------------------------------
1.1  2019-08-29     xlzhu@ips.com                             interface for all dbVisitor

*/
import java.util.Map;
import java.util.List;
import org.antlr.v4.runtime.*;

enum Database{MYSQL,ORACLE,MSSQL};
interface SqlVisitor {
  public List<Map<String,String>> getTableMetaLst(CharStream sqlStream,int order);
}