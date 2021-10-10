import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

class Worker extends Thread
{
  Socket s;
  public Worker(Socket ts)
  {
    s = ts;
  }

  @Override
  public void run()
  {
    try
    {
      //Setup of TCP Socket
      s.setSoTimeout(5*60*1000); //Set time out to 5min
      OutputStream out = s.getOutputStream();
      BufferedInputStream bufIn = new BufferedInputStream(s.getInputStream());

      //Lauching game
      Game g = new Game();
      boolean endGame = false;

      //Send Server Response : A new game was started
      byte[] newGameStarted = {1,1};
      out.write(newGameStarted);
      out.flush();

      int len;
      byte[] header = new byte[2];
      int fireCoords;
      while(true)
      {
        //Check for a game end
        if(g.nbrHits()==(5+4+3+3+2) || g.nbrShots()>=70)
        {
          endGame = true;
        }

        //Wait for a header and read it
        len = 0;
        bufIn.mark(3);
        while(len == 0 || len == 1)
        {
          bufIn.reset();
          len = bufIn.read(header, 0, 2);
        }

        if(header[0]==1)
        {
          if(header[1]==0)
          {
            ///Option 1 : Request for a new game///
            g = new Game();
            endGame = false;

            //Send Server Response : A new game was started
            byte[] gameStarted = {1,1};
            out.write(gameStarted);
            out.flush();
          }
          else if(header[1]==1)
          {
            ///Option 2 : Request for firing///
            fireCoords = bufIn.read();

            if(fireCoords>=0 && fireCoords<=99 && !endGame)
            {
              Coord c = new Coord(fireCoords);
              int type = g.fire(c);

              //Send confirmation of fire
              byte[] succesfullFire = {1,2,(byte) type};
              out.write(succesfullFire);
              out.flush();
            }
            else
            {
              //Request Error
              byte[] requestError = {1,4};
              out.write(requestError);
              out.flush();
            }
          }
          else if (header[1]==2 && !endGame)
          {
            ///Option 3 : Request for the list///
            int nbrOfAllShots = g.listShotsFired().size();
            ArrayList<Coord> shotsFired = g.listShotsFired();
            ArrayList<Integer> typeOfShipHit = g.listTypeOfShipHit();

            //Send the list
            byte[] list = new byte [(3+(2*nbrOfAllShots))];
            list[0] = 1;
            list[1] = 3;
            list[2] = (byte) nbrOfAllShots;
            int k=0;
            while(k < (2*nbrOfAllShots))
            {
              list[k+3] = (byte) shotsFired.get(k/2).getOrder();
              list[k+4] = (byte) ((int) typeOfShipHit.get(k/2));
              k=k+2;
            }
            out.write(list);
            out.flush();
          }
          else
          {
            //Request Error
            byte[] requestError = {1,4};
            out.write(requestError);
            out.flush();
          }
        }
        else
        {
          //Request Error
          byte[] requestError = {1,4};
          out.write(requestError);
          out.flush();
        }
      }
    }
    catch(InvalidCoordinatesException e)
    {
      System.err.println("Worker died : Invalid Coordinates" + e);
    }
    catch(InvalidArgumentException e)
    {
      System.err.println("Worker died : Invalid Argument" + e);
    }
    catch(Exception e)
    {
      System.err.println("Worker died : " + e);
    }
  }
}
