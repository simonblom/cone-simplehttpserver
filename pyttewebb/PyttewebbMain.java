package pyttewebb;

/**
 * A simple web server that supports HTTP 1.0 and HTTP 1.1
 * @author Simon Blom
 * @version 2016-02-19
 *
 */
public class PyttewebbMain 
{
	/**
     * Program entry point. Usage: java PyttewebbMain [port]
     *
     * @param args not used
     */
	public static void main(String[] args)
	{
		int port = 8080;
		
		if (args.length > 0) 
		{
            try 
            {
                port = Integer.valueOf(args[0]);
            } 
            catch (NumberFormatException nfe)
            {
                System.out.println("Non integer value for port number, using " + port + " instead");
            }
        }
		
		Pyttewebb server = new Pyttewebb(port);
		System.out.println("Starting server, listening on port " + port);
		try 
		{
			server.run();
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
		}
	}
}