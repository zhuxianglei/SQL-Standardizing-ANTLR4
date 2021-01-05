/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By       Description
---  -------------  --------------  -----------  --------------   -------------------------------------
1.0  2019-08-26     xlzhu@ips.com                                 for wordStandard
1.1                                 2019-08-29   xlzhu@ips.com    redesign java class
*/
import java.util.ArrayList;
import java.util.List;

public class WordStandard extends DataSource{
  private List<String[]> wordStandardLst;
  private int dbTypeIdx;
  public WordStandard(){
	wordStandardLst=new ArrayList<String[]>();
	for (String[] strArr : getDataList("",null)) {
      wordStandardLst.add(strArr);
    }
  }
  
  @Override 
  protected List<String[]> getDataList(String pDataStr,SqlLogging pLogger){
  //Override&customize this function,maybe visit your data from database or other source
    String conifgKeyWSdFilePath="FILE_WORDSTANDARD";
	String pathFile=getPropValue(conifgKeyWSdFilePath);
	return getCSVDataList(pathFile);
  }
  public List<String[]> getWordList(){
	return wordStandardLst;
  }
  
  private int getDbTypeIdx(){
	return dbTypeIdx;
  }
  
  private void setDbTypeIdx(String dbType){
	for (int i=0;i<wordStandardLst.get(0).length;i++){
	  if (wordStandardLst.get(0)[i].toUpperCase().equals(dbType.toUpperCase())){
		dbTypeIdx=i;
		break;
	  }
	}
  }
  private String purgeStr(String str){
	String strtmp=str.replaceAll("'",""); 
    strtmp=strtmp.replaceAll("`","");  
    return strtmp;	
  }
  public String getSDWord(String Desc){
	String rst="";
	for (String[] strArr:wordStandardLst){
	  if (strArr[2].toUpperCase().equals(Desc.toUpperCase())){
		rst= strArr[1];
		break;
	  }
	} 
    return rst;	
  }
  
  public String getSDTypeLen(String dbType,String Desc){
    String rst="";
	setDbTypeIdx(dbType);
	for (String[] strArr:wordStandardLst){
	  if (strArr[2].toUpperCase().equals(Desc.toUpperCase())){
		rst= strArr[getDbTypeIdx()];
		break;
	  }
	} 
    return rst;		   
  }
  
  public String getSDTypeLenByCol(String dbType,String colName){
    String rst="";
	setDbTypeIdx(dbType);
	for (String[] strArr:wordStandardLst){
	  if (strArr[1].toUpperCase().equals(purgeStr(colName).toUpperCase())){
		rst= strArr[getDbTypeIdx()];
		break;
	  }
	} 
    return rst;		   
  }
}