/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By   Description
---  -------------  --------------  -----------  ----------   -------------------------------------
1.1  2019-08-29     xlzhu@ips.com                             producting standard tablemeata list

*/
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlNormalizer {
  private static final String NOSTANDARD_NORMALIZING=" non-standard,normalizing...";
  private static final String PREFIX_TABLE_DEFAULT="T_";
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
  private static final int MAXLEN_OBJECT_NAME=30;
  private List<Map<String,String>> oriTableMetaLst;
  private Map<String,String> regexCheckMap;
  private WordSegment wSegment;
  private WordStandard wStandard;
  private SqlLogging sLog;
  private String mysql_Algorithm="INPLACE";
  private String mysql_Lock="NONE";
  private boolean checkTableComment=true;
  private boolean checkEventComment=true;
  private boolean checkFunctionComment=true;
  private boolean checkProcedureComment=true;
  private boolean checkColumnComment=true;
  private boolean checkTableUniqueKey=true;
  private boolean normalizingObjsByDftRules=true;
  
  public SqlNormalizer(List<Map<String,String>> tableMetaLst,ParamConfig pConfig,SqlLogging pLogger){
    wSegment= new WordSegment();
	wStandard= new WordStandard();
	oriTableMetaLst=tableMetaLst;
	regexCheckMap=new LinkedHashMap<String,String>();
	sLog=pLogger;
	this.sLog.log("-- init parameter of normalizing");
	checkTableComment=pConfig.getPropValue("CHECK_TABLE_COMMENT").equals("YES");
	checkEventComment=pConfig.getPropValue("CHECK_EVENT_COMMENT").equals("YES");
	checkFunctionComment=pConfig.getPropValue("CHECK_FUNCTION_COMMENT").equals("YES");
	checkProcedureComment=pConfig.getPropValue("CHECK_PROCEDURE_COMMENT").equals("YES");
	checkColumnComment=pConfig.getPropValue("CHECK_COLUMN_COMMENT").equals("YES");
	checkTableUniqueKey=pConfig.getPropValue("CHECK_TABLE_UNIQUEKEY").equals("YES");
	regexCheckMap.put("T_",pConfig.getPropValue("REGEX_CHECK_TABLE"));
	regexCheckMap.put("IDX_",pConfig.getPropValue("REGEX_CHECK_INDEX"));
	regexCheckMap.put("UK_",pConfig.getPropValue("REGEX_CHECK_UNIQUEKEY"));
	regexCheckMap.put("PK_",pConfig.getPropValue("REGEX_CHECK_PRIMARYKEY"));
	regexCheckMap.put("FK_",pConfig.getPropValue("REGEX_CHECK_FOREIGNKEY"));
	regexCheckMap.put("F_",pConfig.getPropValue("REGEX_CHECK_FUNCTION"));
	regexCheckMap.put("SP_",pConfig.getPropValue("REGEX_CHECK_PROCEDURE"));
	regexCheckMap.put("PKG_",pConfig.getPropValue("REGEX_CHECK_PACKAGE"));
	regexCheckMap.put("TRG_",pConfig.getPropValue("REGEX_CHECK_TRIGGER"));
	regexCheckMap.put("PT_",pConfig.getPropValue("REGEX_CHECK_PARTITION"));
	regexCheckMap.put("SEQ_",pConfig.getPropValue("REGEX_CHECK_SEQUENCE"));
	regexCheckMap.put("VW_",pConfig.getPropValue("REGEX_CHECK_VIEW"));	
	regexCheckMap.put("ET_",pConfig.getPropValue("REGEX_CHECK_EVENT"));
	mysql_Algorithm=pConfig.getPropValue("ONLINE_DDL_MYSQL_ALGORITHM");
	mysql_Lock=pConfig.getPropValue("ONLINE_DDL_MYSQL_LOCK");
    normalizingObjsByDftRules=pConfig.getPropValue("NORMALIZING_OBJECTS_BY_DEFAULTRULES").equals("YES");
  } 
  private int getIdxLastCat(List<Map<String,String>> ptableMetaLst,String pCatagory,int pMaxSize){
	int j=-1;
    for (int i = pMaxSize - 1; i >= 0; i--) {
 	  if (ptableMetaLst.get(i).get(KEY_CATAGORY).equals(pCatagory)){
		j=i;
		break;
	  }     
    }	
    return j;	
  }  
  private boolean matchRegex(String pStr,String regEx){
	Pattern prm = Pattern.compile(regEx);
    Matcher mrm = prm.matcher(pStr);
	if (mrm.matches()){//attention!!!!matches vs find
	  return true;
	}else{
	  return false;
	}
  }
  
  private String[] splitObject2LORArray(String pObj){
    //mysql special char ``;oracle special char ""
	int posDot=pObj.indexOf(".");
	int posSQuote=pObj.indexOf("`",posDot)<0?posDot:pObj.indexOf("`",posDot);
	int posEQuote=pObj.indexOf("`",posSQuote+1)<0?pObj.length():pObj.indexOf("`",posSQuote+1);
	String leftStr=pObj.substring(0,posSQuote+1);
	String rightStr=pObj.substring(posEQuote,pObj.length());
	String objStr=pObj.substring(posSQuote+1,posEQuote);
    String[] objArray={leftStr,objStr,rightStr};	
	return objArray;
  }
  private String normalizingPrefix(String pStr,String pPrefix){
	String objStr=pStr;
	if (!objStr.startsWith(pPrefix)){
	  if (pPrefix.equals("PT_")){
	    objStr=Pattern.compile("[^0-9]").matcher(objStr).replaceAll("");
	  }
	  objStr=pPrefix+objStr; 
	}
	return objStr;
  } 
  private String normalizingLen(String pStr){
    String objStr=pStr;
	if (objStr.length()>MAXLEN_OBJECT_NAME){
      objStr=objStr.substring(0,MAXLEN_OBJECT_NAME);
	}
	return objStr;
  }
  private String normalizingObj(String pOriObj,String pPrefix){
	String objFmt;
	String[] strArray=splitObject2LORArray(pOriObj);
	objFmt=normalizingPrefix(strArray[1],pPrefix);
	objFmt=normalizingLen(objFmt);
	
	return strArray[0]+objFmt+strArray[2];
  }
  private String distinctStr(String pStr,String pdelimiter){
	String rstStr="";
	String[] strArray=pStr.split(pdelimiter);
	for (String str:strArray){
	  if (rstStr.indexOf(str)<0){
	    rstStr=rstStr+str;
	  }
	}
	return rstStr;
  }
  private String distinctStrFChar(String pStr,String pdelimiter){
	String rstStr="";
	String rstFstr="";
	String[] strArray=pStr.split(pdelimiter);
	for (String str:strArray){
	  if (rstStr.indexOf(str)<0){
	    rstStr=rstStr+str;
		rstFstr=rstFstr+str.substring(0,1);
	  }
	}
	return rstFstr;
  }
  private String normalizingIdx(String pOriIdx,String pOriTable,String pColumns,String pPrefix,String pSuffix){
	//Index format:PK_TABLENAME;UK/CK/IDX_TABLENAME_COLUMNNAME
	String columnStr="";
	String idxRst="";
	String[] idxArray=splitObject2LORArray(pOriIdx);
	String[] tableArray=splitObject2LORArray(pOriTable);
	String[] columnArray=pColumns.split(",");
	//1.combine columns to string
	for (String str:columnArray){
	  if (columnStr.equals("")){
		columnStr=str; 
	  }else{
	    columnStr=columnStr+"_"+str;	
	  }
	}
	//2.drop table prefix
	if (tableArray[1].startsWith(PREFIX_TABLE_DEFAULT)){
	  tableArray[1]=tableArray[1].substring(PREFIX_TABLE_DEFAULT.length(),tableArray[1].length());
	}
	//3.combine prefix,table,column string
	switch (pPrefix){
		case "PK_":case "TRG_":
		  idxRst=tableArray[1];
		  idxRst=idxRst.length()+pPrefix.length()+pSuffix.length()>MAXLEN_OBJECT_NAME?idxRst.substring(0,MAXLEN_OBJECT_NAME-pPrefix.length()-pSuffix.length()):idxRst;
		  idxRst= pPrefix+idxRst+pSuffix;
		  break;
		default:
		  idxRst= pPrefix+tableArray[1]+"_"+columnStr;
		  //drop _ char
		  idxRst=idxRst.length()>MAXLEN_OBJECT_NAME?pPrefix+tableArray[1]+"_"+columnStr.replace("_",""):idxRst;
		  //distinct column
		  idxRst=idxRst.length()>MAXLEN_OBJECT_NAME?pPrefix+tableArray[1]+"_"+distinctStr(columnStr,"_"):idxRst;
		  //disttinct column first char
		  idxRst=idxRst.length()>MAXLEN_OBJECT_NAME?pPrefix+tableArray[1]+"_"+distinctStrFChar(columnStr,"_"):idxRst;
		  //drop lastfield of table
		  idxRst=idxRst.length()>MAXLEN_OBJECT_NAME?pPrefix+tableArray[1].substring(0,tableArray[1].lastIndexOf("_")==-1?(tableArray[1].length()/2):tableArray[1].lastIndexOf("_"))+"_"+distinctStrFChar(columnStr,"_"):idxRst;
		  break;
	}
	return idxArray[0]+idxRst+idxArray[2];
  }
  
  private String newSqlAdvice(String oldAdvice,String newAdvice){
	String rst=oldAdvice;
	if (rst.endsWith("*/")){
	  if (rst.indexOf(newAdvice)<0){
	    rst=rst.substring(0,rst.length()-2)+","+newAdvice+"*/";
	  }
	}else{
	  rst="/*IPS_SQLCHECK:"+newAdvice+"*/";
	}
    return rst;	
  }
  private void syncTableNameChanged(List<Map<String,String>> plist){
	int i;
    for (Map<String,String> mp : plist) {
	  if (mp.get(KEY_CATAGORY).toString().equals("CT_")&&!mp.get(KEY_NAME).toString().equals(mp.get(KEY_TABLE).toString())&&!mp.get(KEY_TABLE).toString().equals("")){
		i=0;
		this.sLog.log("-- syncTableNameChanged:"+mp.get(KEY_NAME));
		for (Map<String,String> mp2 : plist) {
	      if (mp.get(KEY_TABLE).toString().equals(mp2.get(KEY_TABLE).toString())&&!mp2.get(KEY_CATAGORY).toString().equals("CT_")){
		    plist.get(i).put(KEY_TABLE,mp.get(KEY_NAME).toString());
		  }
          if (mp.get(KEY_TABLE).toString().equals(mp2.get(KEY_COMMENT).toString())){
		    plist.get(i).put(KEY_COMMENT,mp.get(KEY_NAME).toString());
		  }	
		  i++;
	    }
	  }	
    }  
  }
  private void checkObjectUniqueKeyComment(List<Map<String,String>> plist){
	//table of new create has at least one unique key
	boolean existUK;
	int i=0;
	int j;
	for (Map<String,String> mp : plist) {
	  if (mp.get(KEY_CATAGORY).toString().equals("CT_")){
		if (checkTableUniqueKey||checkColumnComment){
		 existUK=false;
		 j=0;
		 for (Map<String,String> mp2 : plist) {
		   if((mp2.get(KEY_CATAGORY).toString().equals("UK_")||mp2.get(KEY_CATAGORY).toString().equals("PK_"))&&mp2.get(KEY_TABLE).toString().equals(mp.get(KEY_NAME).toString())){
		     existUK=true;  
	       }
		   if (mp2.get(KEY_CATAGORY).toString().equals("COLUMN")&& checkColumnComment &&mp2.get(KEY_COMMENT).toString().equals("")&&mp2.get(KEY_TABLE).toString().equals(mp.get(KEY_NAME).toString())){
			 this.sLog.log("-- Warning:"+mp.get(KEY_NAME)+"."+plist.get(j).get(KEY_NAME)+" have not comment!",'w');
		     plist.get(j).put(KEY_ADVICE,newSqlAdvice(plist.get(j).get(KEY_ADVICE),"NO COMMENT"));	
	       } 
		   j++;
		 }
		 
		 if (checkTableUniqueKey&&!existUK){
		   this.sLog.log("-- Warning:"+mp.get(KEY_NAME)+" have not unique key!",'w');
		   plist.get(i).put(KEY_ADVICE,newSqlAdvice(plist.get(i).get(KEY_ADVICE),"NO PRIMARY/UNIQUE KEY"));
		 } 
		}
		if (checkTableComment&&mp.get(KEY_COMMENT).toString().equals("")){
		  this.sLog.log("-- Warning:"+mp.get(KEY_NAME)+" have not comment!",'w');
		  plist.get(i).put(KEY_ADVICE,newSqlAdvice(plist.get(i).get(KEY_ADVICE),"NO COMMENT"));	
		}
	  }else if((mp.get(KEY_CATAGORY).toString().equals("CET_")&&checkEventComment)||(mp.get(KEY_CATAGORY).toString().equals("CSP_")&&checkProcedureComment)||(mp.get(KEY_CATAGORY).toString().equals("CF_")&&checkFunctionComment)){
	    if (mp.get(KEY_COMMENT)==null||mp.get(KEY_COMMENT).toString().equals("")){
		  plist.get(i).put(KEY_ADVICE,newSqlAdvice(plist.get(i).get(KEY_ADVICE),"NO COMMENT")); 
		}		 
	  }
	  
	  i++;
	}
  }
  
  public List<Map<String,String>> normalizingTableMeta(String pdbType){
	//String[4]={"TableName","ColumnName","ColTypeLength","comment"}//old
	//String[6]={"Type","Name(Column)","TypeLength","comment","ParentName(Table),Advice}
	List<Map<String,String>> newTableMetaLst=new ArrayList<Map<String,String>>();
	String wordSegment=null;
	String wordStandardCol=null;
	String wordStandardTL=null;
	//1.check&normalizing object name
	for (Map<String,String> mp:oriTableMetaLst) {
	  newTableMetaLst.add(new LinkedHashMap<String,String>());
	  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_CATAGORY,mp.get(KEY_CATAGORY).toString());
	  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_NAME,mp.get(KEY_NAME).toString());
	  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_PREFIX,mp.get(KEY_PREFIX)==null?"":mp.get(KEY_PREFIX).toString());
	  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_TYPELEN,mp.get(KEY_TYPELEN)==null?"":mp.get(KEY_TYPELEN).toString());
	  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_COMMENT,mp.get(KEY_COMMENT)==null?"":mp.get(KEY_COMMENT).toString());
	  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_TABLE,mp.get(KEY_TABLE)==null?"":mp.get(KEY_TABLE).toString());
	  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_ADVICE,mp.get(KEY_ADVICE)==null?"":mp.get(KEY_ADVICE).toString());
	  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_SQLSN,mp.get(KEY_SQLSN).toString());
	  if (mp.get(KEY_CATAGORY).toString().equals("AT_")||mp.get(KEY_CATAGORY).toString().equals("CIDX_")){
		newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_ALGORITHM,mp.get(KEY_ALGORITHM)==null?"":mp.get(KEY_ALGORITHM).toString());
        newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_LOCK,mp.get(KEY_LOCK)==null?"":mp.get(KEY_LOCK).toString());		
	  }
	  if (mp.get(KEY_CATAGORY).toString()!="COLUMN"&&!matchRegex(mp.get(KEY_NAME).toString(),regexCheckMap.get(mp.get(KEY_PREFIX).toString()).toString())&&!normalizingObjsByDftRules){
		newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_ADVICE,newSqlAdvice(mp.get(KEY_ADVICE).toString(),"NAME MUST LIKE "+regexCheckMap.get(mp.get(KEY_PREFIX).toString())));
		this.sLog.log("-- "+mp.get(KEY_CATAGORY)+":"+mp.get(KEY_NAME)+newSqlAdvice(mp.get(KEY_ADVICE).toString(),"NAME MUST LIKE "+regexCheckMap.get(mp.get(KEY_PREFIX).toString())));
	  }
	  switch (mp.get(KEY_CATAGORY).toString()){
		//RULE:PREFIX_OTHERS,PT_MAX/YYYYMMDD
	    case "CT_":case "CF_":case "CSP_":case "PKG_":case "SEQ_":case "CVW_":case "PT_":case "RT_":case "CET_":
		  if (!matchRegex(mp.get(KEY_NAME).toString(),regexCheckMap.get(mp.get(KEY_PREFIX).toString()).toString())&&normalizingObjsByDftRules){
			this.sLog.log("-- "+mp.get(KEY_CATAGORY)+":"+mp.get(KEY_NAME)+NOSTANDARD_NORMALIZING);
			newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_TABLE,mp.get(KEY_CATAGORY).toString().equals("CT_")?mp.get(KEY_NAME).toString():(mp.get(KEY_TABLE)==null?"":mp.get(KEY_TABLE).toString()));
			newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_NAME,normalizingObj(mp.get(KEY_NAME).toString(),mp.get(KEY_PREFIX).toString()));
			
		  }
		  break;
		//RULE:IDX/UK/FK/CK_TABLENAME_COLUMNNAME;PK/TRG_TABLENAME
		case "IDX_":case "UK_":case "FK_":case "CK_":case "PK_":case "CTRG_":case "CIDX_":case "CUK_"://CK NO DEFINE
		  if (!matchRegex(mp.get(KEY_NAME).toString(),regexCheckMap.get(mp.get(KEY_PREFIX).toString()).toString())&&normalizingObjsByDftRules){
			this.sLog.log("-- "+mp.get(KEY_CATAGORY)+":"+mp.get(KEY_NAME)+NOSTANDARD_NORMALIZING);
			newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_NAME,normalizingIdx(mp.get(KEY_NAME).toString(),mp.get(KEY_TABLE).toString(),mp.get(KEY_COLUMNS)==null?"":mp.get(KEY_COLUMNS).toString(),mp.get(KEY_PREFIX).toString(),"_"+(mp.get(KEY_EVENT)==null?"":mp.get(KEY_EVENT).toString())));
			int idxlastCat=getIdxLastCat(oriTableMetaLst,"AT_",newTableMetaLst.size());
			this.sLog.log(String.valueOf(idxlastCat));
			this.sLog.log(String.valueOf(newTableMetaLst.size()));
			if (idxlastCat>-1&&oriTableMetaLst.get(idxlastCat).get(KEY_NAME).toString().equals(mp.get(KEY_TABLE)==null?"?":mp.get(KEY_TABLE).toString())){
              if (mp.get(KEY_ALGORITHM)==null||mp.get(KEY_ALGORITHM).equals("")){
			    newTableMetaLst.get(idxlastCat).put(KEY_ALGORITHM,mysql_Algorithm);	
			  }	
              if (mp.get(KEY_LOCK)==null||mp.get(KEY_LOCK).equals("")){
			    newTableMetaLst.get(idxlastCat).put(KEY_LOCK,mysql_Lock);	
			  }
            }			
		  }		
		  break;
        //RULE:from list standard word&word segment		  
	    case "COLUMN":
		  this.sLog.log("-- Column:"+mp.get(KEY_NAME));
	      if (mp.get(KEY_COMMENT)!=null&&!mp.get(KEY_COMMENT).toString().equals("")){//exist comment
		    //1.1.get wordsegment by comment of tableMetaLst
		    this.sLog.log("---- ProcesComment:"+mp.get(KEY_COMMENT));
            wordSegment=wSegment.getSegment(mp.get(KEY_COMMENT).toString());
		    this.sLog.log("---- GetWordSegment:"+wordSegment);
		    //1.2.get colname&typelen from standard words list by result of 1(wordsegment)
		    wordStandardCol=wStandard.getSDWord(wordSegment);
		    wordStandardTL=wStandard.getSDTypeLen(pdbType,wordSegment);
			this.sLog.log("---- GetWordStandard:"+wordStandardCol+","+wordStandardTL);
		    if (!wordStandardCol.equals("")&&!wordStandardTL.equals("")){
		      //this.sLog.log("--regex:"+getregex(strArr[IDX_NAME]+"\\s+"+strArr[IDX_TYPLEN]));
	          newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_NAME,wordStandardCol);
		      newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_TYPELEN,wordStandardTL);
		      this.sLog.log("---- Changing Col&TypeLen ok:"+mp.get(KEY_NAME)+"->"+wordStandardCol+","+mp.get(KEY_TYPELEN)+"->"+wordStandardTL);
		    }
	      }else if (mp.get(KEY_TYPELEN)!=null&&!mp.get(KEY_TYPELEN).toString().equals("")){//not drop column,need comment!
		    newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_ADVICE,newSqlAdvice(newTableMetaLst.get(newTableMetaLst.size()-1).get(KEY_ADVICE),"NO COMMENT"));
			  
		  }
	      //1.3 if noexist comment,get colname&typelen from standard words by COLNAME 
	      if ((wordStandardCol==null||wordStandardCol.equals(""))&&!mp.get(KEY_NAME).toString().equals("")){
		    wordStandardTL=wStandard.getSDTypeLenByCol(pdbType,mp.get(KEY_NAME).toString().replaceAll("`",""));
			this.sLog.log("---- GetTypeLenStandard:"+wordStandardTL);
		    if (!wordStandardTL.equals("")){
		      newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_TYPELEN,wordStandardTL);
		      this.sLog.log("---- Changing TypeLen ok:"+mp.get(KEY_TYPELEN)+"->"+wordStandardTL);
		    }
	      }
		  int idxlastCat=getIdxLastCat(oriTableMetaLst,"AT_",newTableMetaLst.size());
		  if (idxlastCat>-1&&oriTableMetaLst.get(idxlastCat).get(KEY_NAME).toString().equals(mp.get(KEY_TABLE)==null?"?":mp.get(KEY_TABLE).toString())){
		    if (mp.get(KEY_ALGORITHM)==null||mp.get(KEY_ALGORITHM).toString().equals("")){
			  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_ALGORITHM,mysql_Algorithm);	
		    }	
            if (mp.get(KEY_LOCK)==null||mp.get(KEY_LOCK).toString().equals("")){
			  newTableMetaLst.get(newTableMetaLst.size()-1).put(KEY_LOCK,mysql_Lock);	
		    }
		  }
		  break;
		default:
		  this.sLog.log("-- Error:unknown category:"+mp.get(KEY_CATAGORY)+"!!!",'s');
		  break;
	  }
    } 
    //2.if tablename changed,change all related
    syncTableNameChanged(newTableMetaLst);
	//3.check table/column whether exist unique key&comment only for ceate table sql
    checkObjectUniqueKeyComment(newTableMetaLst);	
    return 	newTableMetaLst;
  }  
}
