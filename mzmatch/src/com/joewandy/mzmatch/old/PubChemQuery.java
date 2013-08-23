package com.joewandy.mzmatch.old;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import peakml.chemistry.Molecule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.joewandy.mzmatch.model.CouchDbResponse;
import com.joewandy.mzmatch.model.CouchDbRow;
import com.joewandy.mzmatch.model.PubChemSnapshot;

/**
 * Queries PUG for other compounds having same formulae as our query formula.
 * This will retrieve the isomers of the query formula in PubChem database.
 * 
 * @author joewandy
 *
 */
public class PubChemQuery {

	private static final String QUERY_HOST_LOCAL = "localhost";
	private static final int QUERY_PORT_LOCAL = 5984;
	private static final String QUERY_PATH_LOCAL = "/compounds/_design/identify/_view/mass";
	
	private static final String QUERY_HOST = "pubchem.ncbi.nlm.nih.gov";
	private static final String QUERY_PROLOG = "/rest/pug";
	private static final String QUERY_OUTPUT = "/JSON";
	
	private String formula;
	private String listKey;
	private String[] isomerCids;
	private PubChemMolecule[] isomerProperties;
	
	private Set<PubChemSnapshot> pubChemSnapshots;
	
	public String getFormula() {
		return formula;
	}

	public String getListKey() {
		return listKey;
	}

	public String[] getIsomerCids() {
		return isomerCids;
	}

	public PubChemMolecule[] getIsomerProperties() {
		return isomerProperties;
	}

	public Set<PubChemSnapshot> getPubChemSnapshots() {
		return pubChemSnapshots;
	}

	/**
	 * Searches PUG database for other compounds having the same formula specified
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public String findListKeyByFormula(String formula) throws URISyntaxException, ClientProtocolException, IOException, InterruptedException {
		
		/*
		 * Request: http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/formula/C7H19N3/JSON
		 * Response:
		 * { 
		 *    "Waiting": {
		 *       "ListKey": "2883150836684277104",
		 *       "Message": "Your request is running"
		 *    }
		 * }
		 */

		final String input = "/compound/formula/" + formula;
		final String operation = "";

		URI uri = constructUri(input, operation);
		HttpGet httpget = new HttpGet(uri);
		System.out.println("GET: " + httpget.getURI());
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse = httpclient.execute(httpget);

		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {

			InputStream is = httpResponse.getEntity().getContent();
			
			ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		    mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		    
		    ObjectReader reader = mapper.reader(PugFormulaToListKey.class).withRootName("Waiting");
			PugFormulaToListKey retrieved = reader.readValue(is);
			this.formula = formula;
			this.listKey = retrieved.getListKey();

			is.close();
			
		}
		
		return this.listKey;
		
	}

	/**
	 * Retrieves compound ids based on listkey of current query
	 * @param listKey
	 * @return
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String[] findCidsByListKey(String listKey) throws URISyntaxException, ClientProtocolException, IOException {

		/*
		 * Request: http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/listkey/3699069569189429643/cids/JSON
		 * Response:
			{
			  "IdentifierList": {
			    "CID": [
			      1102,
			      7777
			    ]
			  }
			}
		 *
		 *
		 */

		final String input = "/compound/listkey/" + listKey;
		final String operation = "/cids";
		URI uri = constructUri(input, operation);
		HttpGet httpget = new HttpGet(uri);
		System.out.println("GET: " + httpget.getURI());
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse = httpclient.execute(httpget);
		
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {

			InputStream is = httpResponse.getEntity().getContent();			
			
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		    mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		    
		    ObjectReader reader = mapper.reader(PugListKeyToCid.class).withRootName("IdentifierList");
			PugListKeyToCid retrieved = reader.readValue(is);		    
			
			this.listKey = listKey;
			this.isomerCids = retrieved.getCid();
			
			is.close();
			
		}
		
		return this.isomerCids;
	
	}
	
	public PubChemMolecule[] findMoleculesByCids(String[] cids) throws URISyntaxException, ClientProtocolException, IOException {

		/*
		 * Request: 
		 * 	http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid
		 * 	/1102,7777
		 * 	/property/IUPACName,MolecularFormula,MolecularWeight,InChIKey/JSON
		 * Response:
			{
			  "PropertyTable": {
			    "Properties": [
			      {
			        "CID": 1102,
			        "MolecularFormula": "C7H19N3",
			        "MolecularWeight": 145.245859999999993,
			        "InChIKey": "ATHGHQPFGPMSJY-UHFFFAOYSA-N",
			        "IUPACName": "N'-(3-aminopropyl)butane-1,4-diamine"
			      },
			      {
			        "CID": 7777,
			        "MolecularFormula": "C7H19N3",
			        "MolecularWeight": 145.245859999999993,
			        "InChIKey": "KMBPCQSCMCEPMU-UHFFFAOYSA-N",
			        "IUPACName": "N'-(3-aminopropyl)-N'-methylpropane-1,3-diamine"
			      }
			    ]
			  }
			}
         *
		 *
		 */		

		StringBuilder builder = new StringBuilder();
		builder.append(PubChemQuery.QUERY_PROLOG);
		final String input = "/compound/cid/";
		builder.append(input);
		int count = 0;
		for(String s : cids) {
		    builder.append(s).append(",");
		    count++;
		    if (count > 10) {
		    	break;
		    }
		}
		final String operation = "/property/IUPACName,MolecularFormula,MolecularWeight,MonoisotopicMass,Charge,InChIKey";
		builder.append(operation);
		builder.append(PubChemQuery.QUERY_OUTPUT);
		URI uri = constructUri(builder);

		HttpGet httpget = new HttpGet(uri);
		System.out.println("GET: " + httpget.getURI());
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse = httpclient.execute(httpget);
		
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {

			InputStream is = httpResponse.getEntity().getContent();			
			
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		    mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		    
		    ObjectReader reader = mapper.reader(PugCidsToProperties.class).withRootName("PropertyTable");
			PugCidsToProperties retrieved = reader.readValue(is);		    
			
			this.isomerCids = cids;
			this.isomerProperties = retrieved.getProperties();
			
			is.close();
			
		}
		
		return this.isomerProperties;
		
	}
	
	public PubChemMolecule[] findIsomersByFormula(String formula) throws ClientProtocolException, 
		URISyntaxException, IOException, InterruptedException {
		
		System.out.println("=============================================");		
		System.out.println("Querying PubChem for isomers of " + formula);
		System.out.println("=============================================");		
		System.out.println();

		String listKey = findListKeyByFormula(formula);

		// TODO: hack -- delay for 5 seconds before sending the next request
        System.out.println("Waiting for search to finish ...");
	    Thread.sleep(5000);
		System.out.println("Response - listKey=" + listKey);
	    System.out.println();

		String[] cids = findCidsByListKey(listKey);

		// TODO: hack -- delay for 5 seconds before sending the next request
        System.out.println("Waiting for search to finish ...");
	    Thread.sleep(5000);
		System.out.println("Response - Isomers CIDs=");
		int count = 1;
		for (String cid : cids) {
			System.out.println("\t" + count + ". " + cid);
			count++;
		}
	    System.out.println();

	    PubChemMolecule[] pcMolecules = findMoleculesByCids(cids);
	    System.out.println("Response - Isomers Properties=");
		count = 1;
	    for (PubChemMolecule property : pcMolecules) {
			System.out.println("\t" + count + ". " + property);
			count++;
		}	    
        System.out.println("Finished !");

        return pcMolecules;
        
	}
	
	@SuppressWarnings("unused")
	public Set<PubChemSnapshot> findCompoundsByMass(String formula, double mass, double delta) throws URISyntaxException, ClientProtocolException, IOException {

		System.out.println("Querying local PubChem snapshot");
		System.out.println("\tmonoisotopic mass=" + String.format("%.5f", mass));
		System.out.println("\tdelta=" + String.format("%.5f", delta));
		
		/*
		 * Request: 
		 * 	http://localhost:5984/compounds/_design/identify/_view/by_mass?startkey=%22100.1%22&endkey=%22100.2%22&include_docs=true
		 * Response:
			{"total_rows":213085,"offset":5,"rows":[
			{"id":"58861991","key":"100.100048","value":null,"doc":{"_id":"58861991","_rev":"1-34d9aea7e685784322f62771cd7eb7f3","molecular_weight":"100.16218","iupac_inchi_key":"VGXJTJLMULAKRS-UHFFFAOYSA-N","charge":"0","monoisotopic_weight":"100.100048","molecular_formula":"C5H12N2","inchi":null,"traditional_name":"ethyl-[2-(methylamino)vinyl]amine","iupac_inchi":"InChI=1S/C5H12N2/c1-3-7-5-4-6-2/h4-7H,3H2,1-2H3"}}
			]}
		 *
		 *
		 */		

		double fromMass = mass - delta;
		double toMass = mass + delta;
		URI uri = constructMassQueryUri(PubChemQuery.QUERY_PATH_LOCAL, fromMass, toMass);

		HttpGet httpget = new HttpGet(uri);
		System.out.println("GET: " + httpget.getURI());
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse = httpclient.execute(httpget);
		
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		System.out.println("statusCode=" + statusCode);
		if (statusCode == HttpStatus.SC_OK) {

			InputStream is = httpResponse.getEntity().getContent();			
			
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		    
		    ObjectReader reader = mapper.reader(CouchDbResponse.class);
		    CouchDbResponse couchDbResponse = reader.readValue(is);		    
		    CouchDbRow[] rows = couchDbResponse.getRows();

		    // filter out the isomers
		    System.out.println("Found " + rows.length + " matching compound(s) in mass range");
		    Set<PubChemSnapshot> pubchems = new HashSet<PubChemSnapshot>();
		    for (int i = 0; i < rows.length; i++) {

	    		PubChemSnapshot toAdd = rows[i].getDoc();
		    	String compoundFormula = toAdd.getMolecularFormula();
		    	String[] tokens = compoundFormula.split("(\\+|\\-)");
		    	String toCheck = tokens[0];
		    	if (compoundFormula == null) {
		    		continue;
		    	}

		    	boolean sameString = formula.equals(toCheck);
		    	boolean seenBefore = seenBefore(pubchems, toCheck);
		    	if (!sameString && !seenBefore) {
		    		pubchems.add(toAdd);
		    	}
		    	
		    }
		    
		    System.out.println("Found " + pubchems.size() + " other unique formulae");
		    Set<Molecule> compounds = new HashSet<Molecule>();
		    for (PubChemSnapshot pc : pubchems) {
		    	System.out.println("\t" + pc);
		    	Molecule mol = new Molecule(pc.getId(), pc.getTraditionalName(), pc.getMolecularFormula());
		    	compounds.add(mol);
		    }

		    this.pubChemSnapshots = pubchems;
			is.close();
			
		}
		
        return this.pubChemSnapshots;
		
	}
	
	/**
	 * Quick check to see if newFormula has been stored inside compounds before
	 * @param compounds
	 * @param newFormula
	 * @return
	 */
	private boolean seenBefore(Set<PubChemSnapshot> compounds,
			String newFormula) {
		for (PubChemSnapshot compound : compounds) {
			if (compound.getMolecularFormula() == null) {
				continue;
			}
	    	String[] tokens = compound.getMolecularFormula().split("(\\+|\\-)");
	    	String toCheck = tokens[0];
			if (toCheck.equals(newFormula)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Construct URI request to PUG REST
	 * @param input
	 * @param operation
	 * @return
	 * @throws URISyntaxException
	 */
	private URI constructUri(final String input, final String operation) throws URISyntaxException {
		String path = PubChemQuery.QUERY_PROLOG + input + operation + PubChemQuery.QUERY_OUTPUT;
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(PubChemQuery.QUERY_HOST).setPath(path);
		URI uri = builder.build();
		return uri;
	}

	/**
	 * Construct URI request to PUG REST
	 * @param sb
	 * @return
	 * @throws URISyntaxException
	 */
	private URI constructUri(StringBuilder sb) throws URISyntaxException {
		String path = sb.toString();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(PubChemQuery.QUERY_HOST).setPath(path);
		URI uri = builder.build();
		return uri;
	}

	/**
	 * Construct URI request to local PubChem snapshot (CouchDB)
	 * @param path
	 * @param fromMass
	 * @param toMass
	 * @return
	 * @throws URISyntaxException
	 */
	private URI constructMassQueryUri(String path, double fromMass, double toMass) throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(PubChemQuery.QUERY_HOST_LOCAL).setPath(path).setPort(PubChemQuery.QUERY_PORT_LOCAL);
		builder.setParameter("startkey", String.format("\"%.5f\"", fromMass));
		builder.setParameter("endkey", String.format("\"%.5f\"", toMass));
		builder.setParameter("include_docs", "true");
		URI uri = builder.build();
		return uri;
	}
	
}
