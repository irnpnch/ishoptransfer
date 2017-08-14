package MainPackage;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SystemProps {

	// JAR-Spezifische Konfigurationsdaten
	private final static String JAR_VERSION = "609";
	private final static String ROOTPATH = "C:/ehgnord/bin/ishop/";
	private final static String LOG_FILE_PATH = ROOTPATH+"ISHOP_JAVAW.LOG";
	private final static String LOG_BEW_PATH =  ROOTPATH+"AENDERUNG_BEWIRTSCHAFTUNG.CSV";
	public final static String LOCAL_CONFIG_PATH = ROOTPATH + "config.json";
	
	//requiredDirs 4 ConfigThread
	private final static String UMSATZ_DIR = ROOTPATH+"UMS";
	private final static String UMLAGERUNG_DIR = ROOTPATH+"UML";
	private final static String EBUS_STAMMDATEN_DIR = ROOTPATH+"TXT";
	private final static String EBUS_STAMMDATEN_ARCHIV_DIR = EBUS_STAMMDATEN_DIR +"/SAVED";
	
	//UMS -> 
	public final static String UMS_BATCH_PATH = ROOTPATH+"execUMS.bat";
	public final static String UML_BATCH_PATH = ROOTPATH+"execUML.bat";	
	
	public final static String UMS_BATCH_COMMAND = "@echo off\r\nC:\r\nCD/EBUSWIN/EXE\r\nEBUSWINPROZESS -APP=EBUSSERVICE -M BEFEHL=TAGESENDE_ISHOP  MARKT=2\r\nexit";
	public final static String UML_BATCH_COMMAND = "@echo off\r\nC:\r\nCD/EBUSWIN/EXE\r\nEBUSWINPROZESS -APP=EBUSSERVICE -M BEFEHL=MDE_VA\r\nexit";
	public final static String CONFIG_TEMPLATE = Schema.ConfigData.getConfigTemplate();
	
	public final static String SFTP_SAVE_DIR = "transfer"; // Speichert Umsatz und Umlagerungsdateien in diesem Ordner
	
	public static String getUmsatzDir() {
		return UMSATZ_DIR;
	}

	public static String getUmlagerungDir() {
		return UMLAGERUNG_DIR;
	}

	public static String getEbusStammdatenDir() {
		return EBUS_STAMMDATEN_DIR;
	}

	public static String getEbusStammdatenArchivDir() {
		return EBUS_STAMMDATEN_ARCHIV_DIR;
	}

	private final static String REMOTE_SERVER = "http://10.59.101.180/intranet/aim_dev/lib/phpConnector/pushResponse.php";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getJRE() {

		Properties pro = System.getProperties();

		Set set = pro.entrySet();
		String returnVal = "";

		Iterator<Map.Entry<String, String>> itr = set.iterator();
		while (itr.hasNext()) {

			Map.Entry ent = itr.next();

			if (ent.getKey().toString().equals("java.runtime.version")) {
				returnVal = ent.getKey() + ": " + ent.getValue();
			}
		}

		return returnVal;
	}

	public static String getJAR() {
		return JAR_VERSION;
	}

	public static String getLogFilePath() {
		return LOG_FILE_PATH;
	}

	public static String getRemoteServer() {
		return REMOTE_SERVER;
	}

	public static String getBewFilePath() {
		return LOG_BEW_PATH;
	}

}
