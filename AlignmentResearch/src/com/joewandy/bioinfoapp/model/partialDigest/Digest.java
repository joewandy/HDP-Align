package com.joewandy.bioinfoapp.model.partialDigest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Digest {
	private static final String CHROMOSOMES[] = { "chr1", "chr2", "chr3",
	/* etc */
	"chrY" };

	private static class Enzyme {
		String name;
		String site;

		public Enzyme(String name, String site) {
			this.name = name;
			this.site = site.toUpperCase();
		}
	}

	// see http://rebase.neb.com
	private static final Enzyme ENZYMES[] = new Enzyme[] {
			new Enzyme("AatII", "gacgtc"), new Enzyme("AbsI", "cctcgagg"),
			new Enzyme("Acc65I", "ggtacc"),
			/* (...) */
			new Enzyme("BamHI", "ggatcc"),
			/* (...) */
			new Enzyme("EcoRI", "gaattc"),
			/* (...) */
			new Enzyme("XmnI", "gaannnnttc") };

	private static boolean compatible(char a, char b) {
		b = Character.toUpperCase(b);
		if ("ATGC".indexOf(b) == -1)
			return false;
		switch (a) {
		case 'A':
			return b == a;
		case 'T':
			return b == a;
		case 'G':
			return b == a;
		case 'C':
			return b == a;

		case 'W':
			return "AT".indexOf(b) != -1;
		case 'S':
			return "GC".indexOf(b) != -1;
		case 'R':
			return "AG".indexOf(b) != -1;
		case 'Y':
			return "CT".indexOf(b) != -1;
		case 'M':
			return "AC".indexOf(b) != -1;
		case 'K':
			return "GT".indexOf(b) != -1;

		case 'B':
			return "CGT".indexOf(b) != -1;
		case 'D':
			return "AGT".indexOf(b) != -1;
		case 'H':
			return "ACT".indexOf(b) != -1;
		case 'V':
			return "ACG".indexOf(b) != -1;

		case 'N':
			return "ACGT".indexOf(b) != -1;

		default:
			return false;
		}
	}

	public static void main(String[] args) {
		try {
			for (Enzyme enzyme : ENZYMES) {
				for (String chr : CHROMOSOMES) {
					URL url = new URL(
							"http://hgdownload.cse.ucsc.edu/goldenPath/hg19/chromosomes/"
									+ chr + ".fa.gz");
					BufferedReader r = new BufferedReader(
							new InputStreamReader(new GZIPInputStream(
									url.openStream())));
					int c;
					int genome_pos = 0;
					int previous_pos = 0;
					char buffer[] = new char[enzyme.site.length()];
					int buffer_length = 0;
					while ((c = r.read()) != -1) {
						if (c == '>') {
							while ((c = r.read()) != -1 && c != '\n') { /* skip */
							}
							genome_pos = 0;
							buffer_length = 0;
							previous_pos = 0;
							continue;
						}
						if (!Character.isLetter(c))
							continue;
						buffer[buffer_length++] = (char) c;
						genome_pos++;

						if (buffer_length == buffer.length) {
							int i = 0;
							for (i = 0; i < buffer.length; ++i) {
								if (!compatible(enzyme.site.charAt(i),
										buffer[i]))
									break;
							}
							if (i == buffer.length) {
								System.out
										.println(chr
												+ "\t"
												+ enzyme.name
												+ "\t"
												+ (previous_pos)
												+ "\t"
												+ (genome_pos - buffer.length)
												+ "\t"
												+ ((genome_pos - buffer.length) - previous_pos));
								previous_pos = genome_pos - buffer.length;
							}
							buffer_length--;
							System.arraycopy(buffer, 1, buffer, 0,
									buffer.length - 1);
						}

					}
					r.close();
					System.out.println(chr + "\t" + enzyme.name + "\t"
							+ (previous_pos) + "\t"
							+ (genome_pos - buffer.length) + "\t"
							+ ((genome_pos - buffer.length) - previous_pos));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}