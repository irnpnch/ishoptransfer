package Schema;

public class Prozess {

	public Prozess(Thread t, String type, boolean übertragungsStatus) {
		super();
		this.t = t;
		this.type = type;
		this.locked = übertragungsStatus;
		this.closeCounter = 0;
	}

	public Thread t;
	public String type;
	public boolean locked;
	public int closeCounter;
	// locked ist ein Sperrzeiger, der das schließen des Threads verhindert,
	// wenn sich noch eine Datei in der Übertragung befindet
	// TRUE = Es wird gerade eine Datei übertragen. Dieser Thread kann nicht
	// gestoppt werden
	// FALSE = Es wird gerade keine Datei übertragen. Dieser Thread kann
	// gestoppt werden

	@Override
	public String toString() {

		if (this.locked) {
			return "Übertragung läuft:";
		} else {
			return "Warte auf Datei:";
		}

	}

}
