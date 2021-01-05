/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By       Description
---  -------------  --------------  -----------  --------------   -------------------------------------
1.1                                 2019-09-06   xlzhu@ips.com    process mysqlfile
*/
import java.util.List;
import java.util.ArrayList;

public class MySqlFile extends DataSource{
  private static final String REGEX_SEMI=";";
  private static final String REGEX_DELIMITER="DELIMITER\\s*";
  private static final String REGEX_DELIMITER_SEMI="DELIMITER\\s*;";
  private static final String REGEX_DELIMITER_$$="DELIMITER\\s*\\W+";
  private String delimiter_$$="";
  private List<String> sqlList;
  private StringBuffer fileContent;
  public MySqlFile(){
  }
  /*private String dropSpecialChar(String pstr){
	String REGEX_CHAR="[A-Z|a-z]+";
	String str=pstr.trim();
	int posStartChar=getDelimiterStartPos(str,REGEX_CHAR);
	return str.substring(posStartChar,str.length());
  }*/
  @Override 
  public List<String> getDataList(String pDataStr,SqlLogging pLogger){
	//Override&customize this function,maybe visit your data from database or other source
	this.fileContent = new StringBuffer(pDataStr);
	this.sqlList=new ArrayList<String>();
	int posSemi,posStartRegexDelimiter$$,posEndRegexDelimiter$$,posStartRegexDelimiterSemi;
	while (this.fileContent.length()>0){
	  posSemi=getDelimiterStartPos(this.fileContent.toString(),REGEX_SEMI);
	  posStartRegexDelimiterSemi=getDelimiterStartPos(this.fileContent.toString(),REGEX_DELIMITER_SEMI);
	  posStartRegexDelimiter$$=getDelimiterStartPos(this.fileContent.toString(),REGEX_DELIMITER_$$);
	  posEndRegexDelimiter$$=getDelimiterEndPos(this.fileContent.toString(),REGEX_DELIMITER_$$);
	  pLogger.log("-- SplittingSqlsByDelimiter;S_POS(;):"+String.valueOf(posSemi)+";S_POS(DELIMITER ;):"+String.valueOf(posStartRegexDelimiterSemi)+";S_POS(DELIMITER $$)"+
	    String.valueOf(posStartRegexDelimiter$$)+";E_POS(DELIMITER $$):"+String.valueOf(posEndRegexDelimiter$$));
	  if (posSemi<0){
		if (!this.fileContent.toString().trim().equals("")){
		  this.sqlList.add(this.fileContent.toString().trim());
		}
	    this.fileContent.delete(0,this.fileContent.length());  
	  }else if (posStartRegexDelimiter$$<0){//only semi;
         if (!this.fileContent.substring(0,posSemi).trim().equals("")){	  
		   this.sqlList.add(this.fileContent.substring(0,posSemi).trim());
		 }
	     this.fileContent.delete(0,posSemi+1).trimToSize();
	  }else{//exist DELIMITER $$
		if (posStartRegexDelimiter$$<posSemi){//first section is DELIMITER $$
		  delimiter_$$=this.fileContent.substring(getDelimiterEndPos(this.fileContent.toString(),REGEX_DELIMITER),posEndRegexDelimiter$$-1);
		  pLogger.log("--DELIMITER $$:"+delimiter_$$);
		  if (!this.fileContent.substring(posEndRegexDelimiter$$,posStartRegexDelimiterSemi).replace(delimiter_$$,"").trim().equals("")){	
		    this.sqlList.add(this.fileContent.substring(posEndRegexDelimiter$$,posStartRegexDelimiterSemi).replace(delimiter_$$,"").trim());
		  }
	      this.fileContent.delete(getDelimiterStartPos(this.fileContent.toString(),REGEX_DELIMITER_$$),getDelimiterEndPos(this.fileContent.toString(),REGEX_DELIMITER_SEMI));
			
		}else{//first section is semi;
		  if (!this.fileContent.substring(0,posSemi).trim().equals("")){
		    this.sqlList.add(this.fileContent.substring(0,posSemi).trim());
		  }
	      this.fileContent.delete(0,posSemi+1).trimToSize();		  
		} 
	  }
	}
	return this.sqlList;
  }
  

}