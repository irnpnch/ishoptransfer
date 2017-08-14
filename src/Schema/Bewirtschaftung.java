package Schema;

public class Bewirtschaftung {

	public Bewirtschaftung(String neueBewirtschaftung, String bezeichnung, String fla_fla_id, String artikel_id, String markt) {
		super();
		this.neueBewirtschaftung = neueBewirtschaftung;
		this.bezeichnung = bezeichnung;
		this.fla_fla_id = fla_fla_id;
		this.artikel_id = artikel_id;
		this.markt = markt;
	}

	public String neueBewirtschaftung;
	public String bezeichnung;
	public String fla_fla_id;
	public String artikel_id;
	public String markt;

	@Override
	public String toString() {
		return this.neueBewirtschaftung + ";" + this.artikel_id + ";" + this.bezeichnung +";" +this.markt; 
	}

}
