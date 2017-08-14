package Schema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

import MainPackage.Log;

public class MarktImport {

	public MarktImport(String stationILN, String KDNR, String marktName, String marktnummerStation,
			String marktnummerOnline, String exportFile, String uploadRemoteVerzeichnis, String region) {

		super();
		this.stationILN = stationILN;
		this.KDNR = KDNR;
		this.marktName = marktName;
		this.marktnummerStation = marktnummerStation;
		this.marktnummerOnline = marktnummerOnline;
		this.exportFile = exportFile;
		this.uploadRemoteVerzeichnis = uploadRemoteVerzeichnis;
		this.region = region;

	}

	public String stationILN; // PK
	public String KDNR;
	public String marktName;
	public String marktnummerStation;
	public String marktnummerOnline;
	public String exportFile; // ISHOP-TXT-Pfad (z.B: Ebuswin/exe/gh)
	public String uploadRemoteVerzeichnis; // Remote Verzeichnis auf FTP-Server
	public String transferpath; // Dateiname der ZipDate, bevor sie verschickt
								// und archiviert wird
	public long fileSize; // Dateigröße in bit
	public String region;

	public boolean changeEncoding() {
		// keine Anpassung nötig, da XSLT Treiber die Zeichencodierung auf UTF-8 anpasst.
		return true;
	}

	public boolean zip() throws IOException {

		// Checken ob die Datei vorliegt
		File f = new File(this.exportFile);

		this.fileSize = f.length();
		if (this.fileSize < 1000 || !f.exists() || exportFileLocked()) {
			// Datei leer, liegt nicht vor oder ist schreibgeschützt
			return false;
		}

		try {
			// Warte 5 Minuten (300000ms), sodass die Datei zuende geschrieben
			// werden kann
			Thread.sleep(300000);
		} catch (InterruptedException e) {
		}

		byte[] buffer = new byte[1024];

		// Datum für .csv-Export
		DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
		Date date = new Date();
		String dToday = dateFormat.format(date);

		// Erzeuge File und Zip-Stream
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		FileInputStream in = null;

		// Baue den Dateipfand für Zip-Archiv zusammen
		String rootPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(File.separator));
		String zipPath = rootPath + "\\ISHOP_" + System.currentTimeMillis() + "_" + this.marktName + ".zip";

		if (!this.exportFileLocked()) {

			// Erzeuge Zip-Output-Stream
			fos = new FileOutputStream(zipPath);
			zos = new ZipOutputStream(fos);
			ZipEntry ze = new ZipEntry("ISHOP_" + dToday + "_" + this.marktName + ".csv");
			zos.putNextEntry(ze);

			// Befülle Outputstream mit this.exportFile
			in = new FileInputStream(this.exportFile);
			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			in.close();
		}

		// Schließen der Streams
		try {
			zos.closeEntry();
			zos.close();
		} catch (IOException e) {
			Log.write(e + ": MarktImport.zip(" + this.marktName + ") - Fehler beim Schließen des Filestreams");
			return false;
		}

		// Löschen des Input FlatFiles
		deleteExportFile(this.exportFile);
		this.transferpath = zipPath;
		Log.write(this.exportFile + " wurde erfolgreich gezippt");
		return true;
	}

	public boolean sichern() {
		System.out.println("TransferPath: " + transferpath);
		File f = new File(transferpath);

		if (f.renameTo(new File(MainPackage.SystemProps.getEbusStammdatenArchivDir() + "//" + f.getName()))) {

			Log.write("Sicherung von " + f.getAbsolutePath() + " in das Verzeichnis "
					+ MainPackage.SystemProps.getEbusStammdatenArchivDir());

			return true;

		} else {

			Log.write("FEHLER: Sicherung von " + f.getName() + " fehlgeschlagen."
					+ " Konnte die Datei nicht in das Verzeichnis "
					+ MainPackage.SystemProps.getEbusStammdatenArchivDir() + " verschieben.");

			return false;

		}

	}

	private boolean deleteExportFile(String filename) {

		File f = new File(filename);

		try {

			if (f.delete()) {

				Log.write("EBUS-Export wurde erfolgreich gelöscht: " + this.exportFile);
				return true;

			} else {

				Log.write("EBUS-Export konnte nicht gelöscht werden: " + this.exportFile);
				return false;
			}

		} catch (Exception e) {

			Log.write("EBUS-Export konnte nicht gelöscht werden: " + this.exportFile + "\n\nException:\n\n" + e);
			return false;

		}

	}

	@Override
	public String toString() {

		return "\n\n" + "KDNR: " + this.KDNR + "\n" + "marktName: " + this.marktName + "\n" + "marktnummerStation: "
				+ this.marktnummerStation + "\n" + "marktnummerOnline: " + this.marktnummerOnline + "\n"
				+ "exportFile: " + this.exportFile + "\n\n";
	}

	boolean exportFileLocked() {
		boolean returnValue = false;
		try {
			FileInputStream in = new FileInputStream(this.exportFile);
			in.close();

		} catch (FileNotFoundException e) {
			returnValue = true;
		} catch (IOException e) {
			returnValue = true;
		}

		return returnValue;
	}

}
