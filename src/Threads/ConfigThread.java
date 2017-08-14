package Threads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import MainPackage.ConcurrencyController;
import MainPackage.Log;
import MainPackage.IshopTransferMain;
import MainPackage.SystemProps;
import Schema.Batch;
import Schema.ConfigData;
import Schema.MarktImport;

public class ConfigThread implements Runnable {

	// Dieser Thread dient dem stündlichen Abrufen der Config und Warten der
	// offenen Threads
	
	private String URL;
	private Gson jsonParser = new Gson();
	private ConcurrencyController concCtrl;
	public static ConfigData config; 

	public ConfigThread(String URL) {
		this.URL = URL;
	}

	@Override
	public void run() {
				
		do {
			
			config = readConfig();	
			checkDirs(); 
			concCtrl = ConcurrencyController.getInstance(config);
			MainPackage.SysTray.close.setEnabled(true); // Schaltfläche'Schließen' ist klickbar
			concCtrl.refreshThreads();
			try {
				//Alle drei Stunden aktualisieren
				Thread.sleep(10800000);
			} catch (InterruptedException e) {
				Log.write("ConfigThread InterruptedException");
				System.exit(0);
			}
			
		} while (true);

	}

	private void checkDirs() {
		
		//Überprüfen ob alle nötigen Verzeichnisse aus den SystemProps existieren 
		//Wenn nein, dann erstellt checkDirs() diese
		
		ArrayList <File> requiredDirs = new ArrayList<File>();
		
		requiredDirs.add(new File(SystemProps.getUmlagerungDir()));
		requiredDirs.add(new File(SystemProps.getUmsatzDir()));
		requiredDirs.add(new File(SystemProps.getEbusStammdatenDir()));
		requiredDirs.add(new File(SystemProps.getEbusStammdatenArchivDir()));

		for(File f : requiredDirs){
		
			if (!f.exists()) {
				Log.write("Erstelle das Verzeichnis " +f.getAbsolutePath());
				f.mkdir();
			}
		}
		
		// Erstelle benötigte Batches
		
		Batch umsab = new Batch(SystemProps.UMS_BATCH_PATH, "Umsatz");
		Batch umlab = new Batch(SystemProps.UML_BATCH_PATH, "Umlagerung");
			
		umsab.create(); 
		umlab.create();
		
	}

	private Schema.ConfigData readConfig() {
		
		String json = null;
			
		try {

			if (IshopTransferMain.localmode) {
				// Lokale FILE-CONFIG
				
				json = getFileContent(this.URL);
								
			} else {
				json = getHTML(this.URL);
			}

			if (json == null)
				throw new Exception("Der File '" + this.URL + "' konnte nicht ausgelesen werden");

			Schema.ConfigData conf = jsonParser.fromJson(json, ConfigData.class);

			if (conf.MarktImport.length == 0)
				throw new Exception("Keine Olivia-Märkte in Cofig " + this.URL + " hinterlegt");
			if (conf.TransferServer.length == 0)
				throw new Exception("Keine TransferServer in Cofig " + this.URL + " hinterlegt");
			
			return conf;

		} catch (IOException ex) {
			Log.write("Keine Verbindung zum OliviaServer: " + URL + "\nConfig-Inhalt: " + json);
			System.exit(1);
		} catch (JsonParseException e) {
			Log.write("JsonParser-Fehlerhafte Config-Datei " + json );
			if (!IshopTransferMain.localmode)
				new Schema.Response("N.A.", "failure", "ConfigThread.run().readJSON", "Fehlerhaftes Json Format",
						"N.A.").post();
			System.exit(1);
		} catch (Exception e) {
			Log.write("Konfigurationsdatei beschädigt.\n DefaultException in ConfigThread.run().readJSON: " + e.toString());
			System.exit(1);
		}
		return null;
	}

	private String getFileContent(String path) {

		String content = "";
		BufferedReader br = null;
		
		File f = new File(path); 
		if(!f.exists()){
			Batch b = new Batch(path, "Config");
			b.create();
			JOptionPane.showMessageDialog(null, "Bitte " +SystemProps.LOCAL_CONFIG_PATH +" mit marktspezifischen Informationen füllen", "Olivia-Installation",
					JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			String st;
			while ((st = br.readLine()) != null) {
				content = content + "\n" + st;
			}
		} catch (IOException e) {
			Log.write("Config-Datei konnte nicht ausgelesen werden: " + e);
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			Log.write("Fehler beim Schließen des BufferedReaders der Datei: " + path);
			e.printStackTrace();
		}
		return content;

	}

	public static String getHTML(String urlToRead) {

		try {

			StringBuilder result = new StringBuilder();
			URL url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			// new Response("IP","success", "ConfigThread", "STIL ALIVE",
			// "").post();
			return result.toString();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public synchronized static MarktImport getOliviaMarktFromName(String inputName) {
		
		for (MarktImport markt : config.MarktImport) {
			if (markt.marktName.equals(inputName)) {
				return markt;
			}
		}
		return null;
	}
}
