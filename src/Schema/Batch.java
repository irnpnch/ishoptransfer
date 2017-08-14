package Schema;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import MainPackage.Log;
import MainPackage.SystemProps;

public class Batch {

	public Batch(String filename, String type) {
		super();
		this.filename = filename;
		this.type = type;
	}

	String filename; 
	String type;
	
	public boolean run(){
		Log.write("Rufe Batch " +this.filename +" auf!");
		try {
			Runtime.getRuntime().exec(this.filename);
		} catch (IOException e) {
			e.printStackTrace();
			return false; 
		}
		return true;
		
	}
	
	public void create(){
		
	    FileOutputStream fop = null;
	    File f = new File(this.filename);
	 
	    
	    try {
	    	   
		    if(f.exists()){
		    	return;
		    }
		    
		    f.createNewFile();
			fop = new FileOutputStream(f);
		    fop.write(getContent(this.type).getBytes());
		    fop.flush();
		    fop.close();
		} catch (FileNotFoundException e1) {
			Log.write("Can't create Batch "+f.getAbsolutePath()+" -> "+ e1.toString());
			System.exit(1);
		} catch (IOException e2) {
			Log.write("Can't create Batch "+f.getAbsolutePath()+" -> "+ e2.toString());
			System.exit(1);
		} 
		
	}

	private static String getContent(String type) {
		
		Log.write("Erstelle " +type +"-Batch");
		if(type.equals("Umsatz")){
			return SystemProps.UMS_BATCH_COMMAND; 
		}
		
		if(type.equals("Umlagerung")){
			return SystemProps.UML_BATCH_COMMAND;
		}
		
		if(type.equals("Config")){
			return SystemProps.CONFIG_TEMPLATE;
		}
		return null;
		
	}

}
