import java.io.*;
import java.lang.*;
import java.net.*;

public class BattleshipServer
{
  public static void main(String[] args)
  {
    try
    {
      //Create the server socket
      ServerSocket ss = new ServerSocket(2345);
      while(true)
      {
        //Wait fo a new connection and launches a worker to handle it
        Socket ts = ss.accept();
        Worker w = new Worker(ts);
        w.start();
      }
    }
    catch(Exception e)
    {
      System.err.println("Server crashed : " + e);
    }
  }
}
