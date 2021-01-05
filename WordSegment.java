/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By       Description
---  -------------  --------------  -----------  --------------   -------------------------------------
1.0  2019-08-26     xlzhu@ips.com                                 for wordSegment
1.1                                 2019-08-29   xlzhu@ips.com    redesign java class
*/
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

public class WordSegment extends DataSource{
  private List<String[]> wordSegmentLst;
  public WordSegment(){
	this.wordSegmentLst=new ArrayList<String[]>();
	for (String[] strArr :getDataList("",null)) {
      this.wordSegmentLst.add(strArr);
    }
  }
  
  @Override 
  protected List<String[]> getDataList(String pDataStr,SqlLogging pLogger){
	//Override&customize this function,maybe visit your data from database or other source
	String conifgKeyWStFilePath="FILE_WORDSEGMENT";
	String pathFile=getPropValue(conifgKeyWStFilePath);
	return getCSVDataList(pathFile);
  }
  
  public String getSegment(String desc){
	String rst="";
	int cntWS=0;
	HashMap<Integer, String> wordSegs = new HashMap<>();
	//1.get all wordsegment in desc
	for (String[] strArr :this.wordSegmentLst){
	  if (cntWS>2){
		break;
	  }
	  if (desc.toUpperCase().indexOf(strArr[0].toUpperCase())>-1){
		wordSegs.put(desc.toUpperCase().indexOf(strArr[0].toUpperCase()),strArr[0].toUpperCase());
		cntWS++;
	  }
	}
	//2.recombining wordsegment by order of index
    Set set=wordSegs.keySet();
    Object[] arrsS=set.toArray();
    Arrays.sort(arrsS);
    for(Object key:arrsS){
	  rst=rst+wordSegs.get(key);
    }  
    if (rst.equals("")){
	  rst=desc;	
	}
	return rst;	
  } 

}