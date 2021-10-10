import java.net.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.text.*;

public class Worker extends Thread{
	private Socket socket;
	private ArrayList<Game> currentGameList;
	private ArrayList<Game> hallOfFame;
	private InputStream serverIn;
	private PrintWriter serverOut;

	public Worker(Socket s, ArrayList<Game> cgl, ArrayList<Game> hof)
	{
		try
		{
			this.socket = s;
			this.currentGameList = cgl;
			this.hallOfFame = hof;
			serverIn = this.socket.getInputStream();
			serverOut = new PrintWriter(this.socket.getOutputStream(), true);
			socket.setSoTimeout(120000);//Set timer of 1 minute
		}
		catch(Exception e)
		{
			System.err.println("Error Creating Worker : "+e);
		}

	}

	@Override
	public void run()
	{
		try
		{
			ParsedHTTPRequest request = new ParsedHTTPRequest(serverIn);
			URL reqURL = request.getURL();
			///Get method///
			if(request.getMethod().equals("GET"))
			{
				//Redirection
				if(reqURL.getPath().equals("/"))
				{
					serverOut.print("HTTP/1.1 303 See Other\r\n");
					serverOut.print("Location: /play.html\r\n");
					serverOut.print("\r\n");
					serverOut.flush();
				}
				//Request to play.html page
				else if(reqURL.getPath().equals("/play.html"))
				{
					//Get Cookie
					String reqCookie = request.getCookie();
					//Find corresponding game
					Game g = null;
					if(reqCookie!=null)
					{
						synchronized(currentGameList)
						{
							for(int i=0;i<currentGameList.size(); i++)
							{
								if(currentGameList.get(i).getCookie().equals(reqCookie))
								{
									g = currentGameList.get(i);
									break;
								}
							}
						}
					}
					///Check for query
					//Refresh or new game
					if(reqURL.getQuery()==null || reqURL.getQuery().isEmpty() || g==null)
					{
						//New Game requested
						if(reqCookie==null || g==null)
						{
							//Create a new cookie
							String newCookie;
							if(reqCookie==null)
							{
								newCookie = RandomString.getAlphaNumericString(10);
							}
							else
							{
								newCookie = reqCookie;
							}
							DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
							df.setTimeZone(TimeZone.getTimeZone("GMT"));
							Date now = new Date();
							Date expireTime = new Date(now.getTime()+600000);
							String expireString = df.format(expireTime);

							//Create a new game
							Game newGame = new Game(newCookie,expireTime);
							synchronized(currentGameList)
							{
								currentGameList.add(newGame);
							}

							//Generation of html
							HTMLGenerator gen = new HTMLGenerator();
							gen.generate(newGame);
							String htmlCode = gen.html();

							serverOut.print("HTTP/1.1 200 OK\r\n");
							serverOut.print("Connection : close\r\n");
							serverOut.print("Content-Type: text/html; charset=utf-8\r\n");
							serverOut.print("Set-Cookie: SESSID="+newCookie+"; path=/; expires=" + expireString + "\r\n");
							serverOut.print("Transfer-Encoding: chunked\r\n");
							if(request.gzipEncoding())
							{
								serverOut.print("Content-Encoding: gzip\r\n");
							}
							serverOut.print("\r\n");
							serverOut.flush();

							if(request.gzipEncoding())
							{
								gen.chunckEncoding(gen.compress(htmlCode), this.socket.getOutputStream());
								this.socket.getOutputStream().flush();
							}
							else
							{
								serverOut.print(gen.chunckEncoding(htmlCode));
								serverOut.flush();
							}

						}
						//Refresh
						else
						{
							//Generate a new cookie
							String oldCookie = g.getCookie();
							DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
							df.setTimeZone(TimeZone.getTimeZone("GMT"));
							Date now = new Date();
							Date expireTime = new Date(now.getTime()+600000);
							String expireString = df.format(expireTime);

							//Change cookie
							g.setExpiration(expireTime);

							//Generation of html
							HTMLGenerator gen = new HTMLGenerator();
							gen.generateCurrentGame(g);
							String htmlCode = gen.html();

							serverOut.print("HTTP/1.1 200 OK\r\n");
							serverOut.print("Connection : close\r\n");
							serverOut.print("Content-Type: text/html; charset=utf-8\r\n");
							serverOut.print("Set-Cookie: SESSID="+oldCookie+"; path=/; expires=" + expireString + "\r\n");
							serverOut.print("Transfer-Encoding: chunked\r\n");
							if(request.gzipEncoding())
							{
								serverOut.print("Content-Encoding: gzip\r\n");
							}
							serverOut.print("\r\n");
							serverOut.flush();

							if(request.gzipEncoding())
							{
								gen.chunckEncoding(gen.compress(htmlCode), this.socket.getOutputStream());
								this.socket.getOutputStream().flush();
							}
							else
							{
								serverOut.print(gen.chunckEncoding(htmlCode));
								serverOut.flush();
							}
						}
					}
					//Query not null and game found
					//Check for attempt
					else
					{
						//Receive fire instruction
						Coord fireAt = request.getAttempt();
						if(fireAt == null)
							throw new myHTTPException("400");


						//Update cookie
						String oldCookie = g.getCookie();
						DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
						df.setTimeZone(TimeZone.getTimeZone("GMT"));
						Date now = new Date();
						Date expireTime = new Date(now.getTime()+600000);
						String expireString = df.format(expireTime);

						//Update game
						synchronized(g)
						{
							g.fire(fireAt);
							g.setExpiration(expireTime);
						}

						//Send reply
						serverOut.print("HTTP/1.1 200 OK\r\n");
						serverOut.print("Connection : close\r\n");
						serverOut.print("Set-Cookie: SESSID="+oldCookie+"; path=/; expires=" + expireString + "\r\n");
						serverOut.print("Content-Type: JSON\r\n");
						serverOut.print("\r\n");
						serverOut.print("{\"state\":"+ g.checkStateOfTile(fireAt) +", \"tries\":"+ g.nbrShots() +", \"ship\":"+ g.nbrShipAlive() +"}");
						serverOut.flush();

						//Save or delete win/lose games
						//Win or lose
						if(g.nbrShipAlive()==0 || g.nbrShots()>=70)
						{
							//Losing at 70 is worse than wining at 70
							if(g.nbrShipAlive()!=0)
							{
								g.setNbrShot(71);
							}
							//Hall of Fame
							synchronized(hallOfFame)
							{
								int index;
								boolean newScore = true;
								for(index = 0 ; index < hallOfFame.size() ; index++)
								{
									if(hallOfFame.get(index).getCookie().equals(g.getCookie()))
									{
										if(hallOfFame.get(index).nbrShots() <= g.nbrShots())
											newScore = false;
										break;
									}
								}

								if(newScore)
								{
									if(index < 10 && index == hallOfFame.size())
										hallOfFame.add(null);
									for( ; index > 0 ; index--)
									{
										if(hallOfFame.get(index-1).nbrShots() > g.nbrShots())
										{
											if(index < 10)
												hallOfFame.set(index, hallOfFame.get(index-1));
										}
										else
											break;
									}
									if(index < 10)
										hallOfFame.set(index, g);
								}
							}
							//Delete
							synchronized(currentGameList)
							{
								for(int i=0;i<currentGameList.size(); i++)
								{
									if(currentGameList.get(i).getCookie().equals(g.getCookie()))
									{
										currentGameList.remove(i);
										break;
									}
								}
							}
						}
					}
				}
				//Request to halloffame.html page
				else if(reqURL.getPath().equals("/halloffame.html"))
				{
					//Generation of html
					HTMLGenerator gen = new HTMLGenerator();
					gen.generateHall(hallOfFame);
					String htmlCode = gen.html();

					serverOut.print("HTTP/1.1 200 OK\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html; charset=utf-8\r\n");
					serverOut.print("Transfer-Encoding: chunked\r\n");
					if(request.gzipEncoding())
					{
						serverOut.print("Content-Encoding: gzip\r\n");
					}
					serverOut.print("\r\n");
					serverOut.flush();

					if(request.gzipEncoding())
					{
						gen.chunckEncoding(gen.compress(htmlCode), this.socket.getOutputStream());
						this.socket.getOutputStream().flush();
					}
					else
					{
						serverOut.print(gen.chunckEncoding(htmlCode));
						serverOut.flush();
					}
				}
				//Request win page
				else if(reqURL.getPath().equals("/win.html"))
				{
					//Generation of html
					HTMLGenerator gen = new HTMLGenerator();
					gen.generateEndPage(true);
					String htmlCode = gen.html();

					serverOut.print("HTTP/1.1 200 OK\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html; charset=utf-8\r\n");
					serverOut.print("Transfer-Encoding: chunked\r\n");
					if(request.gzipEncoding())
					{
						serverOut.print("Content-Encoding: gzip\r\n");
					}
					serverOut.print("\r\n");
					serverOut.flush();

					if(request.gzipEncoding())
					{
						gen.chunckEncoding(gen.compress(htmlCode), this.socket.getOutputStream());
						this.socket.getOutputStream().flush();
					}
					else
					{
						serverOut.print(gen.chunckEncoding(htmlCode));
						serverOut.flush();
					}
				}
				//Request lose page
				else if(reqURL.getPath().equals("/lose.html"))
				{
					//Generation of html
					HTMLGenerator gen = new HTMLGenerator();
					gen.generateEndPage(false);
					String htmlCode = gen.html();

					serverOut.print("HTTP/1.1 200 OK\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html; charset=utf-8\r\n");
					serverOut.print("Transfer-Encoding: chunked\r\n");
					if(request.gzipEncoding())
					{
						serverOut.print("Content-Encoding: gzip\r\n");
					}
					serverOut.print("\r\n");
					serverOut.flush();

					if(request.gzipEncoding())
					{
						gen.chunckEncoding(gen.compress(htmlCode), this.socket.getOutputStream());
						this.socket.getOutputStream().flush();
					}
					else
					{
						serverOut.print(gen.chunckEncoding(htmlCode));
						serverOut.flush();
					}
				}
				//Error 404
				else
				{
					throw new myHTTPException("404");
				}
			}
			///Post method///
			else
			{
				///Attempt
				//Get Cookie
				String reqCookie = request.getCookie();
				//Find corresponding game
				Game g = null;
				if(reqCookie!=null)
				{
					synchronized(currentGameList)
					{
						for(int i=0;i<currentGameList.size(); i++)
						{
							if(currentGameList.get(i).getCookie().equals(reqCookie))
							{
								g = currentGameList.get(i);
								break;
							}
						}
					}
				}
				//If game expired => create a new one
				if(reqCookie==null || g==null)
				{
					//Create a new cookie
					String newCookie;
					if(reqCookie==null)
					{
						newCookie = RandomString.getAlphaNumericString(10);
					}
					else
					{
						newCookie = reqCookie;
					}
					DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
					df.setTimeZone(TimeZone.getTimeZone("GMT"));
					Date now = new Date();
					Date expireTime = new Date(now.getTime()+600000);
					String expireString = df.format(expireTime);

					//Create a new game
					Game newGame = new Game(newCookie,expireTime);
					synchronized(currentGameList)
					{
						currentGameList.add(newGame);
					}

					//Generation of html
					HTMLGenerator gen = new HTMLGenerator();
					gen.generate(newGame);
					String htmlCode = gen.html();

					serverOut.print("HTTP/1.1 200 OK\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html; charset=utf-8\r\n");
					serverOut.print("Set-Cookie: SESSID="+newCookie+"; path=/; expires=" + expireString + "\r\n");
					serverOut.print("Transfer-Encoding: chunked\r\n");
					if(request.gzipEncoding())
					{
						serverOut.print("Content-Encoding: gzip\r\n");
					}
					serverOut.print("\r\n");
					serverOut.flush();

					if(request.gzipEncoding())
					{
						gen.chunckEncoding(gen.compress(htmlCode), this.socket.getOutputStream());
						this.socket.getOutputStream().flush();
					}
					else
					{
						serverOut.print(gen.chunckEncoding(htmlCode));
						serverOut.flush();
					}
				}

				//Receive fire instruction
				Coord fireAt = request.getAttempt();
				if(fireAt == null)
					throw new myHTTPException("400");

				//Update cookie
				String oldCookie = g.getCookie();
				DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
				Date now = new Date();
				Date expireTime = new Date(now.getTime()+600000);
				String expireString = df.format(expireTime);

				//Update game
				synchronized(g)
				{
					g.fire(fireAt);
					g.setExpiration(expireTime);
				}

				//Save or delete win/lose games
				//Win or lose
				if(g.nbrShipAlive()==0 || g.nbrShots()>=70)
				{
					//Redirect to win or lose response
					if(g.nbrShipAlive()==0)
					{
						//Send redirect
						serverOut.print("HTTP/1.1 303 See Other\r\n");
						serverOut.print("Location: /win.html\r\n");
						serverOut.print("\r\n");
						serverOut.flush();
					}
					else
					{
						//Losing at 70 is worse than wining at 70
						g.setNbrShot(71);
						//Send redirect
						serverOut.print("HTTP/1.1 303 See Other\r\n");
						serverOut.print("Location: /lose.html\r\n");
						serverOut.print("\r\n");
						serverOut.flush();
					}
					//Hall of Fame
					synchronized(hallOfFame)
					{
						int index;
						boolean newScore = true;
						for(index = 0 ; index < hallOfFame.size() ; index++)
						{
							if(hallOfFame.get(index).getCookie().equals(g.getCookie()))
							{
								if(hallOfFame.get(index).nbrShots() <= g.nbrShots())
									newScore = false;
								break;
							}
						}

						if(newScore)
						{
							if(index < 10 && index == hallOfFame.size())
								hallOfFame.add(null);
							for( ; index > 0 ; index--)
							{
								if(hallOfFame.get(index-1).nbrShots() > g.nbrShots())
								{
									if(index < 10)
										hallOfFame.set(index, hallOfFame.get(index-1));
								}
								else
									break;
							}
							if(index < 10)
								hallOfFame.set(index, g);
						}
					}
					//Delete
					synchronized(currentGameList)
					{
						for(int i=0;i<currentGameList.size(); i++)
						{
							if(currentGameList.get(i).getCookie().equals(g.getCookie()))
							{
								currentGameList.remove(i);
								break;
							}
						}
					}
				}
				//Not win or lose
				else
				{
					//Send response
					//Generation of html
					HTMLGenerator gen = new HTMLGenerator();
					gen.generateCurrentGame(g);
					String htmlCode = gen.html();

					serverOut.print("HTTP/1.1 200 OK\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html; charset=utf-8\r\n");
					serverOut.print("Set-Cookie: SESSID="+oldCookie+"; path=/; expires=" + expireString + "\r\n");
					serverOut.print("Transfer-Encoding: chunked\r\n");
					if(request.gzipEncoding())
					{
						serverOut.print("Content-Encoding: gzip\r\n");
					}
					serverOut.print("\r\n");
					serverOut.flush();

					if(request.gzipEncoding())
					{
						gen.chunckEncoding(gen.compress(htmlCode), this.socket.getOutputStream());
						this.socket.getOutputStream().flush();
					}
					else
					{
						serverOut.print(gen.chunckEncoding(htmlCode));
						serverOut.flush();
					}
				}
			}
			serverOut.close();
			socket.close();
		}
		catch(myHTTPException e)
		{
			//Error Code
			switch(e.getMessage())
			{
				case "400" :
					serverOut.print("HTTP/1.1 400 Bad Request\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html\r\n");
					serverOut.print("\r\n");
					serverOut.print("<html>\n<body>\n<h1>\n400 Bad Request\n</h1>\n</body>\n</html>\n");
					serverOut.flush();
					break;
				case "404" :
					serverOut.print("HTTP/1.1 404 Not Found\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html\r\n");
					serverOut.print("\r\n");
					serverOut.print("<html>\n<body>\n<h1>\n404 Not Found\n</h1>\n</body>\n</html>\n");
					serverOut.flush();
					break;
				case "405" :
					serverOut.print("HTTP/1.1 405 Method Not Allowed\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html\r\n");
					serverOut.print("\r\n");
					serverOut.print("<html>\n<body>\n<h1>\n405 Method Not Allowed\n</h1>\n</body>\n</html>\n");
					serverOut.flush();
					break;
				case "411" :
					serverOut.print("HTTP/1.1 411 Length Required\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html\r\n");
					serverOut.print("\r\n");
					serverOut.print("<html>\n<body>\n<h1>\n411 Length Required\n</h1>\n</body>\n</html>\n");
					serverOut.flush();
					break;
				case "500" :
					serverOut.print("HTTP/1.1 500 Internal Server Error\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html\r\n");
					serverOut.print("\r\n");
					serverOut.print("<html>\n<body>\n<h1>\n500 Internal Server Error\n</h1>\n</body>\n</html>\n");
					serverOut.flush();
					break;
				case "501" :
					serverOut.print("HTTP/1.1 501 Not Implemented\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html\r\n");
					serverOut.print("\r\n");
					serverOut.print("<html>\n<body>\n<h1>\n501 Not Implemented\n</h1>\n</body>\n</html>\n");
					serverOut.flush();
					break;
				case "505" :
					serverOut.print("HTTP/1.1 505 HTTP Version Not Supported\r\n");
					serverOut.print("Connection : close\r\n");
					serverOut.print("Content-Type: text/html\r\n");
					serverOut.print("\r\n");
					serverOut.print("<html>\n<body>\n<h1>\n505 HTTP Version Not Supported\n</h1>\n</body>\n</html>\n");
					serverOut.flush();
					break;
				}
			}
		catch(InvalidCoordinatesException e)
		{
			//The coordinate to fire at are wrong
			serverOut.print("HTTP/1.1 400 Bad Request\r\n");
			serverOut.print("Connection : close\r\n");
			serverOut.print("Content-Type: text/html\r\n");
			serverOut.print("\r\n");
			serverOut.print("<html>\n<body>\n<h1>\n400 Bad Request : Wrong fire coordinates\n</h1>\n</body>\n</html>\n");
			serverOut.flush();
		}
		catch(Exception e)
		{
			serverOut.print("HTTP/1.1 500 Internal Server Error\r\n");
			serverOut.print("Connection : close\r\n");
			serverOut.print("Content-Type: text/html\r\n");
			serverOut.print("\r\n");
			serverOut.print("<html>\n<body>\n<h1>\n500 Internal Server Error\n</h1>\n</body>\n</html>\n");
			serverOut.flush();
		}
	}
}
