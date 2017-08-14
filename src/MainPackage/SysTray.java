package MainPackage;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import Schema.Prozess;

public class SysTray {

	public static PopupMenu trayPopupMenu;
	public static MenuItem close = new MenuItem("Programm beenden");
	public ArrayList<Schema.Prozess> threads;

	public SysTray(ArrayList<Prozess> threads) {
		this.threads = threads;
	}

	// start of main method
	public static void newTrayIcon() {

		// checking for support
		if (!SystemTray.isSupported()) {
			Log.write("System Tray wird nicht unterstützt!");
			System.exit(1);
			return;
		}
		// get the systemTray of the system
		SystemTray systemTray = SystemTray.getSystemTray();

		// Lade Image-Icon aus der
		Ressource.ImgLoader imgL = new Ressource.ImgLoader();
		Image image = imgL.getIconImage();

		// popupmenu
		trayPopupMenu = new PopupMenu();

		// setting tray icon
		TrayIcon trayIcon = new TrayIcon(image, "Olivia Synchronisierung", trayPopupMenu);
		// adjust to default size as per system recommendation
		trayIcon.setImageAutoSize(true);

		try {
			systemTray.add(trayIcon);
		} catch (AWTException awtException) {
			awtException.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	public synchronized void updateThreads() {

		trayPopupMenu.removeAll();

		// 1t menuitem for popupmenu
		MenuItem action = new MenuItem("Konfiguration öffnen");
		action.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					if (IshopTransferMain.localmode) {
						// Öffne Lokale JSON in System-Editor
						File directory = new File(IshopTransferMain.ConfigURL);
						Desktop desktop = Desktop.getDesktop();
						if (directory.exists())
							desktop.open(directory);
					} else {
						// Öffne URL aus Browser
						URL htmlFile = new URL(IshopTransferMain.ConfigURL);
						Desktop.getDesktop().browse(htmlFile.toURI());
					}

				} catch (Exception e1) {
					Log.write("Keine Konfigfile zur Bearbeitung gefunden");
					e1.printStackTrace();
				}
			}
		});
		trayPopupMenu.add(action);

		MenuItem ReadLog = new MenuItem("Logfile öffnen");
		ReadLog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(Log.flog);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		trayPopupMenu.add(ReadLog);

		MenuItem bw_delta = new MenuItem("Delta Online-Bewirtschaftung");
		bw_delta.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(Log.fbew);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		trayPopupMenu.add(bw_delta);

		trayPopupMenu.addSeparator();

		// Auflistung der aktuell ausführenden Tasks

		for (Schema.Prozess thread : threads) {
			MenuItem marktThread = new MenuItem(thread.type);
			marktThread.disable();
			trayPopupMenu.add(marktThread);
		}

		trayPopupMenu.addSeparator();

		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Log.write("Schließe Programm manuell");
				System.exit(0);
			}
		});
		trayPopupMenu.add(close);

	}

}
