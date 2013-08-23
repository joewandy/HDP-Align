package com.joewandy.mzmatch.old;

//Axis-generated PUG SOAP classes
import gov.nih.nlm.ncbi.pubchem.CompressType;
import gov.nih.nlm.ncbi.pubchem.EntrezKey;
import gov.nih.nlm.ncbi.pubchem.FormatType;
import gov.nih.nlm.ncbi.pubchem.PUGLocator;
import gov.nih.nlm.ncbi.pubchem.PUGSoap;
import gov.nih.nlm.ncbi.pubchem.StatusType;
import gov.nih.nlm.ncbi.pubchem.holders.DataBlobTypeHolder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

public class MFDownload {

	private EntrezKey entrezKey;
	
	public MFDownload(EntrezKey entrezKey) {
		this.entrezKey = entrezKey;
	}
	
	public void download() throws ServiceException, InterruptedException, IOException {

		PUGLocator pug_locator = new PUGLocator();
		PUGSoap pug_soap = pug_locator.getPUGSoap();

		String listKey = pug_soap.inputEntrez(entrezKey);
		System.out.println("ListKey = " + listKey);

		// Initialize the download; request SDF with gzip compression
		StringHolder downloadKeyHolder = new StringHolder();
		DataBlobTypeHolder dataBlob = new DataBlobTypeHolder();
		pug_soap.download(listKey, FormatType.eFormat_XML,
				CompressType.eCompress_GZip,
				// default 3D args (no 3D in this case)
				false, 1, false,
				// holders are necessary because the Download function
				// may return one of two types; called this way it will return
				// a value in the download key
				downloadKeyHolder, dataBlob);
		String downloadKey = downloadKeyHolder.value;
		System.out.println("DownloadKey = " + downloadKey);

		// Wait for the download to be prepared
		StatusType status;
		while ((status = pug_soap.getOperationStatus(downloadKey)) == StatusType.eStatus_Running
				|| status == StatusType.eStatus_Queued) {
			System.out.println("Waiting for download to finish...");
			Thread.sleep(10000);
		}

		// On success, get the download URL, save to local file
		if (status == StatusType.eStatus_Success) {
			URL url = new URL(pug_soap.getDownloadUrl(downloadKey));
			System.out.println("Success! Download URL = " + url.toString());

			// get input stream from URL
			URLConnection fetch = url.openConnection();
			InputStream input = fetch.getInputStream();

			// open local file based on the URL file name
			String filename = "/home/joewandy/Downloads"
					+ url.getFile().substring(url.getFile().lastIndexOf('/'));
			FileOutputStream output = new FileOutputStream(filename);
			System.out.println("Writing data to " + filename);

			// buffered read/write
			byte[] buffer = new byte[10000];
			int n;
			while ((n = input.read(buffer)) > 0)
				output.write(buffer, 0, n);
		} else {
			System.out.println("Error: "
					+ pug_soap.getStatusMessage(downloadKey));
		}
	}
}

// $Id: ESearchDownload.java 206275 2010-09-27 18:05:14Z thiessen $
