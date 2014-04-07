package com.joewandy.mzmatch.query;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.mzmine.modules.peaklistmethods.identification.dbsearch.DBGateway;
import net.sf.mzmine.modules.peaklistmethods.identification.dbsearch.databases.PubChemGateway;
import net.sf.mzmine.parameters.parametertypes.MZTolerance;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import peakml.chemistry.Molecule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.joewandy.mzmatch.model.PubChemMolecule;
import com.joewandy.mzmatch.model.PugCidsToProperties;

/**
 * Queries PUG for other compounds having same formulae as our query formula.
 * This will retrieve the isomers of the query formula in remote PubChem database.
 * 
 * @author joewandy
 * 
 */
public class PubChemQuery extends BaseQuery implements CompoundQuery {

	// number of results per query
	private static final int DEFAULT_NUM_RESULTS = 10;

	// how many queries to retrieve per batch
	// TODO: this is just a hack. properly we should issue a batch query.
	private static final int DEFAULT_BATCH_SIZE = 10;
	
	private static final String QUERY_HOST = "pubchem.ncbi.nlm.nih.gov";
	private static final String QUERY_PROLOG = "/rest/pug";
	private static final String QUERY_OUTPUT = "/JSON";
	
	private final int batchSize;
	private final int numResults;

	public PubChemQuery() {
		this.numResults = DEFAULT_NUM_RESULTS;		
		this.batchSize = DEFAULT_BATCH_SIZE;
	}
	
	public PubChemQuery(Map<String, Molecule> molecules) {
		this(molecules, PubChemQuery.DEFAULT_NUM_RESULTS, PubChemQuery.DEFAULT_BATCH_SIZE);
	}

	public PubChemQuery(Map<String, Molecule> molecules, int numResults, int batchSize) {
		super.result = new HashSet<Molecule>();
		super.lookup = new HashMap<String, Integer>();
		for (Molecule mol : molecules.values()) {
			super.lookup.put(mol.getPlainFormula(), 1);
		}
		this.numResults = numResults;
		this.batchSize = batchSize;
	}
	
	public Set<Molecule> findCompoundsByMass(double mass, double ppm,
			double delta) throws Exception {

		System.out.print("Querying remote PubChem");
		System.out.print("\tmass=" + String.format("%.5f", mass));
		System.out.print("\tdelta=" + String.format("%.5f", delta));
		System.out.println("\tppm=" + String.format("%.1f", ppm));

		DBGateway gw = new PubChemGateway(); 
		MZTolerance tolerance = new MZTolerance(delta, ppm);
		
		// the last parameter is null because it's not used at all inside PubChemGateway
		String[] cids = gw.findCompounds(mass, tolerance, numResults, null);
		
		// hack: queries in batches, not all once. Workaround the length of GET request.
		int low = 0;
		int high = low + batchSize;
		List<PubChemMolecule> temp = new ArrayList<PubChemMolecule>();
		while (low < cids.length) {

			// take care of corner case
			int end = batchSize;
			if (high > cids.length) {
				end = cids.length - low;
			}

			// extract just parts of cids to retrieve
			String[] tempCids = new String[end];
			System.arraycopy(cids, low, tempCids, 0, end);
			PubChemMolecule[] retrieved = findMoleculesByCids(tempCids);
			temp.addAll(Arrays.asList(retrieved));	
			
			// keep track of where we are now
			low = high;
			high = low + batchSize;

		}

		// remove duplicates
		Set<PubChemMolecule> rows = new HashSet<PubChemMolecule>(temp);
		System.out.println("Found " + rows.size() + " other formulae");
		System.out.println("Inserting: ");
		
		// map pubchem result to peakml compounds
		Set<Molecule> retrieved = new HashSet<Molecule>();
		int counter = 0;
		for (PubChemMolecule pc : rows) {
			
			/* 
			 * keep things simple for now: 
			 * ignore molecules with weird formula, usually charged ones
			 */
			int charge = Integer.parseInt(pc.getCharge());
			if (charge != 0) {
				continue;
			}
			
			// also skip those already in the existing standard database
			if (lookup.get(pc.getMolecularFormula()) != null) {
				continue;
			}

			Molecule mol = null;
			try {
				// will throw exception if anything wrong with the formula
				mol = new Molecule(pc.getCid(), pc.getIupacName(), pc.getMolecularFormula());	
			} catch (RuntimeException e) {
				// ignore any exception handling for now
				// System.out.println(e.getMessage());
			}
			if (mol != null) {
				
				mol.setInChi(pc.getInChi());
				mol.setMass(Double.parseDouble(pc.getMonoisotopicMass()));
				System.out.println("\t" + pc.getMolecularFormula() + " " + pc.getMonoisotopicMass());					
				retrieved.add(mol);					
				
				// store the formula in lookup table, for checking in future queries
				lookup.put(pc.getMolecularFormula(), 1);

				counter++;
				
			}
								
		}
		
		System.out.println(counter + " inserted.");
		
		result.addAll(retrieved);
		return retrieved;

	}
	
	public PubChemMolecule findCompoundsByNameFormula(Molecule original, boolean useFormula) throws Exception {

		String name = original.getName();
		String formula = original.getPlainFormula();
		
		if (useFormula) {
			System.out.println("Querying PubChem by name=" + name + " formula=" + formula);			
		} else {
			System.out.println("\t--RETRY-- Querying PubChem by name=" + name + " only");
		}

		String[] cids = null;
		if (useFormula) {
			cids = findCompounds(name, formula);		
		} else {
			cids = findCompounds(name, null);		
		}
		PubChemMolecule[] retrieved = findMoleculesByCids(cids);
		if (retrieved == null) {
			return null;
		}
		PubChemMolecule singleRetrieved = retrieved[0];			
		return singleRetrieved;

	}
	
	private String[] findCompounds(String name, String formula) throws IOException {
		
		StringBuilder pubchemUrl = new StringBuilder();
//		pubchemUrl
//			.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?usehistory=n&db=pccompound&sort=cida&retmax=");
		pubchemUrl
			.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?usehistory=n&db=pccompound&retmax=");
		pubchemUrl.append(1);
		pubchemUrl.append("&term=");
		pubchemUrl.append("\"" + name + "\"[Synonym]");			
		if (formula != null) {
			pubchemUrl.append("%20AND%20" + formula);			
		}
		System.out.println("\tQuery: " + pubchemUrl);

		NodeList cidElements;

		try {
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = dbf.newDocumentBuilder();
		    Document parsedResult = builder.parse(pubchemUrl.toString());

		    XPathFactory factory = XPathFactory.newInstance();
		    XPath xpath = factory.newXPath();
		    XPathExpression expr = xpath.compile("//eSearchResult/IdList/Id");
		    cidElements = (NodeList) expr.evaluate(parsedResult,
			    XPathConstants.NODESET);

		} catch (Exception e) {
		    throw (new IOException(e));
		}

		String cidArray[] = new String[cidElements.getLength()];
		for (int i = 0; i < cidElements.getLength(); i++) {
		    Element cidElement = (Element) cidElements.item(i);
		    cidArray[i] = cidElement.getTextContent();
		}

		return cidArray;
		
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
		
		if (cids.length == 0) {
			System.out.println("\tNo compound to retrieve");
			return null;
		}

		StringBuilder builder = new StringBuilder();
		builder.append(PubChemQuery.QUERY_PROLOG);
		final String input = "/compound/cid/";
		builder.append(input);
		for(String s : cids) {
		    builder.append(s).append(",");
		}
		final String operation = "/property/IUPACName,MolecularFormula,MolecularWeight,MonoisotopicMass,Charge,InChIKey,InChI";
		builder.append(operation);
		builder.append(PubChemQuery.QUERY_OUTPUT);
		URI uri = constructUri(builder);

		HttpGet httpget = new HttpGet(uri);
		// System.out.println("GET: " + httpget.getURI());
		System.out.println("\tFetching " + cids.length + " compounds from PubChem");
		System.out.println("\t\t" + httpget);
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse = httpclient.execute(httpget);
		
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		PubChemMolecule[] pcMols = null;
		if (statusCode == HttpStatus.SC_OK) {

			InputStream is = httpResponse.getEntity().getContent();			
			
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		    mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		    
		    ObjectReader reader = mapper.reader(PugCidsToProperties.class).withRootName("PropertyTable");
			PugCidsToProperties retrieved = reader.readValue(is);
			pcMols = retrieved.getProperties();
			
			is.close();
			
		}
		
		return pcMols;
		
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
	 * Quick check to see if newFormula has been stored inside compounds before
	 * 
	 * @param compounds
	 * @param newFormula
	 * @return
	 */
	private boolean seenBefore(Set<PubChemMolecule> compounds, String newFormula) {
		for (PubChemMolecule compound : compounds) {
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
	 * Construct URI request to local PubChem snapshot (CouchDB)
	 * 
	 * @param path
	 * @param fromMass
	 * @param toMass
	 * @return
	 * @throws URISyntaxException
	 */
	private URI constructMassQueryUri(String path, double fromMass,
			double toMass) throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(CompoundQuery.QUERY_HOST_LOCAL)
				.setPath(path).setPort(CompoundQuery.QUERY_PORT_LOCAL);
		builder.setParameter("startkey", String.format("\"%.5f\"", fromMass));
		builder.setParameter("endkey", String.format("\"%.5f\"", toMass));
		builder.setParameter("include_docs", "true");
		URI uri = builder.build();
		return uri;
	}

}
