/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By   Description
---  -------------  --------------  -----------  ----------   -------------------------------------
1.0  2019-08-21     xlzhu@ips.com                             visitor mysql ddl,Attention!!must implements SQLVisitor interface!
*/
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import IPSAST.mysql.*;


public class MySqlVisitor extends MySqlParserBaseVisitor<String> implements SqlVisitor{
  //String[4]={"TableName","ColumnName","ColTypeLength","comment"}//old
  //String[5]={"Category","Name(Column)","TypeLength","comment(column)","ParentName(Table),Advice} ;Category:T_,COLUMN,IDX_,UK_,PK_,FK_,TRG_,F_,SP_,PKG_,PT_,SEQ_,VW_
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
  public List<Map<String,String>> tableMetaLst;
  private String parentStrName="";
  private String strName="";
  private String colTypeLen="";
  private String comment="";
  private String uidType="";//uidType:CT_,AT_,COLUMN,IDX_,UK_,PK_,FK_,TRG_,F_,SP_,PKG_,PT_,SEQ_,VW_
  private String prefixObj="";
  private String rex_Replace="";
  private String fmt_Replace="";
  private int tableIdx,sqlOrder;
  private boolean isDDL;
	
  @Override 
  public String visitDdlStatement(MySqlParser.DdlStatementContext ctx) { 
    this.isDDL=true;
	this.tableMetaLst = new ArrayList<Map<String,String>>();
    return visitChildren(ctx); 
  }
  
  @Override 
  public String visitColumnCreateTable(MySqlParser.ColumnCreateTableContext ctx) { 
  	if (this.isDDL){
	  this.uidType="CT_";
	  this.prefixObj="T_";
	  this.parentStrName=ctx.tableName().getText();
	}
    return visitChildren(ctx); 
  }
  @Override 
  public String visitTableOptionComment(MySqlParser.TableOptionCommentContext ctx) { 
	if (this.isDDL){
	  this.comment=ctx.STRING_LITERAL().getText();
	  this.tableMetaLst.get(getIdxLastCat("CT_")).put(KEY_COMMENT,this.comment);
	}
    return visitChildren(ctx); 
  }  
  @Override 
  public String visitAlterTable(MySqlParser.AlterTableContext ctx) {
  	if (this.isDDL){
	  this.uidType="AT_";
	  this.prefixObj="T_";
	  this.parentStrName=ctx.tableName().getText();
    }
	return visitChildren(ctx); 
  }
  @Override 
  public String visitRenameTableClause(MySqlParser.RenameTableClauseContext ctx) {
	this.uidType="RT_";
	this.prefixObj="T_";
	this.parentStrName=ctx.tableName(0).getText();
	putOthers(this.uidType,ctx.tableName(1).getText(),this.parentStrName,this.prefixObj);
    return visitChildren(ctx); 
  }
  @Override 
  public String visitCreateIndex(MySqlParser.CreateIndexContext ctx) {
	this.uidType="CUK_";
	this.prefixObj="UK_";
	if (ctx.UNIQUE()==null){
	  this.uidType="CIDX_";	
	  this.prefixObj="IDX_";
	}
	putIndexPT(this.uidType,ctx.uid().getText(),cutSpecialChar(ctx.indexColumnNames().getText()),ctx.tableName().getText(),this.prefixObj,ctx.algType==null?"":ctx.algType.getText(),ctx.lockType==null?"":ctx.lockType.getText(),this.rex_Replace,this.fmt_Replace);  
	this.uidType="";
	this.prefixObj="";
    return visitChildren(ctx); 
  }
  @Override 
  public String visitPartitionComparision(MySqlParser.PartitionComparisionContext ctx) {
	this.uidType="PT_";
	this.prefixObj="PT_";
	return visitChildren(ctx); 
  }
  @Override 
  public String visitPartitionListAtom(MySqlParser.PartitionListAtomContext ctx) {
	this.uidType="PT_";
	this.prefixObj="PT_";
	return visitChildren(ctx); 
  }
  @Override 
  public String visitPartitionListVector(MySqlParser.PartitionListVectorContext ctx) {
	this.uidType="PT_";
	this.prefixObj="PT_";
	return visitChildren(ctx); 
  }
  @Override 
  public String visitPartitionSimple(MySqlParser.PartitionSimpleContext ctx) {
	this.uidType="PT_";
	this.prefixObj="PT_";
	return visitChildren(ctx); 
  }
  @Override 
  public String visitUid(MySqlParser.UidContext ctx) {
	//avoid conflict with visitIndexColumnNames
	this.strName=ctx.getText();
	if (!this.uidType.equals("PK_")&&!this.uidType.equals("UK_")&&!this.uidType.equals("CK_")&&!this.uidType.equals("IDX_")){//&&!this.uidType.equals("RT_")
	  putOthers(this.uidType,this.strName,this.strName.equals(this.parentStrName)?null:this.parentStrName,this.prefixObj);
	}
	return visitChildren(ctx); 
  }
  
  @Override 
  public String visitColumnDeclaration(MySqlParser.ColumnDeclarationContext ctx) {
	if (this.isDDL){
	  this.uidType="COLUMN";
	  this.prefixObj="";
    }
	return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByAddColumn(MySqlParser.AlterByAddColumnContext ctx) {
	this.uidType="COLUMN";
	this.prefixObj=""; 
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByAddColumns(MySqlParser.AlterByAddColumnsContext ctx) {
    this.uidType="COLUMN";
	this.prefixObj="";
    return visitChildren(ctx); 
  }
  @Override 
  public String visitDataType(MySqlParser.DataTypeContext ctx) { 
	if (this.isDDL){
	  this.colTypeLen=ctx.getText();
	  topMapPut(KEY_TYPELEN,this.colTypeLen);
	}
    return visitChildren(ctx); 
  }
  @Override 
  public String visitCommentColumnConstraint(MySqlParser.CommentColumnConstraintContext ctx) {
	if (this.isDDL){
	  this.comment=ctx.STRING_LITERAL().getText();
	  this.tableMetaLst.get(getIdxLastCat("COLUMN")).put(KEY_COMMENT,this.comment);
	}	
	return visitChildren(ctx); 
  }

  @Override 
  public String visitPrimaryKeyColumnConstraint(MySqlParser.PrimaryKeyColumnConstraintContext ctx) {
	//PRIMARY KEY NO NAME
	putIndexPT("PK_","PK_SYSDEFAULT",this.strName,this.parentStrName,"PK_",null,null,this.rex_Replace,this.fmt_Replace);
	return visitChildren(ctx); 
  }
  @Override 
  public String visitUniqueKeyColumnConstraint(MySqlParser.UniqueKeyColumnConstraintContext ctx) {
	//UNIQUE KEY NAME=COLUMNNAME
	this.rex_Replace="(?i)UNIQUE\\s*(KEY)?\\s*,";
	this.fmt_Replace=",\r\n  UNIQUE KEY %s ("+this.strName+"),";
	putIndexPT("UK_","",this.strName,this.parentStrName,"UK_",null,null,this.rex_Replace,this.fmt_Replace);
	return visitChildren(ctx); 
  }
  @Override 
  public String visitConstraintDeclaration(MySqlParser.ConstraintDeclarationContext ctx) {
	//clear uid because of pk,uk,fk maybe havenot uid
	if (this.isDDL){
	  this.strName="";
    }
    return visitChildren(ctx); 
  }
  @Override 
  public String visitPrimaryKeyTableConstraint(MySqlParser.PrimaryKeyTableConstraintContext ctx) {
	//primary key name unused whether sefdefine or not,default primary
	if (this.isDDL){
	  this.uidType="PK_";
	  this.prefixObj="PK_";
    }	  
    return visitChildren(ctx); 
  }
  @Override 
  public String visitUniqueKeyTableConstraint(MySqlParser.UniqueKeyTableConstraintContext ctx) {
	//IF UNDEFINE NAME,DEFAULT NAME=COLUMNNAME
	if (this.isDDL){
	  this.uidType="UK_";
	  this.prefixObj="UK_";
	  this.rex_Replace=ctx.index==null?"(?i)UNIQUE\\s+"+ctx.indexFormat.getText():"";
	  this.fmt_Replace=ctx.index==null?"UNIQUE "+ctx.indexFormat.getText()+" %s ":""; 
    }	  
    return visitChildren(ctx); 
  }
  @Override 
  public String visitForeignKeyTableConstraint(MySqlParser.ForeignKeyTableConstraintContext ctx) {
	if (this.isDDL){
	  this.uidType="FK_";
	  this.prefixObj="FK_";
	  this.rex_Replace=ctx.name==null?"(?i),\\s*[\\r\\n]*\\s*(CONSTRAINT)?\\s*\\S*\\s*FOREIGN":"";
	  this.fmt_Replace=ctx.name==null?",\r\n  CONSTRAINT %s FOREIGN":"";
    }	  
    return visitChildren(ctx); 
  }
  @Override 
  public String visitCheckTableConstraint(MySqlParser.CheckTableConstraintContext ctx) {
	//mysql5.7 unsupported
	if (this.isDDL){
	  this.uidType="CK_";
	  this.prefixObj="CK_";
	  this.strName=ctx.uid().getText();
	  putIndexPT(this.uidType,this.strName,null,this.parentStrName,this.prefixObj,null,null,this.rex_Replace,this.fmt_Replace);
    }
    return visitChildren(ctx); 
  }
  @Override 
  public String visitIndexDeclaration(MySqlParser.IndexDeclarationContext ctx) {
	if (this.isDDL){
	  this.uidType="IDX_";
	  this.prefixObj="IDX_";
    }	
    return visitChildren(ctx); 
  }
  @Override 
  public String visitSimpleIndexDeclaration(MySqlParser.SimpleIndexDeclarationContext ctx) { 
    this.uidType="IDX_";
	this.prefixObj="IDX_";
    this.strName=ctx.uid()==null?"":ctx.uid().getText();
	this.rex_Replace=this.strName.equals("")?"(?i),\\s*[\\r\\n]*\\s*"+ctx.indexFormat.getText()+"[^`]":"";//!!!!!!!!!!!mybe replace (
	this.fmt_Replace=this.strName.equals("")?",\r\n  "+ctx.indexFormat.getText()+" %s ":"";       //!!!!!!!!!!!maybe need add ( 
    return visitChildren(ctx); 
  }
  @Override 
  public String visitIndexColumnNames(MySqlParser.IndexColumnNamesContext ctx) {
	//System.out.println("---indexcolumnname");
	//System.out.println(ctx.getText());
	if (this.isDDL){
	  if (this.strName.equals("")&&this.uidType.equals("PK_")){
		this.strName=this.prefixObj+"SYSDEFAULT";
	  }
	  putIndexPT(this.uidType,this.strName,cutSpecialChar(ctx.getText()),this.parentStrName,this.prefixObj,null,null,this.rex_Replace,this.fmt_Replace);
	  this.strName="";
	  this.uidType="";
	  this.prefixObj="";
	  this.rex_Replace="";
	  this.fmt_Replace="";
    }  
    return ""; 
  }
  @Override 
  public String visitAlterBySetAlgorithm(MySqlParser.AlterBySetAlgorithmContext ctx) {
	this.tableMetaLst.get(getIdxLastCat("AT_")).put(KEY_ALGORITHM,ctx.algType.getText());
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByLock(MySqlParser.AlterByLockContext ctx) {
	this.tableMetaLst.get(getIdxLastCat("AT_")).put(KEY_LOCK, ctx.lockType.getText());
    return visitChildren(ctx); 
  }
  //20191014
  @Override 
  public String visitAlterByAddIndex(MySqlParser.AlterByAddIndexContext ctx) { 
    this.uidType="IDX_";
	this.prefixObj="IDX_";
    this.strName=ctx.uid()==null?"":ctx.uid().getText();
	this.rex_Replace=this.strName.equals("")?"(?i)ADD\\s+"+ctx.indexFormat.getText():"";
	this.fmt_Replace=this.strName.equals("")?"ADD "+ctx.indexFormat.getText()+" %s ":"";
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByAddPrimaryKey(MySqlParser.AlterByAddPrimaryKeyContext ctx) { 
    //this syntax without selfdefine name whether add constraint name or not,default PRIMARY
    this.uidType="PK_";
	this.prefixObj="PK_";
    this.strName=ctx.uid()==null?"":ctx.uid().getText();
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByAddUniqueKey(MySqlParser.AlterByAddUniqueKeyContext ctx) { 
    this.uidType="UK_";
	this.prefixObj="UK_";
    this.strName=ctx.indexName==null?(ctx.name==null?"":ctx.name.getText()):ctx.indexName.getText();
	this.rex_Replace=this.strName.equals("")?"(?i)UNIQUE\\s+"+(ctx.indexFormat==null?"":ctx.indexFormat.getText()):"";
	this.fmt_Replace=this.strName.equals("")?"UNIQUE "+(ctx.indexFormat==null?"":ctx.indexFormat.getText())+" %s ":"";
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByAddForeignKey(MySqlParser.AlterByAddForeignKeyContext ctx) { 
    this.uidType="FK_";
	this.prefixObj="FK_";
    this.strName=ctx.name==null?"":ctx.name.getText();//(?i),\\s*[\\r\\n]*\\s*(CONSTRAINT)?\\s*\\S*\\s*FOREIGN
	this.rex_Replace=this.strName.equals("")?"(?i)ADD\\s+(CONSTRAINT)?\\s*\\S*\\s*FOREIGN":"";
	this.fmt_Replace=this.strName.equals("")?"ADD CONSTRAINT %s FOREIGN":"";
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByAddCheckTableConstraint(MySqlParser.AlterByAddCheckTableConstraintContext ctx) { 
    //mysql 5.7 unsupported
    this.uidType="CK_";
	this.prefixObj="CK_";
    this.strName=ctx.uid()==null?"":ctx.uid().getText();  
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByChangeColumn(MySqlParser.AlterByChangeColumnContext ctx) { 
    this.uidType="COLUMN";
	this.prefixObj="";	
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByModifyColumn(MySqlParser.AlterByModifyColumnContext ctx) {
    this.uidType="COLUMN";
	this.prefixObj="";	  
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByRenameIndex(MySqlParser.AlterByRenameIndexContext ctx) { 
    this.uidType="IDX_";
	this.prefixObj="IDX_"; 
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterByRename(MySqlParser.AlterByRenameContext ctx) { 
    //delele preobject table
	//this.tableMetaLst.remove(this.tableMetaLst.size()-1);
    this.uidType="RT_";
	this.prefixObj="T_";
    return visitChildren(ctx); 
  }//RENAME TABLE
  //20191014
  @Override 
  public List<Map<String,String>> getTableMetaLst(CharStream sqlStream,int order){
	this.sqlOrder=order;
    MySqlLexer lexer=new MySqlLexer(sqlStream);
	CommonTokenStream tokens=new CommonTokenStream(lexer);
	MySqlParser parser=new MySqlParser(tokens);
	ParseTree tree=parser.ddlStatement();//if sql error,this will print error to console,then contiue to visit(tree);	
	visit(tree);//++visitor
	return this.tableMetaLst; 
  };
  @Override 
  public String visitCreateEvent(MySqlParser.CreateEventContext ctx) {
	putOthers("CET_",ctx.fullId().getText(),null,"ET_");
	topMapPut(KEY_COMMENT,ctx.STRING_LITERAL()==null?"":ctx.STRING_LITERAL().getText());
    return visitChildren(ctx); 
  }
  @Override 
  public String visitAlterEvent(MySqlParser.AlterEventContext ctx) {
	if (ctx.RENAME()!=null){
	  putOthers("ET_",ctx.fullId(1).getText(),null,"ET_");	 
	}
	return visitChildren(ctx); 
  }
  @Override 
  public String visitCreateProcedure(MySqlParser.CreateProcedureContext ctx) { 
    for (MySqlParser.RoutineOptionContext rc: ctx.routineOption()){
	  if ( rc instanceof MySqlParser.RoutineCommentContext){
		  MySqlParser.RoutineCommentContext rcc= (MySqlParser.RoutineCommentContext)rc;
	    this.comment=rcc.STRING_LITERAL()==null?"":rcc.STRING_LITERAL().getText(); 
	  }
	}
    putOthers("CSP_",ctx.fullId().getText(),null,"SP_");
	topMapPut(KEY_COMMENT,this.comment);
	return "";
    //return visitChildren(ctx); 
  }
  @Override 
  public String visitCreateFunction(MySqlParser.CreateFunctionContext ctx) { 
    for (MySqlParser.RoutineOptionContext rc: ctx.routineOption()){
	  if ( rc instanceof MySqlParser.RoutineCommentContext){
		  MySqlParser.RoutineCommentContext rcc= (MySqlParser.RoutineCommentContext)rc;
	    this.comment=rcc.STRING_LITERAL()==null?"":rcc.STRING_LITERAL().getText(); 
	  }
	}
    putOthers("CF_",ctx.fullId().getText(),null,"F_");
	topMapPut(KEY_COMMENT,this.comment);
	return "";
    //return visitChildren(ctx); 
  }
  @Override 
  public String visitCreateView(MySqlParser.CreateViewContext ctx) {
	putOthers("CVW_",ctx.fullId().getText(),null,"VW_");
	return visitChildren(ctx); 
  }
  private String cutSpecialChar(String pStr){ 
	String tmp=pStr.replace("(","");
	tmp=tmp.replace(")","");
	return tmp;
  }
  @Override 
  public String visitCreateTrigger(MySqlParser.CreateTriggerContext ctx) { 
    putOthers("CTRG_",ctx.fullId(0).getText(),ctx.tableName().getText(),"TRG_");
	topMapPut(KEY_EVENT,ctx.INSERT()==null?(ctx.UPDATE()==null?ctx.DELETE().getText():ctx.UPDATE().getText()):ctx.INSERT().getText());
	return "";
  }
  private int getIdxMeta(String pCat,String pName,String pTable){
	//COLUMN&INDEX:(CAT,NAME,TABLE,DB);OTHERS:(CAT,NAME,DB)
	int i=0;
	int j=-1;
	boolean allEqual=false;
	String vCat=pCat==null?"":pCat;
	String vName=pName==null?"":pName;
	for (Map mp: this.tableMetaLst) {
	  allEqual=mp.get(KEY_CATAGORY).equals(vCat);
	  allEqual=allEqual==false?allEqual:(vName.equals("")?false:mp.get(KEY_NAME).equals(vName));
	  if (pTable!=null){
	    allEqual=allEqual==false?allEqual:mp.get(KEY_TABLE).equals(pTable);
	  }
	  if (allEqual){
		j=i;
		break;
	  }
	  i++;
    } 
    return j;	
  }
  private int getIdxLastCat(String pCatagory){
	int j=-1;
    for (int i = this.tableMetaLst.size() - 1; i >= 0; i--) {
 	  if (this.tableMetaLst.get(i).get(KEY_CATAGORY).equals(pCatagory)){
		j=i;
		break;
	  }     
    }	
    return j;	
  } 
  private void topMapPut(String pKeyObj,String pValueObj){
    this.tableMetaLst.get(this.tableMetaLst.size()-1).put(pKeyObj, pValueObj==null?"":pValueObj);	  
  }
  private void putColumn(String pCat,String pName,String pTypeLen,String pComment,String pTable,String pAlgorithm,String pLock){
	//SQLSN,CATAGORY,NAME,TYPELEN,COMMENT,COLUMNS,TABLE,PREFIX,EVENT,LOCK,ALGORITHM,ADVICE,
	if (pCat!=null&&!pCat.equals("")){
	  int idxMeta=getIdxMeta(pCat,pName,pTable);
	  if (idxMeta<0){ 
        this.tableMetaLst.add(new LinkedHashMap<String,String>());//String[0]=TYPE,String[1]=JSon string
	    idxMeta=this.tableMetaLst.size()-1;
	  }
	  this.tableMetaLst.get(idxMeta).put(KEY_SQLSN, String.valueOf(this.sqlOrder));
	  this.tableMetaLst.get(idxMeta).put(KEY_CATAGORY, pCat==null?"":pCat);
	  this.tableMetaLst.get(idxMeta).put(KEY_NAME, pName==null?"":pName);
	  this.tableMetaLst.get(idxMeta).put(KEY_TYPELEN, pTypeLen==null?"":pTypeLen);
	  this.tableMetaLst.get(idxMeta).put(KEY_COMMENT, pComment==null?"":pComment);
	  this.tableMetaLst.get(idxMeta).put(KEY_TABLE, pTable==null?"":pTable);
	  this.tableMetaLst.get(idxMeta).put(KEY_ALGORITHM, pAlgorithm==null?"":pAlgorithm);
	  this.tableMetaLst.get(idxMeta).put(KEY_LOCK, pLock==null?"":pLock);
	  this.tableMetaLst.get(idxMeta).put(KEY_ADVICE, "");
	}
  }
  private void putIndexPT(String pCat,String pName,String pColumns,String pTable,String pPrefix,String pAlgorithm,String pLock,String pREx,String pFmt){
	//SQLSN,CATAGORY,NAME,TYPELEN,COMMENT,COLUMNS,TABLE,PREFIX,EVENT,LOCK,ALGORITHM,ADVICE,
	if (pCat!=null&&!pCat.equals("")){
	  int idxMeta=getIdxMeta(pCat,pName,null);
	  if (idxMeta<0){ 
        this.tableMetaLst.add(new LinkedHashMap<String,String>());//String[0]=TYPE,String[1]=JSon string
	    idxMeta=this.tableMetaLst.size()-1;
	  }
	  this.tableMetaLst.get(idxMeta).put(KEY_SQLSN, String.valueOf(this.sqlOrder));
	  this.tableMetaLst.get(idxMeta).put(KEY_CATAGORY, pCat==null?"":pCat);
	  this.tableMetaLst.get(idxMeta).put(KEY_NAME, pName==null?"":pName);
	  this.tableMetaLst.get(idxMeta).put(KEY_COLUMNS, pColumns==null?"":pColumns);
	  this.tableMetaLst.get(idxMeta).put(KEY_TABLE, pTable==null?"":pTable);
	  this.tableMetaLst.get(idxMeta).put(KEY_PREFIX, pPrefix==null?"":pPrefix);
	  if (pName==null||pName.equals("")){
	    this.tableMetaLst.get(idxMeta).put(KEY_REX, pREx);
	    this.tableMetaLst.get(idxMeta).put(KEY_FMT, pFmt);
	  }
	  this.tableMetaLst.get(idxMeta).put(KEY_ALGORITHM, pAlgorithm==null?"":pAlgorithm);
	  this.tableMetaLst.get(idxMeta).put(KEY_LOCK, pLock==null?"":pLock);
	  this.tableMetaLst.get(idxMeta).put(KEY_ADVICE, "");
	}
  }
  private void putOthers(String pCat,String pName,String pTable,String pPrefix){
	//SQLSN,CATAGORY,NAME,TYPELEN,COMMENT,COLUMNS,TABLE,PREFIX,EVENT,LOCK,ALGORITHM,ADVICE,
	if (pCat!=null&&!pCat.equals("")){
	  int idxMeta=getIdxMeta(pCat,pName,null);
	  if (idxMeta<0){ 
        this.tableMetaLst.add(new LinkedHashMap<String,String>());//String[0]=TYPE,String[1]=JSon string
	    idxMeta=this.tableMetaLst.size()-1;
	  }
	  this.tableMetaLst.get(idxMeta).put(KEY_SQLSN, String.valueOf(this.sqlOrder));
	  this.tableMetaLst.get(idxMeta).put(KEY_CATAGORY, pCat==null?"":pCat);
	  this.tableMetaLst.get(idxMeta).put(KEY_NAME, pName==null?"":pName);
	  this.tableMetaLst.get(idxMeta).put(KEY_ADVICE, "");
	  if (pTable!=null){
		this.tableMetaLst.get(idxMeta).put(KEY_TABLE, pTable);  
	  }
	  if (pPrefix!=null){
	  this.tableMetaLst.get(idxMeta).put(KEY_PREFIX, pPrefix);
	  }  
	}
  }
}