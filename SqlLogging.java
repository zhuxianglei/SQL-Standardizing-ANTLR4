/*
Ver  Creation_Time  Created_By      Update_Time  Updated_By   Description
---  -------------  --------------  -----------  ----------   -------------------------------------
1.1  2019-09-04     xlzhu@ips.com                             java logging

*/
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Handler;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;

public class SqlLogging{
  private Logger sloger;
  public SqlLogging(String pathFile){ 
    try{
	  this.sloger = Logger.getLogger("IPSSqlCheckLogging");
	  Handler[] handlers = this.sloger.getHandlers();
	  for (Handler hd:handlers){
	    this.sloger.removeHandler(hd);
	  }
	  this.sloger.setUseParentHandlers(false); 
	  SqlLogFormatter sLF=new SqlLogFormatter();	
      ConsoleHandler consoleHandler =new ConsoleHandler(); 
      //consoleHandler.setLevel(Level.ALL); 
	  consoleHandler.setFormatter(sLF);
      this.sloger.addHandler(consoleHandler);   
      String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date())+".log";  
	  FileHandler fileHandler = new FileHandler(pathFile+dateString,10000000,10,true); 
	  //fileHandler.setLevel(Level.ALL); 
	  fileHandler.setFormatter(sLF);
      this.sloger.addHandler(fileHandler);
	  //java.util.logging.SimpleFormatter.format="%1$tF %1$tH:%1$tM:%1$tS %2$s%n%4$s: %5$s%6$s%n";
    }catch(IOException e) {  
     e.printStackTrace();
    }	
  }
  
  public void log(String msg){
	sloger.info(msg);
  }
  public void log(String msg,char type){
	switch (type){
	  case 'w':
	    sloger.warning(msg);
		break;
	  case 's':
	    sloger.severe(msg);
		break;
	  default:
	    sloger.info(msg);
		break;
	}  
  }
  
  class SqlLogFormatter extends Formatter{
    @Override
    public String format(LogRecord record) {
      return new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()) + String.format("%-9s", "["+record.getLevel()+"]")+":"
             +record.getMessage()+"\r\n";
        }
        
    }
}