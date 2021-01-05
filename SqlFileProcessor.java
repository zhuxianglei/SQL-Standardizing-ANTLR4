/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By       Description
---  -------------  --------------  -----------  --------------   -------------------------------------
1.0  2019-08-20     xlzhu@ips.com                                 process sql file
1.1                                 2019-08-29   xlzhu@ips.com    redesign java class

*/
import java.io.File;
import java.lang.Exception;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.antlr.v4.runtime.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import IPSAST.mysql.*;

public class SqlFileProcessor{
  private static final String FILE_ENCODING = "UTF-8"; 
  private static final String PATH_LOGFILE = "PATH_LOGFILE";
  private static final String KEY_SQLSN="SQLSN";        //COLUMN,IDX&KEY,PT_ TRG F_,SP_,PKG_,SEQ_,VW_
  private static final String KEY_CATAGORY="CAT";       //COLUMN,IDX&KEY,PT_ TRG F_,SP_,PKG_,SEQ_,VW_
  private static final String KEY_NAME="NAME";          //COLUMN,IDX&KEY,PT_ TRG F_,SP_,PKG_,SEQ_,VW_ 
  private static final String KEY_TYPELEN="TYPELEN";    //COLUMN 
  private static final String KEY_COMMENT="COMMENT";    //COLUMN 
  private static final String KEY_COLUMNS="COLUMNS";    //      ,IDX&KEY,PT_                 
  private static final String KEY_TABLE="TABLE";        //COLUMN,IDX&KEY,PT_ TRG             
  private static final String KEY_PREFIX="PREFIX";      //      ,IDX&KEY,PT_ TRG F_,SP_,PKG_,SEQ_,VW_  
  private static final String KEY_EVENT="EVENT";        //                   TRG
  private static final String KEY_LOCK="LOCK";          //COLUMN,IDX&KEY,PT_
  private static final String KEY_ALGORITHM="ALGORITHM";//COLUMN,IDX&KEY,PT_
  private static final String KEY_ADVICE="ADVICE";      //COLUMN,IDX&KEY,PT_ TRG F_,SP_,PKG_,SEQ_,VW_
  private static final String KEY_REX="REX_RPC";    //String regluar express which will be replaced
  private static final String KEY_FMT="FMT_RPC";  //String will replacing REGEX_REPLACE
  
  private static String getregex(String str){
	if (!str.equals("")){
	  String stmp=str.replaceAll("\\(","\\\\(");
	  return "(?i)"+stmp.replaceAll("\\)","\\\\)");  
	}else{
	  return str;
	}
  }
  private static String getSpecialFmt(String pStr,String pRegex,String pFmt){
	String strFmt=pFmt;
	Pattern prm = Pattern.compile(pRegex);
    Matcher mrm = prm.matcher(pStr);
	if (mrm.find()){
	  if (pStr.substring(mrm.end()-1,mrm.end()).equals("(")){
		strFmt=strFmt+"(";	
	  }
    }
	return strFmt;
  }
  private static int getIdxMetaLst(List<Map<String,String>> pMetaLst,Map pMeta){
	int i=0;
	int j=-1;//String[6]={"CATATORY","Name(Column)","TypeLength","comment","ParentName(Table),Advice}
	for (Map mp : pMetaLst) {
      if (mp.get(KEY_CATAGORY).equals(pMeta.get(KEY_CATAGORY))&&mp.get(KEY_NAME).equals(pMeta.get(KEY_NAME))&&mp.get(KEY_TABLE).equals(pMeta.get(KEY_TABLE))){
		j=i;
		break;
	  }
	  i++;
      } 
    return j;	  
  }
  public static void main(String[] args) throws Exception {
    String fileLines,strOnLineDDL;
	String dbType=Database.MYSQL.toString();
	boolean isEmpty=true;
	int sqlSN=0;
    List<Map<String,String>> tableMetaLst,newTableMetaLst;//String[4]={"Tablename","Columnname","coldatatypelength","comment"}
	List<String> oriSqlList=new ArrayList<String>();
	List<String> tmpSqlList=new ArrayList<String>();
	//1 init parameters
	ParamConfig paramList =new ParamConfig();
	SqlLogging sLog=new SqlLogging(paramList.getPropValue(PATH_LOGFILE));
	sLog.log("---***startmain***---");	
    //2.read ddl sql file	
    File file = new File(args[0]);  
    Long fileLength = file.length();  
    byte[] fileContent = new byte[fileLength.intValue()];  
    try {  
       BufferedInputStream in = new BufferedInputStream(new FileInputStream(args[0]));  
       in.read(fileContent);  
       in.close();  
      } catch (FileNotFoundException e) {  
         e.printStackTrace();  
      } catch (IOException e) {  
         e.printStackTrace();  
      }  
    try {  
      fileLines= new String(fileContent, FILE_ENCODING);
	  StringBuffer sbFileLines=new StringBuffer(fileLines);
	  tableMetaLst = new ArrayList<Map<String,String>>();
	  newTableMetaLst = new ArrayList<Map<String,String>>();
	  DataSource ds=SqlFileFactory.getInstance().creator(dbType);
	  //3.get metadata of table from AST
	  sqlSN=1;
	  for(Object sqlStr:ds.getDataList(fileLines,sLog)){
		//--get tablemeta list array--
		sLog.log("-- DDL:");
		sLog.log(sqlStr.toString());
		oriSqlList.add(sqlStr.toString());
		tmpSqlList.add(sqlStr.toString());
		CharStream input = CharStreams.fromStream(new ByteArrayInputStream(sqlStr.toString().trim().toUpperCase().getBytes(FILE_ENCODING)));
        SqlVisitor sv=SqlVisitorFactory.getInstance().creator(dbType);
        try{	
          isEmpty=true;		
		  for (Map<String,String> mp : sv.getTableMetaLst(input,sqlSN)) {
			isEmpty=false;
            tableMetaLst.add(mp);
          }
		  if (isEmpty&&sqlStr.toString().length()>0){
			//tmpSqlList.set(tmpSqlList.size()-1,"\r\n/*IPS_SQLCHECK:THE FOLLOWING SQL UNSUPPORTED/SYNTAX ERROR,PLEASE CHECK&MODIFY!*/\r\n"+tmpSqlList.get(tmpSqlList.size()-1));
			sLog.log("-- Warning!!! Maybe SQL Unsupported/Syntax Error!SQL OrderNo:"+sqlSN,'w'); 	
		  } 
	    }catch(Exception e){
		   sLog.log("-- Error:parsing sql...",'s');
		   e.printStackTrace();
	    }
		sqlSN++;
	  }
	  //temp remarked
	  sLog.log("-- tableMetaLst:");
	  sLog.log(Arrays.deepToString(tableMetaLst.toArray()));
	  //4.prcess metadata
	  SqlNormalizer sner=new SqlNormalizer(tableMetaLst,paramList,sLog);
	  for (Map<String,String> mp : sner.normalizingTableMeta(dbType)) {
        newTableMetaLst.add(mp);
      }
	  //4.2 process sqllist and metadata,
	  sLog.log("-- newTableMetaLst:");
	  sLog.log(Arrays.deepToString(newTableMetaLst.toArray()));
      //5.replace ori colname&typelen by new colname&typelen in file;
	  int i=0;
	  for (Map mp : tableMetaLst) {	  
		switch (mp.get(KEY_CATAGORY).toString()){ 
		  case "COLUMN":
	        if (!mp.get(KEY_NAME).toString().equals(newTableMetaLst.get(i).get(KEY_NAME).toString())||(mp.get(KEY_TYPELEN)!=null&&!mp.get(KEY_TYPELEN).toString().equals(newTableMetaLst.get(i).get(KEY_TYPELEN).toString()))||!mp.get(KEY_ADVICE).toString().equals(newTableMetaLst.get(i).get(KEY_ADVICE).toString())){
		      //fileLines=fileLines.replaceAll(getregex(strArr[IDX_NAME]+"\\s+"+strArr[IDX_TYPLEN]),newTableMetaLst.get(i)[IDX_NAME]+"    "+newTableMetaLst.get(i)[IDX_TYPLEN]+newTableMetaLst.get(i)[IDX_ADVICE]);
			  tmpSqlList.set(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1,tmpSqlList.get(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1).replaceAll(getregex(mp.get(KEY_NAME)+"\\s+"+mp.get(KEY_TYPELEN)),newTableMetaLst.get(i).get(KEY_NAME)+"    "+newTableMetaLst.get(i).get(KEY_TYPELEN)+newTableMetaLst.get(i).get(KEY_ADVICE)));
			  sLog.log("-- Changing Old:"+mp.get(KEY_CATAGORY)+":"+mp.get(KEY_TABLE)+"."+mp.get(KEY_NAME)+" "+mp.get(KEY_TYPELEN));
			  sLog.log("-- -------> New:"+newTableMetaLst.get(i).get(KEY_CATAGORY)+":"+newTableMetaLst.get(i).get(KEY_TABLE)+"."+newTableMetaLst.get(i).get(KEY_NAME)+" "+newTableMetaLst.get(i).get(KEY_TYPELEN)+":"+newTableMetaLst.get(i).get(KEY_ADVICE));
			}
		    break;
		  case "AT_":
		    strOnLineDDL=newTableMetaLst.get(i).get(KEY_ALGORITHM)==null?",ALGORITHM=INPLACE":(newTableMetaLst.get(i).get(KEY_ALGORITHM).toString().equals("")?",ALGORITHM=INPLACE":",ALGORITHM="+newTableMetaLst.get(i).get(KEY_ALGORITHM).toString());
			strOnLineDDL=strOnLineDDL+(newTableMetaLst.get(i).get(KEY_LOCK)==null?",LOCK=NONE":(newTableMetaLst.get(i).get(KEY_LOCK).toString().equals("")?",LOCK=NONE":",LOCK="+newTableMetaLst.get(i).get(KEY_LOCK).toString()));
		    if (!strOnLineDDL.equals("")){
		      //fileLines=fileLines.replaceAll(getregex(strArr[IDX_NAME]),newTableMetaLst.get(i)[IDX_NAME]+newTableMetaLst.get(i)[IDX_ADVICE]);
			  tmpSqlList.set(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1,tmpSqlList.get(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1)+strOnLineDDL);
		      sLog.log("-- Changed OnlineDDL ok:"+mp.get(KEY_CATAGORY)+":"+mp.get(KEY_NAME));
		    }
		    //break;
		  case "CIDX_":case "CUK_":
		    strOnLineDDL=newTableMetaLst.get(i).get(KEY_ALGORITHM)==null?" ALGORITHM=INPLACE":(newTableMetaLst.get(i).get(KEY_ALGORITHM).toString().equals("")?" ALGORITHM=INPLACE":"");
			strOnLineDDL=strOnLineDDL+(newTableMetaLst.get(i).get(KEY_LOCK)==null?" LOCK=NONE":(newTableMetaLst.get(i).get(KEY_ALGORITHM).toString().equals("")?" LOCK=NONE":""));
		    if (!strOnLineDDL.equals("")&&tmpSqlList.get(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1).indexOf("ALGORITHM=INPLACE")<0){
		      //fileLines=fileLines.replaceAll(getregex(strArr[IDX_NAME]),newTableMetaLst.get(i)[IDX_NAME]+newTableMetaLst.get(i)[IDX_ADVICE]);
			  tmpSqlList.set(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1,tmpSqlList.get(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1)+strOnLineDDL);
		      sLog.log("-- Changed OnlineDDL ok:"+mp.get(KEY_CATAGORY)+":"+mp.get(KEY_NAME));
		    }
		    //break;	
		  default:
		    if (!mp.get(KEY_NAME).toString().equals(newTableMetaLst.get(i).get(KEY_NAME).toString())){
		      //fileLines=fileLines.replaceAll(getregex(strArr[IDX_NAME]),newTableMetaLst.get(i)[IDX_NAME]+newTableMetaLst.get(i)[IDX_ADVICE]);
			  if (!mp.get(KEY_NAME).toString().equals("")){
			    tmpSqlList.set(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1,tmpSqlList.get(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1).replaceAll(getregex(mp.get(KEY_NAME).toString()),
			                  newTableMetaLst.get(i).get(KEY_NAME).toString()+(newTableMetaLst.get(i).get(KEY_ADVICE)==null?"":newTableMetaLst.get(i).get(KEY_ADVICE).toString())));
				sLog.log("-- Changed ok:"+mp.get(KEY_CATAGORY)+":"+mp.get(KEY_NAME)+"->"+newTableMetaLst.get(i).get(KEY_NAME));			  
		      }
			  if (mp.get(KEY_REX)!=null&&!mp.get(KEY_REX).toString().equals("")&&(mp.get(KEY_CATAGORY).toString().equals("IDX_")||mp.get(KEY_CATAGORY).toString().equals("UK_")||mp.get(KEY_CATAGORY).toString().equals("FK_"))){
				  tmpSqlList.set(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1,tmpSqlList.get(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1).replaceAll(mp.get(KEY_REX).toString(),
				                 getSpecialFmt(tmpSqlList.get(Integer.parseInt(mp.get(KEY_SQLSN).toString())-1),mp.get(KEY_REX).toString(),String.format(mp.get(KEY_FMT).toString(),newTableMetaLst.get(i).get(KEY_NAME).toString()))));
                  sLog.log("-- INDEX/KEY EMPTY,REGEX:"+mp.get(KEY_REX)+",REPLACED:"+mp.get(KEY_FMT));				  
				} 
		    }
		    break;
		}
		i++;
      }
	  i=0;
	  sLog.log("--- newfile");
	  for (String str:tmpSqlList){
		  sLog.log("-- oriSQL:"+oriSqlList.get(i));
		  sLog.log("-- newSQL:"+str);
		 fileLines=fileLines.replace(oriSqlList.get(i),str); 
        i++;		
	  }
	  
	  //6.producting new ddl sql file	
	  try {
	    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(args[0].substring(0,args[0].lastIndexOf("\\")+1)+"new"+args[0].substring(args[0].lastIndexOf("\\")+1,args[0].length())));
	    out.write(fileLines.getBytes(FILE_ENCODING));
		out.close();
	  } catch (FileNotFoundException e) {  
          e.printStackTrace();  
      } catch (IOException e) {  
          e.printStackTrace();  
      } //temp remarked end 
	  sLog.log("---***endmain***---");
      } catch (UnsupportedEncodingException e) {  
          sLog.log("The OS does not support " + FILE_ENCODING);  
          e.printStackTrace();  
      } 
  }
}