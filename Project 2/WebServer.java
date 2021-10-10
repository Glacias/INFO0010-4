import java.net.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {

	private static ServerSocket ss;
	private static ExecutorService threadPool;
	private static ArrayList<Game> currentGameList;
	private static ArrayList<Game> hallOfFame;

	public static void main(String[] args)
	{
		try
		{
			if(args.length != 1)
			{
				throw new InvalidArgumentException("Wrong number of argument");
			}

			hallOfFame = new ArrayList<Game>();
			currentGameList = new ArrayList<Game>();

			int poolSize = Integer.parseInt(args[0]);
			ss = new ServerSocket(8036);
			threadPool = Executors.newFixedThreadPool(poolSize);

			int nbrOp = 0;
			while(true)
			{
				//Process new request
				threadPool.execute(new Worker(ss.accept(),currentGameList, hallOfFame));

				//Check for expired cookie
				if(nbrOp==100)
				{
					synchronized(currentGameList)
					{
						Date now = new Date();
						for(int i=0; i<currentGameList.size(); i++)
						{
							//If a game is expired delete it
							if(currentGameList.get(i).getExpiration().before(now))
							{
								currentGameList.remove(i);
							}
						}
					}
					nbrOp = 0;
				}
				nbrOp++;
			}
		}
		catch(InvalidArgumentException e)
		{
			System.err.println(e);
		}
		catch(NumberFormatException e)
		{
			System.err.println("Wrong argument type, please insert the max number of thread");
		}
		catch(Exception e)
		{
			System.err.println("Error Server : "+e);
		}
	}
}
