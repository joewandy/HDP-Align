// $header$



package cmdline;


//java





/**
 * This object contains the version of the project.
 * 
 * @author RA Scheltema
 */
public class Version
{
	/**
	 * This method converts the static version to a string. 
	 * 
	 * @return The string representation of the version.
	 */
	public static String convertToString()
	{
		return "" + major + "." + minor + "." + maintenance;
	}

	
	/** The major part of the version, which is changed for major interface changes */
	public static int major = 2;
	/** The minor part of the version, which is changed when interfaces have been added */
	public static int minor = 0;
	/** The maintenance part of the version, which is changed for bug-fixes */
	public static int maintenance = 0;
}
