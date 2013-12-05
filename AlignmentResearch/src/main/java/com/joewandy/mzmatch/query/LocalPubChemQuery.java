package com.joewandy.mzmatch.query;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import mzmatch.ipeak.util.GeneralMassSpectrum;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import peakml.chemistry.Molecule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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
public class LocalPubChemQuery extends BaseQuery implements CompoundQuery {

	public LocalPubChemQuery() {
		super.result = new HashSet<Molecule>();
	}

	@SuppressWarnings("unused")
	public Set<Molecule> findCompoundsByMass(double mass, double ppm,
			double delta) throws Exception {

		System.out.println("Querying local PubChem snapshot");
		System.out
				.println("\tmonoisotopic mass=" + String.format("%.5f", mass));
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
		URI uri = constructMassQueryUri(CompoundQuery.QUERY_PATH_LOCAL,
				fromMass, toMass);

		HttpGet httpget = new HttpGet(uri);
		System.out.println("GET: " + httpget.getURI());
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse = httpclient.execute(httpget);

		// the actual code that queries local couchdb of compounds
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		System.out.println("statusCode=" + statusCode);
		CouchDbRow[] rows = null;
		if (statusCode == HttpStatus.SC_OK) {

			InputStream is = httpResponse.getEntity().getContent();

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(
					DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);

			ObjectReader reader = mapper.reader(CouchDbResponse.class);
			CouchDbResponse couchDbResponse = reader.readValue(is);
			rows = couchDbResponse.getRows();
			is.close();

		}

		// process the result
		Set<Molecule> unique = new HashSet<Molecule>();
		if (rows != null) {

			// filter out the isomers
			System.out.println("Found " + rows.length
					+ " matching compound(s) in mass range");
			Set<PubChemSnapshot> pubchems = new HashSet<PubChemSnapshot>();
			for (int i = 0; i < rows.length; i++) {

				PubChemSnapshot toAdd = rows[i].getDoc();
				String compoundFormula = toAdd.getMolecularFormula();
				String[] tokens = compoundFormula.split("(\\+|\\-)");
				String toCheck = tokens[0];
				if (compoundFormula == null) {
					continue;
				}
				
				String formula = "";
				boolean sameString = formula.equals(toCheck);
				boolean seenBefore = seenBefore(pubchems, toCheck);
				if (!sameString && !seenBefore) {
					pubchems.add(toAdd);
				}

			}

			// map pubchem result to peakml compounds
			System.out.println("Found " + pubchems.size()
					+ " other unique formulae");
			for (PubChemSnapshot pc : pubchems) {
				System.out.println("\t" + pc);
				try {
					Molecule mol = new Molecule(pc.getId(),
							pc.getTraditionalName(), pc.getMolecularFormula());	
					mol.setInChi(pc.getIupacInchi());
					mol.setMass(pc.getMonoisotopicWeight());
					unique.add(mol);
				} catch (RuntimeException e) {
					// ignore
					System.out.println(e.getMessage());
				}
			}
			result.addAll(unique);

		}

		return unique;

	}
	
	/**
	 * Quick check to see if newFormula has been stored inside compounds before
	 * 
	 * @param compounds
	 * @param newFormula
	 * @return
	 */
	private boolean seenBefore(Set<PubChemSnapshot> compounds, String newFormula) {
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
