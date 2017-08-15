package MainPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import Schema.MarktImport;
import Schema.Bewirtschaftung;

public class DB_Queries {

	public synchronized static void setbew(String hostIp, MarktImport oliviaMarkt) {

		Connection conn = null;
		
		// Bitte füllen
		int port = 0; //zu_fuellen
		String user = "zu_fuellen"; 
		String password = "zu_fuellen";
		String dbTable = "zu_fuellen";
		
		try {
			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + hostIp +":" +port+"/"+dbTable, user, password);
		} catch (SQLException e) {
			Log.write("DB_Queries.Setbew(" + hostIp + ":"+port+")-Connection-Exception: " + e);
		}

		try {
			Statement stmt = conn.createStatement();

			// Baue Queries zusammen
			String query_entwirt = queryFactory("entwirtschaften", oliviaMarkt);
			String query_bewirt = queryFactory("bewirtschaften", oliviaMarkt);
			String query_pfand = queryFactory("pfand", oliviaMarkt);
			// 'Pseudo Bulk-Collect'
			ArrayList<Bewirtschaftung> changeBewirt = new ArrayList<Bewirtschaftung>();

			// Ausführen der Queries
			changeBewirt = getChanges(stmt, "0", query_entwirt, changeBewirt);
			changeBewirt = getChanges(stmt, "1", query_bewirt, changeBewirt);
			changeBewirt = getChanges(stmt, "1", query_pfand, changeBewirt);

			if (changeBewirt != null) {
				Log.write("DatabaseController.setbewirt(" + hostIp + "," + oliviaMarkt.marktName + "): Änderung von "
						+ changeBewirt.size() + " Bewirtschaftungs-Kennzeichen");
				Log.writeBewirtLog(changeBewirt); //Siehe @Override toString Methode 

				for (Bewirtschaftung Artikel : changeBewirt) {
					
					String update = "update filialartikel_table set knz_bewirtschaftet = " + Artikel.neueBewirtschaftung
							+ " where fla_fla_id = " + Artikel.fla_fla_id;
					
					stmt.executeQuery(update);		
				}
			}			
			conn.close();
		} catch (SQLException e) {
			Log.write("Statement-SQL-Exception: " + e);
		}

	}

	private synchronized static ArrayList<Bewirtschaftung> getChanges(Statement stmt, String neuesKennzeichen,
			String query, ArrayList<Bewirtschaftung> changeBewirt) {

		try {
			ResultSet rs;
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				changeBewirt.add(new Bewirtschaftung(neuesKennzeichen, rs.getString("BEZEICHNUNG"), rs.getString("FLA"),
						rs.getString("EBUSNR"), rs.getString("MARKT")));
			}
			rs.close();
			return changeBewirt;

		} catch (SQLException e) {
			Log.write("DB_Queries.getChanges-Result-Exception: " + e);
			return null;
		}
	}

	private static String queryFactory(String type, MarktImport oliviaMarkt) {

		if (type.equals("pfand")) {

			return "SELECT \n" + "\t	ArtFil.Bezeichnung AS BEZEICHNUNG,\n"
					+ "\t 	TO_CHAR(ArtFil.artikel_id) AS EBUSNR, \n" + "\t 	TO_CHAR(ArtFil.fla_fla_id) AS FLA, \n"
					+ "\t 	mktstm.markt_id AS MARKT \n" + "FROM \n" + "\t 	filialartikel_table ArtFil\n"
					+ "\t	INNER JOIN warengruppenstamm wgrstm ON ArtFil.wgrstm_wgrstm_id = wgrstm.wgrstm_id \n"
					+ "\t	INNER JOIN marktstamm_table mktstm ON ArtFil.mktstm_mktstm_id = mktstm.mktstm_id \n"
					+ "WHERE \n" + "\t	mktstm.markt_id = " + oliviaMarkt.marktnummerOnline + "\n"
					+ "\t 	AND Artfil.KNZ_BEWIRTSCHAFTET = 0\n" + "\t	AND (wgrstm.Warengruppen_ID = 87 \n"
					+ "\t	OR wgrstm.Warengruppen_ID = 88)";
		}

		String header = "SELECT \n" + "\t	ArtFil.Bezeichnung AS BEZEICHNUNG,\n"
				+ "\t 	TO_CHAR(ArtFil.artikel_id)	AS EBUSNR,\n" + "\t 	TO_CHAR(FilbezDest.fla_id) 	AS FLA,\n"
				+ "\t 	MktStDest.markt_id AS MARKT\n" + "FROM \n" + "\t 	marktstamm_table     		MktSt, \n"
				+ "\t 	marktstamm_table     		MktStDest, \n" + "\t 	filialartikel_table  		ArtFil, \n"
				+ "\t 	filialartikel_table  		ArtFilDest, \n" + "\t 	filial_lief_art_bez 		FilbezDest, \n"
				+ "\t 	artikelstamm         		ArtSt, \n" + "\t   warengruppenstamm 			wgrstm \n"
				+ "WHERE \n" + "\t 	ArtFil.mktstm_mktstm_id = MktSt.mktstm_id\n"
				+ "\t   AND ArtFilDest.wgrstm_wgrstm_id = wgrstm.wgrstm_id"
				+ "\t 	AND ArtFil.artst_artst_id = ArtSt.artst_id\n"
				+ "\t 	AND ArtFilDest.mktstm_mktstm_id = MktStDest.mktstm_id\n"
				+ "\t 	AND ArtFilDest.artst_artst_id = ArtFil.artst_artst_id\n"
				+ "\t 	AND FilbezDest.fla_id = ArtFilDest.fla_fla_id\n" + "\t 	AND ArtSt.datum_auslistung IS NULL\n";

		String markt = "\t\t AND MktSt.markt_id = " + oliviaMarkt.marktnummerStation + "\n"
				+ "\t	AND MktStDest.markt_id = " + oliviaMarkt.marktnummerOnline + "\n";
		String logik = "";

		if (type.equals("bewirtschaften")) {
			logik = "\t 	AND ArtFilDest.knz_bewirtschaftet = 0 	\n"
					+ "\t 	AND (FilbezDest.knz_etikettentyp = 2 	\n"
					+ "\t 	OR 	ArtFil.knz_bewirtschaftet > 0 		\n" + "\t 	AND FilbezDest.knz_etikettentyp = 1)";
		}

		if (type.equals("entwirtschaften")) { // online bewirtschaftung
												// entziehen wenn
			logik = "\t 	AND ArtFilDest.knz_bewirtschaftet > 0 \n" // 1.
																		// Online
																		// Bewirtschaftung
																		// vorliegt
					+ "\t 	AND (FilbezDest.knz_etikettentyp = 0  \n" // 2. UND
																		// (Etikettentyp
																		// Online
																		// = 0
					+ "\t 	OR (FilbezDest.knz_etikettentyp = 1 \n" // 3. ODER
																	// (Etikettentyp
																	// = 1
					+ "\t	AND ArtFil.knz_bewirtschaftet = 0)) \n" // UND
																	// Bewirtschaftung
																	// = 0))
					+ "\t	AND wgrstm.Warengruppen_ID NOT BETWEEN 87 AND 88"; // 4.
																				// UND
																				// Kein
																				// Pfand
																				// /
																				// Leergut
		}

		return header + markt + logik;
	}
}
