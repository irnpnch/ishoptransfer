package Schema;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;

import MainPackage.Log;
import MainPackage.IshopTransferMain;
import MainPackage.SystemProps;

public class Response {

	public Response(String ILN, String successLevel, String operation, String logmsg, String marktname) {
		super();
		this.ILN = ILN;
		this.successLevel = successLevel;
		this.operation = operation;
		this.logmsg = logmsg;
		this.marktname = marktname;
		this.USER_AGENT = "JRE: " + SystemProps.getJRE() + " | JAR: " + SystemProps.getJAR();
		this.timestamp = MainPackage.Log.getTimestamp();
	}

	public String ILN;
	public String USER_AGENT = "JRE: " + SystemProps.getJRE() + " | JAR: " + SystemProps.getJAR();
	public String successLevel;
	public String operation;
	public String logmsg;
	public String marktname;
	public String timestamp;

	public void post() {

		if (IshopTransferMain.localmode) {
			// Lokaler Modus sendet keine Erfolgsrückmeldung an den Server
			return;
		}
		String url = SystemProps.getRemoteServer();
		URL obj;

		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", this.USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			// Post-Parameter = dieses.Objekt
			Gson g = new Gson();
			String urlParameters = g.toJson(this);

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			Log.write("\nSending 'POST' request to URL : " + url + "\nPARA-JSON:" + urlParameters);
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			Log.write("HTTP-responseCode: " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());

		} catch (MalformedURLException e) {
			Log.write("Monitoring Fehler (MalformedURLException): " + e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.write("Monitoring Fehler (IOException): " + e);
			e.printStackTrace();
		}
	}

}
