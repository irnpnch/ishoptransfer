package MainPackage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import Schema.MarktImport;

public class SFTP {

	public SFTP(Schema.TransferServer server, String proxyHost, int proxyPort) {
		super();
		this.server = server;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	Schema.TransferServer server;
	String proxyHost;
	int proxyPort;
	public static String save_dir = "transfer";

	public synchronized int download(String type, Schema.MarktImport importMarkt) {

		String SFTPWORKINGDIR = "/transfer/export/backward/" + importMarkt.region + "/" + importMarkt.KDNR;
		if (type.equals("Umlagerung")) {
			SFTPWORKINGDIR = SFTPWORKINGDIR + "/rearrangement";
		} else {
			SFTPWORKINGDIR = SFTPWORKINGDIR + "/turnover";
		}

		ChannelSftp channelSftp = null;

		try {
			channelSftp = getChannelSftp(this.server.user, this.server.password, this.server.domain, 22, this.proxyHost,
					this.proxyPort);
		} catch (JSchException e1) {
			Log.write("Couldn't get SFTP Channel: " + e1);
			return 0;
		}

		try {
			channelSftp.cd(SFTPWORKINGDIR);
		} catch (SftpException e) {
			Log.write("Couldn't change Directory on Remote Server: " + SFTPWORKINGDIR + " ->" + e);
		}

		String inhalt = "";
		try {
			inhalt = channelSftp.ls(SFTPWORKINGDIR).toString();
		} catch (SftpException e) {
			Log.write("Couldn't List Content in: " + SFTPWORKINGDIR + " ->" + e);
		}

		createSaveDir(SFTPWORKINGDIR + "/" + save_dir, channelSftp);
		int files_downloaded = downloadAllMarktFiles(type, importMarkt, channelSftp, inhalt, SFTPWORKINGDIR);

		if (channelSftp != null) {
			channelSftp.disconnect();
		}

		return files_downloaded;

	}

	private ChannelSftp getChannelSftp(String SFTPUSER, String SFTPPASS, String SFTPHOST, int SFTPPORT,
			String proxy_host, int proxy_port) throws JSchException {

		JSch jsch = new JSch();
		Session session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
		session.setPassword(SFTPPASS);
		if (this.server.applyProxy)
			session.setProxy(new ProxyHTTP(proxy_host, proxy_port));
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();
		Channel channel = session.openChannel("sftp");
		channel.connect();
		return (ChannelSftp) channel;
	}

	private synchronized static int downloadAllMarktFiles(String type, MarktImport importMarkt, ChannelSftp channelSftp,
			String dir, String SFTPWORKINGDIR) {

		ArrayList<String> filenames; // alle anstehenden Files

		if (type.equals("Umlagerung")) {
			filenames = listFiles(dir, "TAB");
		} else {
			filenames = listFiles(dir, "TXT");
		}

		// Lädt alle Filenamen des Verzeichnisses aus
		for (String fname : filenames) {

			byte[] buffer = new byte[1024];
			BufferedInputStream bis;
			BufferedOutputStream bos;

			try {
				bis = new BufferedInputStream(channelSftp.get(fname));
				File newFile = getLocalFileName(type, importMarkt);
				OutputStream os = new FileOutputStream(newFile, true);
				bos = new BufferedOutputStream(os);
				int readCount;
				while ((readCount = bis.read(buffer)) > 0) {
					bos.write(buffer, 0, readCount);
				}

				bis.close();
				bos.close();

				// Sichern der übertragenen Datei
				channelSftp.rename(SFTPWORKINGDIR + "/" + fname, SFTPWORKINGDIR + "/" + save_dir + "/" + fname);
			} catch (SftpException e1) {
				e1.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.write(fname + ": erfolgreich heruntergeladen und auf gesichert");
		}
		return filenames.size();
	}

	public void createSaveDir(String transfer_path, ChannelSftp channelSftp) {

		// Save-Ordner erstellen, falls noch nicht existent
		try {
			channelSftp.lstat(transfer_path);
		} catch (SftpException e) {
			Log.write("Erstelle Verzeichnis " + transfer_path);
			try {
				channelSftp.mkdir(save_dir);
			} catch (SftpException e1) {
				Log.write("Konnte " + transfer_path + " nicht erstellen");
				System.exit(0);
			}
		}
	}

	private static ArrayList<String> listFiles(String inhalt, String dateiEndung) {
		String[] splited = inhalt.replaceAll(",", "").replaceAll("]", "").split("\\s+");

		ArrayList<String> filenames = new ArrayList<String>();

		for (int i = 0; i < splited.length; i++) {

			if (splited[i].toLowerCase().contains(dateiEndung.toLowerCase())) {
				filenames.add(splited[i]);
			}
		}

		return filenames;

	}

	public static File getLocalFileName(String type, Schema.MarktImport importMarkt) {

		if (type.equals("Umlagerung")) {
			// DOWNLOAD UMLAGERUNGSDATEI
			return new File(SystemProps.getUmlagerungDir() + "/UML." + importMarkt.marktnummerStation);
		} else {
			// DOWNLOAD UMSATZDATEI
			return new File(SystemProps.getUmsatzDir() + "/UMS." + importMarkt.marktnummerOnline);
		}

	}

}
