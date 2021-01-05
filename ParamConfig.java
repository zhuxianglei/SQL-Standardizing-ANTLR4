/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By       Description
---  -------------  --------------  -----------  --------------   -------------------------------------
1.1                                 2019-09-04   xlzhu@ips.com    ini config parameters
*/
import java.util.List;
import java.util.Properties;

public class ParamConfig extends DataSource{
  private Properties prop;
  public ParamConfig(){
	this.prop = getProps();
  }
  
  @Override 
  protected List<String[]> getDataList(String pDataStr,SqlLogging pLogger){
	//Override&customize this function,maybe visit your data from database or other source
	return null;
  }
  
  @Override
  public String getPropValue(String pkey){
    return this.prop.getProperty(pkey); 
  }
}