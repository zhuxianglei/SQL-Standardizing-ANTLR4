/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By       Description
---  -------------  --------------  -----------  --------------   -------------------------------------------------------
1.0  2019-08-26     xlzhu@ips.com                                 basic class for data source of csv&keyvalue&sqlfile;
1.1                                 2019-08-29   xlzhu@ips.com    redesign java class
*/
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DataSource {
  private static String REGEX_SEMI = ";\\s*\\r{0,1}\\n{0,1}"; 
  //implement getting csv data function
  protected List<String[]> getCSVDataList(String pathFile){
    List<String[]> dataListCSV= new ArrayList<String[]>();
    String lineFile;
	String encodingFile="UTF-8";
	String charSplit=",";
	try{
	  File fl = new File(pathFile); 
	  InputStreamReader read = new InputStreamReader(new FileInputStream(fl),encodingFile);       
      BufferedReader reader=new BufferedReader(read); 
      while ((lineFile = reader.readLine()) != null) {
        String fields[] = lineFile.split(charSplit);
        dataListCSV.add(fields);
      }
	  read.close();
	  } catch (IOException e) {  
         e.printStackTrace();  
      }
      return dataListCSV;
    }
	
  //implement getting config.properties
  protected Properties getProps(){
	String CONFIG_FILE="config.properties";
	Properties prop = new Properties();
	try{
      prop.load(new FileInputStream(CONFIG_FILE));
    } catch (IOException e) {  
        e.printStackTrace();  
    }
	return prop;
  }
  protected String getPropValue(String pkey){
	return getProps().getProperty(pkey);
  }
  
  //implement gettting pos of delimiter
  protected int getDelimiterStartPos(String pStr,String delimiter){
    String REGEX = "(?i)"+delimiter+"\\s*\\r{0,1}\\n{0,1}"; 
	Pattern prm = Pattern.compile(REGEX);
    Matcher mrm = prm.matcher(pStr);
	//mrm.matches();
	if (mrm.find()){
	  return mrm.start();
	}else{
	  return -1;
	}
  }
  protected int getDelimiterEndPos(String pStr,String delimiter){
    String REGEX = "(?i)"+delimiter+"\\s*\\r{0,1}\\n{0,1}"; 
	Pattern prm = Pattern.compile(REGEX);
    Matcher mrm = prm.matcher(pStr);
	//mrm.matches();
	if (mrm.find()){
	  return mrm.end();
	}else{
	  return -1;
	}
  } 

  protected String decorateSpecialChar(String str){
	  String stmp=str.replaceAll("\\(","\\\\(");
	  return "(?i)"+stmp.replaceAll("\\)","\\\\)");  
  } 
  //please customize this function,maybe visit your data from database or other source
  protected abstract List<?> getDataList(String pDataStr,SqlLogging pLogger);
}