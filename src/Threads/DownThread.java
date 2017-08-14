package Threads;

import MainPackage.ConcurrencyController;
import MainPackage.Log;
import MainPackage.SFTP;
import MainPackage.SystemProps;

public class DownThread implements Runnable {

	public DownThread(Schema.MarktImport mImport, Schema.TransferServer server, String type, String proxyHost,
			int proxyPort) {
		super();

		this.mImport = mImport;
		this.type = type;
		this.server = server;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;

	}

	Schema.MarktImport mImport;
	Schema.TransferServer server;
	String type; // Umsatz oder Umlagerung
	String proxyHost;
	int proxyPort;

	@Override
	public synchronized void run() {

		do {

			// Thread wird gesperrt, damit dieser nicht in der Übertragung
			// unterbrochen wird
			ConcurrencyController.setThreadLock(this.mImport.marktName + "-" + this.type, true);

			// Lade die Datei(n) des Marktes mImport vom Server Olivia runter
			SFTP sftpConx = new SFTP(this.server, this.proxyHost, this.proxyPort);

			if (sftpConx.download(this.type, this.mImport) > 0) {
				// Es wurden Dateien heruntergeladen, daher kann der Thread
				// geschlossen werden und die Umsatzdateien in Ebus eingelesen
				// werden.

				// Batch initialisieren
				Schema.Batch b = null;
				if (this.type.equals("Umsatz")) {
					b = new Schema.Batch("cmd /c start " +SystemProps.UMS_BATCH_PATH +" " + this.mImport.marktName + " "
							+ this.mImport.marktnummerOnline, this.type);
				} 
				
				if (this.type.equals("Umlagerung")) {
					b = new Schema.Batch("cmd /c start " +SystemProps.UML_BATCH_PATH +" " + this.mImport.marktName + " "
							+ this.mImport.marktnummerStation, this.type);
				}

				// Batch-Aufruf zum Einlesen der Umsatz und Umlagerungsdateien
				if (b.run()) {
					Log.write(this.mImport.marktName + "- " + this.type + " wurde erfolgreich eingelesen");
				} else {
					Log.write(this.mImport.marktName + "- " + this.type + " konnte NICHT eingelesen werden");
				}

			}

			// Thread wird entsperrt
			ConcurrencyController.setThreadLock(this.mImport.marktName + "-" + this.type, false);

			// Schlafe 15 Minuten
			try {
				Thread.sleep(900000);
			} catch (InterruptedException e) {
				Log.write(e.toString());
			}

		} while (true);

	}

}
