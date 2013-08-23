import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JDBCExample {

	private List<Result> results;
	private Map<String, Integer> commIds;
	private int yearFrom;
	private int yearTo;
	
	public JDBCExample(int yearFrom, int yearTo) {

		this.results = new ArrayList<Result>();
		this.commIds = new HashMap<String, Integer>();
		this.yearFrom = yearFrom;
		this.yearTo = yearTo;

	}
	
	public static void main(String[] args) {
		JDBCExample ex = new JDBCExample(1890, 1900);
		ex.process();
	}
	
	public List<Result> process() {
		
		commIds = getUniqueCommodities();
		int counter = 1;
	    try {
			FileWriter writer = new FileWriter("/home/joewandy/summer_school/index_" + yearFrom + "_" + yearTo + ".csv");
			writer.append("termID, term\n");
			for (Entry<String, Integer> entry : commIds.entrySet()) {
				String commodity = entry.getKey();
				int id = entry.getValue();
				System.out.println("#" + counter + "/" + commIds.size() + " Processing " + commodity);
				fetchRelatedTerms(commodity);
				counter++;
				writer.append(id + ", " + commodity + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    try {
			FileWriter writer = new FileWriter("/home/joewandy/summer_school/output_" + yearFrom + "_" + yearTo + ".csv");
			writer.append(Result.getHeader() + "\n");
			for (Result res : results) {
				writer.append(res + "\n");
			}
			writer.flush();
			writer.close();
			System.out.println("-- DONE --");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return results;
		
	}
	
	private void fetchRelatedTerms(String commodity) {
		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Connection con = null;
		Statement stmt = null;
		try {

			con = DriverManager
					.getConnection(
							"jdbc:postgresql://vm01.cs.st-andrews.ac.uk:5432/summer_school",
							"summer_school", "GluonFluxTripBucket");

			if (con != null) {

				String query = 
						"SELECT '" + commodity + "' AS COMMODITY_1, COMMODITY_TEXT AS COMMODITY_2, COUNT(*) AS FREQ " + 
						"FROM TRADES WHERE DOCUMENT_ID IN ( " + 
							"SELECT DOCUMENT_ID FROM ( " + 
								"SELECT COMMODITY_TEXT, DOCUMENT_ID, COUNT(*) AS FREQ " + 
								"FROM TRADES " + 
								"WHERE " +
									"LOWER(COMMODITY_TEXT) = '" + commodity.toLowerCase() + "' AND " + 
									"PUB_YEAR >= " + this.yearFrom + " AND " + 
									"PUB_YEAR < " + this.yearTo + " " +
									"GROUP BY COMMODITY_TEXT, DOCUMENT_ID " + 
									"ORDER BY COMMODITY_TEXT, FREQ DESC " + 
									"LIMIT 10 " + 
								") AS DOC_ID " + 
							") " + 
						"GROUP BY COMMODITY_TEXT " + 
						"ORDER BY FREQ DESC";
				try {

					stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(query);
					while (rs.next()) {
						String commodity1 = rs.getString("COMMODITY_1");
						String commodity2 = rs.getString("COMMODITY_2");
						double value = rs.getDouble("FREQ");
						int commodityID1 = commIds.get(commodity1);
						int commodityID2 = commIds.get(commodity2);
						Result result = new Result(commodity1, commodity2, commodityID1, commodityID2, value);
						results.add(result);
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private Map<String, Integer> getUniqueCommodities() {

		System.out.println("Retrieving all unique commodity texts");
		
		Map<String, Integer> ids = new HashMap<String, Integer>();		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Connection con = null;
		Statement stmt = null;
		try {

			con = DriverManager
					.getConnection(
							"jdbc:postgresql://vm01.cs.st-andrews.ac.uk:5432/summer_school",
							"summer_school", "GluonFluxTripBucket");

			if (con != null) {

				String query = "SELECT DISTINCT COMMODITY_TEXT FROM TRADES " + 
						"WHERE " +
						"PUB_YEAR >= " + this.yearFrom + " AND " + 
						"PUB_YEAR < " + this.yearTo + " " +
						" ORDER BY COMMODITY_TEXT";
				try {

					stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(query);
					int id = 0;
					while (rs.next()) {
						String commodityText = rs.getString("COMMODITY_TEXT");
						id++;
						ids.put(commodityText, id);
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ids;

	}

}