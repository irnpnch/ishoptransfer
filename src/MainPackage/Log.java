package MainPackage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Log {

	// TODO: Logfile in Config unterbringen
	public static File flog = new File(SystemProps.getLogFilePath());
	public static File fbew = new File(SystemProps.getBewFilePath());
	public static String lastContent = "";

	public synchronized static void write(String content) {

		if (content.equals(lastContent)) {

			// Letzte Fehler-Meldung = aktuelle Fehlermeldung...
			// Um das Log nicht mit redundanten / sich wiederholenden Infos zu
			// füllen

		} else {

			// System.out Nur für Konsolenbetrieb,
			// Bei Start über javaw nicht relevant

			System.out.println(content);
			lastContent = content;

			try {

				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(flog, true)));
				out.println("[" + getTimestamp() + "] " + content + "\n");
				out.close();

			} catch (IOException e) {
				System.out.println("Log.write Fehler:\n\n" + e + "\n\n");
			}
		}
	}

	public synchronized static void writeBewirtLog(ArrayList<Schema.Bewirtschaftung> bew) {
				
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date date = new Date();
	
		try {
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fbew, true)));
			if(fbew.length()==0){
				//Schreibe Header, wenn Datei Leer ist
				out.println("Datum;neues Kennzeichen;GH-Nr;Bezeichnung;Markt"); 
			}

			for (Schema.Bewirtschaftung b : bew) {
				out.println(dateFormat.format(date)+";"+b.toString());
			}
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getTimestamp() {

		return new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss", Locale.GERMAN).format(new Date());

	}

}
