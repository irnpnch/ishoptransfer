package MainPackage;
public class IshopTransferMain {

	public static boolean localmode = true;
	public static String ConfigURL = "";

	public static void main(String[] args) {
		
		/* Mehrfilialisten im HTTP-Modus über URL spezifizieren
		 * 
		 * for(int i = 0 ; i < args.length; i++){
		 * 
		 * if(args[i].equals("local"))localmode = true;
		 * 
		 * }
		 * 
		 */

		if (localmode) {
			ConfigURL = SystemProps.LOCAL_CONFIG_PATH;
		} else {
			ConfigURL = "http://10.59.101.180/intranet/aim_dev/lib/phpConnector/DFUE/config.json";
		}

		if (Instances.singleInstance()) {

			System.exit(0);

		} else {

			// Lege SystemTray-Icon an
			SysTray.newTrayIcon();
			// Abrufen globaler Konfigurationsdaten
			// Dieser Thread steuert die Prozessaufrufe für seperate
			// Transfer-Jobs
			Instances.newGlobalConfigThread(ConfigURL);

		}

	}

}
