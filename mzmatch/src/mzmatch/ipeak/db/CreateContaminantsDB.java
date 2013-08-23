package mzmatch.ipeak.db;

public class CreateContaminantsDB
{
	public static void main(String args[])
	{
//		try
//		{
//			HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
//			
//			int id = 0;
//			String line = "";
//			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("d:/ESI-contaminants.txt")));
//			while ((line = in.readLine()) != null)
//			{
//				String tokens[] = line.trim().split("\t");
//				
//				double mass = Double.parseDouble(tokens[0]);
//				String iontype = tokens[1];
//				String formula = tokens[2];
//				String name = tokens[3];
//				String description = tokens[4];
//				
//				try
//				{
//					MolecularFormula f = new MolecularFormula(iontype, formula);
//					if (Math.abs(mass-(f.getMass(Mass.MONOISOTOPIC) + PeriodicTable.proton)) < 1000)
//					{
//						String dbid = String.format("CONTAMINANTDB_%04d", id++);
//						Molecule molecule = new Molecule(dbid, name, f);
//						molecule.setDescription(description);
//						molecules.put(dbid, molecule);
//					}
//					
//					
//					if (Math.abs(mass-(f.getMass(Mass.MONOISOTOPIC))) > 0.001)
//						System.out.println(mass + "\t" + (f.getMass(Mass.MONOISOTOPIC) + PeriodicTable.proton));
//				}
//				catch (Exception e)
//				{
////					System.out.println(line);
////					e.printStackTrace();
//				}
//			}
//			
//			peakml.io.chemistry.MoleculeIO.writeXml(molecules, new FileOutputStream("d:/ESI-contaminants.xml"));
	}
}
