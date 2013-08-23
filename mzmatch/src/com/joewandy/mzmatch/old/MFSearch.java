package com.joewandy.mzmatch.old;


//Axis-generated PUG SOAP classes
import gov.nih.nlm.ncbi.pubchem.*;

public class MFSearch {

 public static void main (String[] argv) throws Exception {
     
	PUGLocator locator = new PUGLocator();
	PUGSoap soap = locator.getPUGSoap();

     // Initialize MF search
	String listKey = soap.MFSearch(
             "C7H19N3",                         // formula
             new MFSearchOptions(false, null), // don't allow other elements
             null);                            // no limits
	System.out.println("ListKey = " + listKey);
     
     // Wait for the search to finish
	StatusType status;
	while ((status = soap.getOperationStatus(listKey)) 
                 == StatusType.eStatus_Running || 
            status == StatusType.eStatus_Queued) 
     {
         System.out.println("Waiting for search to finish...");
	    Thread.sleep(10000);
	}
     
     // On success, get the results as an Entrez URL
     if (status == StatusType.eStatus_Success ||
         status == StatusType.eStatus_TimeLimit ||
         status == StatusType.eStatus_HitLimit) 
     {
         if (status == StatusType.eStatus_TimeLimit) {
             System.out.println(
                 "Warning: time limit reached before entire db searched");
         } else if (status == StatusType.eStatus_HitLimit) {
             System.out.println(
                 "Warning: hit limit reached before entire db searched");
         }
         int count = soap.getListItemsCount(listKey);
         if (count == 0) {
             System.out.println("No hits found");
         } else {
             EntrezKey entrezKey = soap.getEntrezKey(listKey);
             String URL = soap.getEntrezUrl(entrezKey);
             System.out.println("Success! Entrez URL = " + URL);
             MFDownload downloader = new MFDownload(entrezKey);
             downloader.download();
         }
     } else {
         System.out.println("Error: " 
             + soap.getStatusMessage(listKey));            
     }
 }
}

//$Id: MFSearch.java 219276 2011-01-07 17:18:25Z thiessen $
