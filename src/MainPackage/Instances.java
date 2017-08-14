package MainPackage;

import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JOptionPane;

public class Instances {

	static int socket = 666; // standartisierter Doom-Port, daher (hoffentlich)
								// immer frei im EH :D

	static boolean singleInstance() {

		// Verhindert das Starten einer zweiten Instanz des Programms durch das
		// Blockieren des Sockets

		try {

			@SuppressWarnings({ "resource", "unused" })
			ServerSocket blockedSocket = new ServerSocket(socket);

		} catch (IOException e) {

			Log.write("Blockierter System-Socket: Starten einer zweiten Instanz verhindert.");
			JOptionPane.showMessageDialog(null, "Programm wird bereits ausgeführt!", "Olivia Sync: Socket blockiert",
					JOptionPane.ERROR_MESSAGE);

			return true;
		}

		Log.write("Starte Programm auf Sockel " + socket +". Initialisiere Prozesse.");
		return false;

	}

	public static void newGlobalConfigThread(String URL) {

		Log.write("Lese Config aus " + URL);
		Runnable r = new Threads.ConfigThread(URL);
		Thread cThread = new Thread(r);
		cThread.start();

	}

}
