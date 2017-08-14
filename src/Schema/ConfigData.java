package Schema;

import com.google.gson.Gson;

import MainPackage.SystemProps;

public class ConfigData {

	public ConfigData(String proxyHost, int proxyPort,
			String oracleIP, Schema.TransferServer[] TransferServer, Schema.MarktImport[] MarktImport) {

		this.oracleIP = oracleIP; // DB-Location (TODO: Aus TNSNAMES.ORA extrahieren)
		this.proxyHost = proxyHost; 
		this.proxyPort = proxyPort;
		this.TransferServer = TransferServer;
		this.MarktImport = MarktImport;
	}

	public String proxyHost;
	public int proxyPort;
	public String oracleIP;
	public Schema.TransferServer[] TransferServer;
	public Schema.MarktImport[] MarktImport;

	public Schema.TransferServer getServer(String template) {

		for (int i = 0; i < this.TransferServer.length; i++) {
			if (this.TransferServer[i].template.equals(template)) {
				return this.TransferServer[i];
			}
		}

		MainPackage.Log.write("configData.getServer(String template) -> Kein Server zum template " + template + " gefunden");
		return null;
	}

	public static String getConfigTemplate(){
		
		TransferServer[] tServerArray = {
				new TransferServer("Gertrud","gertrud-transit.liqham.com",21,"edeka_markt","7vXxvReO4qk4","FTP",true),
				new TransferServer("Olivia","10.1.39.20",22,"jobno","SStFgYRgWYnC","SFTP",false)};
		MarktImport[] mImportArray= {
				new MarktImport("%0000000000%","%00000%","%ShopUrl (zB. schubertwaren)%","%Stationaere Marktnummer (dreistellig mit fuehrenden Nullen)%","%Online Marktnummer (dreistellig mit fuehrenden Nullen)%",SystemProps.getEbusStammdatenDir()+"/Ishop_%Online Marktnummer%.txt" ,"jsTestVerzeichnis", "no")
				};
		ConfigData empty = new ConfigData("edeka-nord-proxy.app.mediaways.net",8080,"%Datenbank-IP%", tServerArray, mImportArray);
		Gson g = new Gson();
		String json = g.toJson(empty);
		json = json.replace(",", ",\r\n");
		json = json.replace("{","{\r\n");
		json = json.replace("}","}\r\n");
		json = json.replace("}","\r\n}");
		
		json = json.replace("[","[\r\n");
		json = json.replace("]","]\r\n");
		
		return json;

	}
}
