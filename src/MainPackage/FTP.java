package MainPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTP {

	public FTP(Schema.TransferServer server, String proxyHost, int proxyPort) {
		super();
		this.server = server;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	Schema.TransferServer server;
	String proxyHost;
	int proxyPort;

	public boolean upload(Schema.MarktImport importMarkt) {

		boolean returnValue = true;

		Log.write("Upload " + importMarkt.transferpath + " zu " + this.server.domain + ":" + this.server.port
				+ " in das Verzeichnis " + importMarkt.uploadRemoteVerzeichnis + " über den Proxy " + this.proxyHost + ":"
				+ this.proxyPort);

		try {

			String serverAddress = this.server.domain;
			String userId = this.server.user;
			String password = this.server.password;
			FTPClient ftp;

			if (this.server.applyProxy) {
				// Verwende Proxy für externe Kommunikation
				ftp = new FTPHTTPClient(this.proxyHost, this.proxyPort, "", "");
			} else {
				System.out.println("Verwende keinen Proxy bei " + this.server.template);
				// lokale Adresse, kein Proxy benötigt
				ftp = new FTPHTTPClient(this.server.domain, this.server.port);
			}

			ftp.connect(serverAddress);

			// Login
			if (!ftp.login(userId, password)) {
				Log.write("FTP-Login mit Credentials " +userId +":"+password+ " fehlgeschlagen");
				ftp.logout();
				return false;
			}

			// Test ob Server erreichbar ist
			int reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {

				Log.write("Server nicht erreichbar: " + this.server.domain + ":" + this.server.port + " | Reply-Code: "
						+ reply);
				ftp.disconnect();
				return false;

			}

			// FTP-Verbindungseigenschaften (verhindert Verbindungsabbrüche)
			ftp.enterLocalPassiveMode();
			ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
			ftp.setKeepAlive(true);
			System.out.println("remote Dir uploadRemoteVerzeichnis: "+ importMarkt.uploadRemoteVerzeichnis);
			// Remote Verzeichnis
			ftp.changeWorkingDirectory(importMarkt.uploadRemoteVerzeichnis);

			// Lokaler Transfer-File
			InputStream input = new FileInputStream(new File(importMarkt.transferpath));

			// Lokalen Inputfile(input) in das Remoteverzeichnis(uploadRemoteVerzeichnis) schieben
			String remoteFilename = "ISHOP_"+System.currentTimeMillis()+"_"+importMarkt.marktName+".ZIP";
			ftp.storeFile(remoteFilename, input);

			// Datei umbenennen			
			if (ftp.rename(remoteFilename, remoteFilename.substring(0, remoteFilename.length() - 4) + "_COMPLETE.ZIP")) {
				Log.write("Datei " +remoteFilename +" wurde erfolgreich umbenannt als '_COMPLETE.ZIP'");
			} else {
				Log.write("Datei " +remoteFilename +" konnte nicht erfolgreich umbenannt werden.");
				// TODO: Löschen des Bereits hochgeladenen Files
				returnValue = false;
			}

			// close the stream
			input.close();
			ftp.logout();
			ftp.disconnect();

		} catch (Exception ex) {
			Log.write(importMarkt.marktName + " Fehler bei Dateiupload " + ex);
			ex.printStackTrace();
			returnValue = false;

		}

		// Wenn in der Zwischenzeit kein Fehler aufgetreten ist bleibt der
		// Rückgabewert=true
		return returnValue;

	}

}
