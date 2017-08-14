package Schema;

public class TransferServer {

	public TransferServer(String template, String domain, int port, String user, String password, String connectionType,
			boolean applyProxy) {
		super();
		this.template = template;
		this.domain = domain;
		this.port = port;
		this.user = user;
		this.password = password;
		this.connectionType = connectionType;
		this.applyProxy = applyProxy;

	}

	public String template; // ID
	public String domain;
	public int port;
	public String user;
	public String password;
	public String connectionType;
	public boolean applyProxy;

}
