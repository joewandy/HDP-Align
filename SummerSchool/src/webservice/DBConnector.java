package webservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBConnector {
    
    Connection con;

    public DBConnector() {
        try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        try{
        	con = DriverManager.getConnection("jdbc:postgresql://vm01.cs.st-andrews.ac.uk:5432/summer_school",
					"summer_school", "GluonFluxTripBucket");
        }catch(SQLException e){
            e.printStackTrace();
        }

    }
    
    public List<Commodity> fetchCommodities(String query){
        System.out.println(query);
        List<Commodity> commodities =  new ArrayList();
        Statement stmt = null;           
        if (con != null) {                       
            try {
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);                            
                while (rs.next()) {
                    String name = rs.getString("COMMODITY_2");
                    //String docId = rs.getString("document_id");
                    int freq = rs.getInt("FREQ");
                    Commodity comm = new Commodity();
                    comm.setName(name);
                    //comm.setDocId(docId);
                    comm.setFreq(freq);
                    commodities.add(comm);
                }

            } catch (SQLException e ) {
                e.printStackTrace();
            } finally {
                if (stmt != null) { 
                    try{
                        stmt.close();
                    } catch(SQLException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return commodities;
        
    }
    
     public List<Commodity> fetchRelCommodities(String... comms){
         List<Commodity> commodities =  new ArrayList();
         for(String com:comms){
              String query = "SELECT '"+com+"' AS COMMODITY_1, COMMODITY_TEXT AS COMMODITY_2, COUNT(*) AS FREQ "+
            "FROM TRADES WHERE DOCUMENT_ID IN ( "+
                "SELECT DOCUMENT_ID FROM ( "+
                    "SELECT COMMODITY_TEXT, DOCUMENT_ID, COUNT(*) AS FREQ "+
                    "FROM TRADES WHERE "+
                        "LOWER(COMMODITY_TEXT) = '"+com+"' AND "+
                        "PUB_YEAR >= 1800 AND "+
                        "PUB_YEAR < 1810 "+
                    "GROUP BY COMMODITY_TEXT, DOCUMENT_ID "+
                    "ORDER BY COMMODITY_TEXT, FREQ DESC "+
                    "LIMIT 10 "+
                ") AS DOC_ID "+
            ") "+
            "GROUP BY COMMODITY_TEXT "+
            "ORDER BY FREQ DESC LIMIT 30";
        System.out.println(query);
        
            Statement stmt = null;           
            if (con != null) {                       
                try {
                    stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);                            
                    while (rs.next()) {
                        String name = rs.getString("COMMODITY_2");
                        //String docId = rs.getString("document_id");
                        int freq = rs.getInt("FREQ");
                        Commodity comm = new Commodity();
                        comm.setName(name);
                        comm.setRel(com);
                        //comm.setDocId(docId);
                        comm.setFreq(freq);
                        commodities.add(comm);
                    }

                } catch (SQLException e ) {
                    e.printStackTrace();
                } finally {
                    if (stmt != null) { 
                        try{
                            stmt.close();
                        } catch(SQLException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
         }
        return commodities;
        
    }

     public List<Commodity> fetchSelectedCommodities(String... comms){

    	 List<Commodity> commodities =  new ArrayList();
         String param = "";
         for(int i=0;i<comms.length-1;i++){
             param+="'"+comms[i]+"',";
         }
         param+="'"+comms[comms.length-1]+"'";

         
         for(String com:comms){
              String query = "SELECT '"+com+"' AS COMMODITY_1, COMMODITY_TEXT AS COMMODITY_2, COUNT(*) AS FREQ "+
            "FROM TRADES WHERE DOCUMENT_ID IN ( "+
                "SELECT DOCUMENT_ID FROM ( "+
                    "SELECT COMMODITY_TEXT, DOCUMENT_ID, COUNT(*) AS FREQ "+
                    "FROM TRADES WHERE "+
                        "LOWER(COMMODITY_TEXT) = '"+com+"' AND "+
                        "PUB_YEAR >= 1800 AND "+
                        "PUB_YEAR < 1810 "+
                    "GROUP BY COMMODITY_TEXT, DOCUMENT_ID "+
                    "ORDER BY COMMODITY_TEXT, FREQ DESC "+
                    "LIMIT 10 "+
                ") AS DOC_ID "+
            ") "+
            "AND COMMODITY_TEXT IN (" + param + ")" +
            "GROUP BY COMMODITY_TEXT "+
            "ORDER BY FREQ DESC LIMIT 30";
        System.out.println(query);
        
            Statement stmt = null;           
            if (con != null) {                       
                try {
                    stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);                            
                    while (rs.next()) {
                        String name = rs.getString("COMMODITY_2");
                        //String docId = rs.getString("document_id");
                        int freq = rs.getInt("FREQ");
                        Commodity comm = new Commodity();
                        comm.setName(name);
                        comm.setRel(com);
                        //comm.setDocId(docId);
                        comm.setFreq(freq);
                        commodities.add(comm);
                    }

                } catch (SQLException e ) {
                    e.printStackTrace();
                } finally {
                    if (stmt != null) { 
                        try{
                            stmt.close();
                        } catch(SQLException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
         }
        return commodities;
        
    }
     
    public List<Commodity> fetchCommoditiesLoc(String... comms){        
        String param = "";
        for(int i=0;i<comms.length-1;i++){
            param+="'"+comms[i]+"',";
        }
        param+="'"+comms[comms.length-1]+"'";
        
        String query = "SELECT COMMODITY_TEXT, LOCATION_ID, FREQ, TEXT AS LOCATION_TEXT, LAT, LON FROM ("+
            "SELECT COMMODITY_TEXT, LOCATION_ID, COUNT(*) AS FREQ FROM TRADES "+
            "WHERE LOWER(COMMODITY_TEXT) IN ("+param+") AND "+
                "PUB_YEAR >= 1800 AND "+
                "PUB_YEAR < 1830 "+
            "GROUP BY COMMODITY_TEXT, LOCATION_ID "+
            ") RESULT  "+
            "INNER JOIN API_LOCATION "+
            "ON RESULT.LOCATION_ID = API_LOCATION.ID "+
            "ORDER BY FREQ DESC"; 
        
        System.out.println(query);
        List<Commodity> commodities =  new ArrayList();
        Statement stmt = null;           
        if (con != null) {                       
            try {
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);                            
                while (rs.next()) {
                    String name = rs.getString("COMMODITY_TEXT");
                    int locId = rs.getInt("LOCATION_ID");
                    int freq = rs.getInt("FREQ");
                    String locText = rs.getString("LOCATION_TEXT");
                    double lat = rs.getDouble("LAT");
                    double lon = rs.getDouble("LON");
                    
                    Commodity comm = new Commodity();
                    comm.setName(name);
                    //comm.setDocId(docId);
                    comm.setFreq(freq);
                    commodities.add(comm);
                    comm.setLocId(locId);
                    comm.setLocText(locText);
                    comm.setLat(lat);
                    comm.setLon(lon);
                    
                }

            } catch (SQLException e ) {
                e.printStackTrace();
            } finally {
                if (stmt != null) { 
                    try{
                        stmt.close();
                    } catch(SQLException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return commodities;
        
    }

    public List<Commodity> fetchCommoditiesYear(String... comms){        

        List<Commodity> commodities =  new ArrayList<Commodity>();
        for(String com:comms){

            String query = "SELECT COMMODITY_TEXT, PUB_YEAR, COUNT(*) AS FREQ " + 
            		"FROM TRADES " + 
            		"WHERE COMMODITY_TEXT = '" + com + "' " +
            		"GROUP BY COMMODITY_TEXT, PUB_YEAR ORDER BY PUB_YEAR;";
                        
            System.out.println(query);
            Statement stmt = null;           
            if (con != null) {                       
                try {
                    stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);                            
                    while (rs.next()) {
                        String name = rs.getString("COMMODITY_TEXT");
                        int pubYear = rs.getInt("PUB_YEAR");
                        int freq = rs.getInt("FREQ");
                        
                        Commodity comm = new Commodity();
                        comm.setName(name);
                        comm.setFreq(freq);
                        comm.setPubYear(pubYear);
                        commodities.add(comm);
                        
                    }

                } catch (SQLException e ) {
                    e.printStackTrace();
                } finally {
                    if (stmt != null) { 
                        try{
                            stmt.close();
                        } catch(SQLException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        	
        }

        return commodities;

    }
	
}