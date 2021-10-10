import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class ParsedHTTPRequest{

	private String method;
	private URL myURL;
	private String reqHTTPVersion;
	private ArrayList<String> options = new ArrayList<String>();
	private int contentLength = 0;
	private String content;


	public ParsedHTTPRequest(InputStream in) throws myHTTPException
	{
		try
		{
			BufferedReader bufferedIn = new BufferedReader(new InputStreamReader(in));

			// Parse Request Line
			String currLine = bufferedIn.readLine();
			StringTokenizer requestLine = new StringTokenizer(currLine);
			if(requestLine.countTokens() != 3)
			throw new myHTTPException("400");

			// Check method
			method = requestLine.nextToken();
			if(!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST") &&!method.equals("PUT") &&!method.equals("DELETE") &&!method.equals("CONNECT") &&!method.equals("OPTIONS") &&!method.equals("TRACE"))
			throw new myHTTPException("405");

			if(!method.equals("GET") && !method.equals("POST"))
			throw new myHTTPException("501");

			// Get URL relative path
			String host = null;
			String relativePath = requestLine.nextToken();

			// Check HTTP version
			reqHTTPVersion = requestLine.nextToken();
			if(!reqHTTPVersion.equals("HTTP/1.1"))
			throw new myHTTPException("505");

			// Get header
			currLine = bufferedIn.readLine();
			while(!currLine.isEmpty())
			{
				// Invalid option line (must contains ':')
				if(currLine.indexOf(':') == -1)
				throw new myHTTPException("400");

				options.add(currLine);

				// Try to get the host for complete URL
				if(currLine.length() >= 4 && currLine.substring(0,4).equals("Host"))
				host = currLine.substring(6); // "Host: "

				// Try to get content length
				if(currLine.length() >= 14 && currLine.substring(0,14).equals("Content-Length"))
				contentLength = Integer.parseInt(currLine.substring(16));

				currLine = bufferedIn.readLine();
			}

			// If POST, length mandatory
			if(method.equals("POST") && contentLength == 0)
			throw new myHTTPException("411");

			// Read content
			if(contentLength > 0)
			{
				char[] charBuff = new char[contentLength];
				// Check that enough char has been read
				if(bufferedIn.read(charBuff, 0, contentLength) != contentLength)
				throw new myHTTPException("400");

				content = new String(charBuff);
			}

			// Host mandatory in HTTP/1.1
			if(host == null)
			throw new myHTTPException("400");

			myURL = new URL(new URL("http://"+host), relativePath);

			//bufferedIn.close();
			//inStream.close();

		}
		// Unable to build an URL from given field
		catch(MalformedURLException e)
		{
			throw new myHTTPException("400");
		}
		// Unable to parse an int in content length
		catch(NumberFormatException e)
		{
			throw new myHTTPException("400");
		}
		// if nextToken() fail (not supposed to occur)
		catch(NoSuchElementException e)
		{
			throw new myHTTPException("500"); // internal error server ?
		}
		// String exception
		catch(IndexOutOfBoundsException e)
		{
			throw new myHTTPException("500"); // internal error server ?
		}
		// Reader Exception
		catch(IOException e)
		{
			throw new myHTTPException("500"); // internal error server ?
		}
	}

	public String getOptionValue(String optionName)
	{
		for(int i = 0 ; i < options.size() ; i++)
		{
			String myOption = options.get(i);
			int separator = myOption.indexOf(':');
			String myOptionName = myOption.substring(0,separator);

			if(myOptionName.equals(optionName))
			return myOption.substring(separator+2);
		}
		return null;
	}

	public String getMethod()
	{
		return method;
	}

	public URL getURL()
	{
		return myURL;
	}

	public String getContent()
	{
		return content;
	}

	public boolean gzipEncoding()
	{
		String encodeValue = getOptionValue("Accept-Encoding");

		if(encodeValue == null || !encodeValue.contains("gzip"))
			return false;

		return true;
	}

	public String getCookie()
	{
		String value = getOptionValue("Cookie");
		if(value == null)
			return null;
		String[] cookies = value.split("; ");
		for(int i = 0 ; i < cookies.length ; i++)
			if(cookies[i].length() >= 7 && cookies[i].substring(0,7).equals("SESSID="))
				return cookies[i].substring(7, cookies[i].length());
		return null;
	}

	public Coord getAttempt() throws myHTTPException, InvalidCoordinatesException
	{
		Coord c = null;
		try
		{
			// Get attempt in content
			if(method.equals("POST"))
			{
				String content = getContent();
				if(content.length() >= 4 && content.substring(0,4).equals("row="))
				{
					String row = content.substring(4,5);
					if(content.length() >= 14 && content.substring(5,13).equals("&column="))
					{
						String column = content.substring(13,content.length());
						c = new Coord(row.charAt(0),Integer.parseInt(column));
					}
				}
			}

			// Get attempt in URL
			if(method.equals("GET"))
			{
				String query = myURL.getQuery();
				if(query.length() >= 9 && query.substring(0,9).equals("fire=cell"))
				{
					String pos = query.substring(9,query.length());
					c = new Coord(Integer.parseInt(pos)-1);
				}
			}
		}
		catch(NumberFormatException e)
		{
			throw new myHTTPException("400");
		}
		return c;
	}

}
