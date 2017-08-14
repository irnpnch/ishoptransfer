package Threads;

import MainPackage.ConcurrencyController;
import MainPackage.FTP;
import MainPackage.Log;
import Schema.MarktImport;
import Schema.TransferServer;

public class UpThread implements Runnable {

	public UpThread(MarktImport mImport, TransferServer transferServer, String proxyHost, int proxyPort) {
		super();
		this.mImport = mImport;
		this.transferServer = transferServer;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;

	}

	Schema.MarktImport mImport;
	Schema.TransferServer transferServer;
	String proxyHost;
	int proxyPort;
	
	//Arbeitsschritte
	private static boolean changeEncoding;
	private static boolean gezippt;
	private static boolean uebertragen;
	private static boolean gesichert;

	@Override
	public void run() {

		initProgress();

		do {
			//ThreadLock setzten, damit der ConcurrencyController keine Übertragung unterbricht
			ConcurrencyController.setThreadLock(this.mImport.marktName + "-Upload", true);

			if(!changeEncoding){
				changeEncoding = this.mImport.changeEncoding();
			}	
	
			// Komprimierung
			if (changeEncoding && !gezippt) {

				// Zippe, wenn noch nicht gezippt wurde
				try {
					if (this.mImport.zip()) {
						// Erfolgreich gezippt
						gezippt = true;
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			// Uebertragung

			if (gezippt & !uebertragen) {

				// Uebertrage, wenn noch nicht uebertragen wurde
				// Lädt den importFile zum Server Gertrud hoch
				FTP ftpConx = new FTP(this.transferServer, this.proxyHost, this.proxyPort);
				if (ftpConx.upload(this.mImport)) {
					uebertragen = true;
				}

			}

			// Sicherung des Zip-Archivs im Ordner config.exportfile / ISHOP
			if (uebertragen & !gesichert) {
				
				// zips in sicherungsarchiv ishop verschieben wenn bereits
				// übertragen und gezippt wurde
				if (this.mImport.sichern()) {
					gesichert = true;
				}

			}

			if (changeEncoding & gezippt & uebertragen & gesichert) {
				
				//ThreadLock entsperren
				ConcurrencyController.setThreadLock(this.mImport.marktName + "-Upload", false);
				MainPackage.Log.write(this.mImport.marktName + " wurde erfolgreich gezipt, übertragen & gesichert.");
				new Schema.Response(this.mImport.stationILN, "success", "Import", "erfolgreich uebertragen",this.mImport.marktName).post();
				initProgress();
			}

			try {

				// Wartet eine viertelstunde vor
				// erneuter Schleifen-Iteration
				Thread.sleep(900000);

			} catch (InterruptedException e) {
				Log.write(e.toString());
			}

		} while (true);

	}

	private void initProgress() {
		
		// Arbeitsschritt-Status (true = erledigt, false = noch zu erledigen)
		changeEncoding = false;
		gezippt = false;
		uebertragen = false;
		gesichert = false;
		
	}

}
