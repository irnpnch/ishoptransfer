package Schema;

public class Prozess {

	public Prozess(Thread t, String type, boolean �bertragungsStatus) {
		super();
		this.t = t;
		this.type = type;
		this.locked = �bertragungsStatus;
		this.closeCounter = 0;
	}

	public Thread t;
	public String type;
	public boolean locked;
	public int closeCounter;
	// locked ist ein Sperrzeiger, der das schlie�en des Threads verhindert,
	// wenn sich noch eine Datei in der �bertragung befindet
	// TRUE = Es wird gerade eine Datei �bertragen. Dieser Thread kann nicht
	// gestoppt werden
	// FALSE = Es wird gerade keine Datei �bertragen. Dieser Thread kann
	// gestoppt werden

	@Override
	public String toString() {

		if (this.locked) {
			return "�bertragung l�uft:";
		} else {
			return "Warte auf Datei:";
		}

	}

}
