package pyttewebb;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A simple web server that supports HTTP 1.0 and HTTP 1.1
 * 
 * @author Simon Blom
 * @version 2016-02-19
 *
 */
public class Pyttewebb 
{
	private final String CRLF = "\r\n"; //Carriage Return Line Feed
	private final String LOCALHOST = "127.0.0.1";
	private int mPort;
	private String mRequestMethod = "";
	private String mFileName;
	
	/**
	 * Constructs a server that can listen to a port.
	 * 
	 * @param port port the server listens to
	 */
	public Pyttewebb(int port)
	{
		mPort = port;
	}
	
	/**
	 * Opens socket and runs the server.
	 * 
	 * @throws IOException if there was a problem running the server
	 */
	public void run() throws IOException
	{
		boolean running = true;
		ServerSocket serverSocket = new ServerSocket(mPort);
		while(running)
		{
			running = this.handleRequest(serverSocket.accept());
		}
		serverSocket.close();
	}
	
	/**
	 * Handles requests given to server.
	 * 
	 * @param clientHandlerSocket socket associated with the request
	 * @return true if request did not start with QUIT
	 * @throws IOException if there was a problem reading or writing
	 */
	private boolean handleRequest(Socket clientHandlerSocket) throws IOException
	{
		mFileName = "error400.html"; //default;
		DataOutputStream writer = new DataOutputStream(clientHandlerSocket.getOutputStream());
		InputStream input = clientHandlerSocket.getInputStream();
		
		String request = this.readStream(input, CRLF+CRLF);
		if(request.startsWith("QUIT"))
		{
			System.out.println("Shutting down server");
			return false;
		}
		this.parseRequest(request);
		byte[] file = this.readFile(writer, mFileName);
		this.send(writer, file, mFileName);
		return true;
	}
	
	/**
	 * Read from the stream until provided stop sign is encountered.
	 * 
	 * @param input stream the server reads from
	 * @param stop server reads until this is encountered
	 * @return all character up to and including stop
	 * @throws IOException if there was a problem while reading stream
	 */
	private String readStream(InputStream input, String stop) throws IOException
	{
        int avaliable = 0;
        String received = "";

        while (!received.endsWith(stop)) {
            avaliable = input.available();

            byte[] bytes = new byte[avaliable];
            input.read(bytes);

            received += new String(bytes);
        }
        return received.trim();
	}
	
	/**
	 * Parses given request.
	 * 
	 * @param request request sent to server
	 */
	private void parseRequest(String request)
	{
		String [] requestLine = request.split(" ");
		String [] requestRow = request.split(CRLF);
		
		if(requestLine.length > 2)
		{
			mRequestMethod = requestLine[0];
			String resource = requestLine[1];
			String protocol = requestLine[2];
			
			if(protocol.equals("HTTP/1.0"))
			{
				this.parseMethod(resource);
			}
			//compatibility with web browser
			else if (requestRow.length > 1 && protocol.startsWith("HTTP/1.1"))
			{
				if(requestRow[1].equals("Host: "+LOCALHOST+":"+mPort)) 
				{
					this.parseMethod(resource);
				}
			}
		}
	}
	
	/**
	 * Parses given method in request and declares mFileName accordingly.
	 * 
	 * @param resource requested resource
	 */
	private void parseMethod(String resource)
	{
		if(mRequestMethod.equals("GET") || mRequestMethod.equals("HEAD"))
		{
			if(resource.startsWith("/"))
			{
				resource = resource.substring(1);
			}
			
			if(!resource.equals(""))
			{
				if(Files.exists(FileSystems.getDefault().getPath(resource)))
				{
					mFileName = resource;
				}
				else
				{
					mFileName = "error404.html";
				}
			}
		}
	}
	
	/**
	 * Parses response from server.
	 * 
	 * @param file used to give type and length in headers
	 * @return status and headers as string
	 */
	private String parseResponse(byte[] file)
	{
		String response = "";
		String status = "";
		String header = "";
		if (mFileName.equals("error404.html"))
		{
			status = "HTTP/1.1 404 File not found"+CRLF;
		}
		else if (mFileName.equals("error400.html"))
		{
			status = "HTTP/1.1 400 Bad Request"+CRLF;
		}
		else
		{
			status = "HTTP/1.1 200 OK"+CRLF;
		}
		header += "Content-Type: "+getFileType()+CRLF;
		header += "Content-Length: "+file.length+CRLF;
		//more headers can be added
		response += status+header+CRLF;
		return response;
	}
	
	/**
	 * Gets file type of mFileName.
	 * 
	 * @return file type in MIME format as String
	 */
	private String getFileType()
	{
		String fileType = "";
		if(mFileName.endsWith(".html") || mFileName.endsWith(".htm"))
		{
			fileType = "text/html";
		}
		else if (mFileName.endsWith(".gif"))
		{
			fileType = "image/gif";
		}
		else if (mFileName.endsWith(".txt"))
		{
			fileType = "text/plain";
		}
		//more file types can be added
		return fileType;
	}
	
	/**
	 * Send a file with the provided writer.
	 * 
	 * @param writer Writer we want to use
	 * @param file The file we want to send
	 * @throws IOException In case of a IO problem while sending
	 */
	private void send(DataOutputStream writer, byte[] file, String fileName) throws IOException
	{
		String response = this.parseResponse(file);
		writer.writeBytes(response);
		if(!mRequestMethod.equals("HEAD"))
		{
			writer.write(file);
		}
		writer.close();
	}
	
	/**
	 * Reads a small file into a byte[].
	 * 
	 * @param fileName Name of the file we want to read
	 * @return The file as a byte[]
	 * @throws IOException In case of a IO problem while reading
	 */
	private byte[] readFile(DataOutputStream writer, String fileName) throws IOException
	{
		Path file = Paths.get(fileName);
		return Files.readAllBytes(file);
	}
}