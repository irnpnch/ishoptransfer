package MainPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Schema.ConfigData;
import Schema.MarktImport;
import Schema.Prozess;
import Schema.TransferServer;
import Threads.DownThread;
import Threads.UpThread;

public class ConcurrencyController {
	
	private static ConcurrencyController instance;
	private static Schema.ConfigData config;
	private static Map<String, Prozess> executingTransferThreads = new HashMap<String, Prozess>();
	
	private ConcurrencyController(ConfigData config) {
		ConcurrencyController.config = config;
	}
	
	public static synchronized ConcurrencyController getInstance(ConfigData new_config){

		if(instance == null || !ConcurrencyController.config.equals(new_config)){
			// Initialisiere die ConcurrencyController-Instanz,
			// wenn diese noch nicht initialisiert wurde oder sich die Daten verändert haben
			
			instance = new ConcurrencyController(new_config);
		}
		
		// Gibt die Instanz zurück
		return instance;
		
	}



	@SuppressWarnings("rawtypes")
	public void refreshThreads() {

		ArrayList<String> killQue = new ArrayList<String>();

		// schließt alle offenen Threads, die nicht schreibgeschützt sind
		Iterator it = executingTransferThreads.entrySet().iterator();

		while (it.hasNext()) {

			Map.Entry pair = (Map.Entry) it.next();
			Prozess prozess = (Prozess) pair.getValue();

			if (!prozess.locked) {

				// Thread befindet sich gegenwärtig nicht in der Ausführung,
				// daher kann dieser getötet werden oder er hängt seit über 2
				// Stunden in der Ausführung
				killQue.add(pair.getKey().toString());

			}

		}

		// Zum Vermeiden einer ConcurrentModificationException wird jetzt erst
		// der Thread aus der toKill-Liste der CurrentThreads entfernt und
		// geschlossen

		if (killQue.size() > 0) {
			for (String thread : killQue) {
				closeThread(thread);
			}
		}

		for (Schema.MarktImport marktImport : ConcurrencyController.config.MarktImport) {

			if (executingTransferThreads.get(marktImport.marktName + "-Upload") == null) {
				newThread(marktImport.marktName + "-Upload", marktImport);
			}

			if (executingTransferThreads.get(marktImport.marktName + "-Umlagerung") == null) {
				newThread(marktImport.marktName + "-Umlagerung", marktImport);
			}

			if (executingTransferThreads.get(marktImport.marktName + "-Umsatz") == null) {
				newThread(marktImport.marktName + "-Umsatz", marktImport);
			}

			// Führe die Bewirtschaftungslogik aus
			DB_Queries.setbew(ConcurrencyController.config.oracleIP, marktImport);

		}

		updateThreadReference();
	}

	private static void updateThreadInMap(String threadname, Prozess prozess) {
		executingTransferThreads.remove(threadname);
		executingTransferThreads.put(threadname, prozess);
	}

	private void newThread(String threadname, MarktImport importMarkt) {

		Runnable r = null;

		if (threadname.contains("Upload")) {
			// Neuer Uplaod Thread für Import
			r = new UpThread(importMarkt, getServerFromName("Gertrud"), ConcurrencyController.config.proxyHost, ConcurrencyController.config.proxyPort);
		}

		if (threadname.contains("Umsatz")) {
			// Neuer Download Thread für Umsatz
			r = new DownThread(importMarkt, getServerFromName("Olivia"), "Umsatz", ConcurrencyController.config.proxyHost,
					ConcurrencyController.config.proxyPort);
		}

		if (threadname.contains("Umlagerung")) {
			// Neuer Download Thread für Umlagerung
			r = new DownThread(importMarkt, getServerFromName("Olivia"), "Umlagerung", ConcurrencyController.config.proxyHost,
					ConcurrencyController.config.proxyPort);
		}

		if (r == null) {
			Log.write(threadname + " konnte nicht gestartet werden (newTThread.Runnable r == null)");
			return;
		}

		Thread t = new Thread(r);
		executingTransferThreads.put(threadname, new Prozess(t, threadname.split("-")[1], false));
		t.start();
	}

	public static synchronized void setThreadLock(String threadname, boolean locked) {

		if (executingTransferThreads.get(threadname) != null) {
			// Updating Thread Reference
			Prozess p = executingTransferThreads.get(threadname);
			p.locked = locked;
			updateThreadInMap(threadname, p);
		}

	}

	@SuppressWarnings("rawtypes")
	private synchronized static void updateThreadReference() {

		Iterator it = executingTransferThreads.entrySet().iterator();
		ArrayList<Schema.Prozess> threads = new ArrayList<Schema.Prozess>();
		while (it.hasNext()) {

			Map.Entry pair = (Map.Entry) it.next();
			Schema.Prozess thread = (Prozess) pair.getValue();
			thread.type = pair.getKey().toString();
			threads.add(thread);

		}

		SysTray s = new SysTray(threads);
		s.updateThreads();

	}

	@SuppressWarnings("deprecation")
	public synchronized static void closeThread(String threadname) {
		if (executingTransferThreads.get(threadname) != null) {
			executingTransferThreads.get(threadname).t.stop();
			executingTransferThreads.remove(threadname);
			updateThreadReference();

		} else {
			Log.write("Fehler: closeThread konnte den Thread '" + threadname
					+ "' nicht schließen, da dieser nicht in executingThreads hinterlegt ist");
		}
	}

	TransferServer getServerFromName(String servername) {

		for (int i = 0; i < ConcurrencyController.config.TransferServer.length; i++) {

			if (ConcurrencyController.config.TransferServer[i].template.equals(servername)) {
				return ConcurrencyController.config.TransferServer[i];
			}

		}
		Log.write("getServerFromName(String " + servername
				+ ") konnte kein Ergebnis liefern. -> Fehlender Config-Eintrag");
		return null;

	}

}
