package com.joewandy.bioinfoapp.main;

import com.joewandy.bioinfoapp.model.alignment.distanceMatrix.BLOSSUM62SubstitutionMatrix;
import com.joewandy.bioinfoapp.model.alignment.distanceMatrix.PAM250SubstitutionMatrix;
import com.joewandy.bioinfoapp.model.alignment.pairwise.GlobalAlignment;

public class TestGlobalAlignment {

	public static void main(String[] args) {

		String sequence1 = "ATGCCTAATGC";
		String sequence2 = "ATG--AATG";

		System.out.println("Test simple DNA sequence aligment");
		String output = new GlobalAlignment.Builder(sequence1, sequence2)
				.build().align().printOutput();
		System.out.println(output);

		System.out.println("\nTest protein sequence aligment - PAM250");
		String protein1Desc = ">ECOICD_1 E.coli icd gene encoding isocitrate "
				+ "dehydrogenase, complete cds.";
		String protein1 = "MESKVVVPAQGKKITLQNGKLNVPENPIIPYIEGDGIGVDVTPAMLKVV"
				+ "DAAVEKAYKGERKISWMEIYTGEKSTQVYGQDVWLPAETLDLIREYRVA"
				+ "IKGPLTTPVGGGIRSLNVALRQELDLYICLRPVRYYQGTPSPVKHPELT"
				+ "DMVIFRENSEDIYAGIEWKADSADAEKVIKFLREEMGVKKIRFPEHCGI"
				+ "GIKPCSEEGTKRLVRAAIEYAIANDRDSVTLVHKGNIMKFTEGAFKDWG"
				+ "YQLAREEFGGELIDGGPWLKVKNPNTGKEIVIKDVIADAFLQQILLRPA"
				+ "EYDVIACMNLNGDYISDALAAQVGGIGIAPGANIGDECALFEATHGTAP"
				+ "KYAGQDKVNPGSIILSAEMMLRHMGWTEAADLIVKGMEGAINAKTVTYD"
				+ "FERLMDGAKLLKCSEFGDAIIENM";
		String protein2Desc = ">sequenceY_1";
		String protein2 = "MESKVVVPAEGKKITVDAQGKLVVPHNPIIPFIEGDGIGVDVTPAMINV"
				+ "VDAAVKKAYNGERKISWMEIYTGEKSTHVYGKDVWLPEETLDLIRDYRV"
				+ "AIKGPLTTPVGGGIRSLNVALRQQLDLYVCLRPVRYYEGTPSPVKHPEL"
				+ "TNMVIFRENAEDIYAGIEWKAGSPEAEKVIKFLREEMGVKKIRFPEQCG"
				+ "IGVKPCSEEGTKRLVRAAIEYAITNDRESVTLVHKGNIMKFTEGAFKDW"
				+ "GYQLAREEFGGELIDGGPWVK";

		output = new GlobalAlignment.Builder(protein1, protein2)
				.s1Desc(protein1Desc).s2Desc(protein2Desc).indelPenalty(-1)
				.substitutionMatrix(new PAM250SubstitutionMatrix()).build()
				.align().printOutput();
		System.out.println(output);

		System.out.println("\nTest protein sequence aligment - BLOSSUM62");
		output = new GlobalAlignment.Builder(protein1, protein2)
				.s1Desc(protein1Desc).s2Desc(protein2Desc).indelPenalty(-1)
				.substitutionMatrix(new BLOSSUM62SubstitutionMatrix()).build()
				.align().printOutput();
		System.out.println(output);

	}

}
