package Schema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Singleton-Entwurfsmuster

public class DB_Connection {

	private static DB_Connection instance = null;
	private Connection conn;
	private String hostIp;

	protected DB_Connection(String hostIp) {
		if (this.hostIp == null) {
			this.hostIp = hostIp;
		}
	}

	public Connection getInstance() {
		if (instance == null) {
			try {
				this.conn = DriverManager.getConnection("jdbc:oracle:thin:@" + hostIp + ":1521/ebus.world", "ebus",
						"ebus");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return this.conn;
	}

}
