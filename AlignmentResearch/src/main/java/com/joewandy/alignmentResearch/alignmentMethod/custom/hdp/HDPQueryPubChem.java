package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

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

import peakml.chemistry.Mass;
import peakml.chemistry.Molecule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.joewandy.mzmatch.model.PubChemMolecule;
import com.joewandy.mzmatch.model.PugCidsToProperties;
import com.joewandy.mzmatch.query.BaseQuery;
import com.joewandy.mzmatch.query.CompoundQuery;

/**
 * Queries PUG for other compounds within mass tolerance
 * 
 * @author joewandy
 * 
 */
public class HDPQueryPubChem extends BaseQuery implements CompoundQuery {

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

	public HDPQueryPubChem() {
		super.result = new HashSet<Molecule>();
		this.numResults = DEFAULT_NUM_RESULTS;		
		this.batchSize = DEFAULT_BATCH_SIZE;
	}
	
	public HDPQueryPubChem(Map<String, Molecule> molecules) {
		this(molecules, HDPQueryPubChem.DEFAULT_NUM_RESULTS, HDPQueryPubChem.DEFAULT_BATCH_SIZE);
	}

	public HDPQueryPubChem(Map<String, Molecule> molecules, int numResults, int batchSize) {
		super.result = new HashSet<Molecule>();
		this.numResults = numResults;
		this.batchSize = batchSize;
	}
	
	public Set<Molecule> findCompoundsByMass(double mass, double ppm,
			double delta) throws Exception {
		
//		System.out.print("Querying remote PubChem");
//		System.out.print("\tmass=" + String.format("%.5f", mass));
//		System.out.print("\tdelta=" + String.format("%.5f", delta));
//		System.out.println("\tppm=" + String.format("%.1f", ppm));

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
		
		// remove duplicates --> isomers sharing the same formulae
		Set<PubChemMolecule> rows = new HashSet<PubChemMolecule>(temp);
//		System.out.println("Found " + rows.size() + " other formulae");
//		System.out.println("Inserting: ");
		
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
			
			Molecule mol = null;
			try {
				// will throw exception if anything wrong with the formula
				mol = new Molecule(pc.getCid(), pc.getIupacName(), pc.getMolecularFormula());	
			} catch (RuntimeException e) {
				// ignore any exception handling for now
//				System.out.println("Error parsing formula " + e.getMessage());
			}
			if (mol != null) {
				
				mol.setInChi(pc.getInChi());
				mol.setMass(Double.parseDouble(pc.getMonoisotopicMass()));
//				System.out.println("\t" + pc.getIupacName() + " " + pc.getMolecularFormula() + " " + pc.getMonoisotopicMass());					
				retrieved.add(mol);					
				
				counter++;
				
			}
								
		}
		
//		System.out.println(counter + " inserted.");
		
		result.addAll(retrieved);
		return retrieved;

	}

	private PubChemMolecule[] findMoleculesByCids(String[] cids) throws URISyntaxException, ClientProtocolException, IOException {

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
		builder.append(HDPQueryPubChem.QUERY_PROLOG);
		final String input = "/compound/cid/";
		builder.append(input);
		for(String s : cids) {
		    builder.append(s).append(",");
		}
		final String operation = "/property/IUPACName,MolecularFormula,MolecularWeight,MonoisotopicMass,Charge,InChIKey,InChI";
		builder.append(operation);
		builder.append(HDPQueryPubChem.QUERY_OUTPUT);
		URI uri = constructUri(builder);

		HttpGet httpget = new HttpGet(uri);
		// System.out.println("GET: " + httpget.getURI());
//		System.out.println("\tFetching " + cids.length + " compounds from PubChem");
//		System.out.println("\t\t" + httpget);
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
		builder.setScheme("http").setHost(HDPQueryPubChem.QUERY_HOST).setPath(path);
		URI uri = builder.build();
		return uri;
	}
	
	public static void main(String [] args) throws Exception {
		
		HDPQueryPubChem query = new HDPQueryPubChem();
		final double ppm = 10;				
		double[] terms = { 217.095, 218.098, 478.154, 576.126, 318.106, 347.982, 577.13 };
		for (double term : terms) {
			queryAndPrint(query, ppm, term);
			System.out.println();
		}

		
	}

	private static void queryAndPrint(HDPQueryPubChem query, final double ppm,
			double term) throws Exception {
		System.out.println("Query PubChem for monoisotopic mass " + term + " at " + ppm + " ppm");
		Set<Molecule> results = query.findCompoundsByMass(term, ppm, 0);
		System.out.println("Top-10 Results:");
		for (Molecule mol : results) {
			System.out.println("\tid=" + mol.getDatabaseID() + "\tname=" + mol.getName() + "(" + mol.getPlainFormula() + ")" + "\tmass=" + mol.getMass(Mass.MOLECULAR, true));
		}
	}
	
}
