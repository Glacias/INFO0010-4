import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

class BattleshipClient
{
  public static final int TIMEOUTDELAY = 10000;//Time out delay = 10 sec
  public static void main(String[] args)
  {
    try
    {
      //Connecting to server (will launch a game automatically)
      Socket s = new Socket("localhost",2345);
      s.setSoTimeout(TIMEOUTDELAY);
      OutputStream out = s.getOutputStream();
      BufferedInputStream bufIn = new BufferedInputStream(s.getInputStream());

      ///Check that the automatically launched game as started
      //Start timer to check for time out
      long startTime = System.currentTimeMillis();
      long elapsedTime = 0L;
      int len = 0;
      byte[] header = new byte[2];
      bufIn.mark(2);
      //Looks for a new header to read
      while (len == 0 || len == 1)
      {
        bufIn.reset();
        len = bufIn.read(header, 0, 2);

        //Time Out Check
        elapsedTime = (new Date()).getTime() - startTime;
        if(elapsedTime > TIMEOUTDELAY)
        {
          throw new TimeOutException("Timed out waiting for server response");
        }
      }

      //Verifing Response
      if(header[0]!=1 || header[1]!=1)
      {
        System.out.printf("Game was not started\n");
      }

      ///Start the menu
      Scanner sc = new Scanner(System.in);
      int x = 0;
      while(true)
      {
        //Restart the menu in the case of a wrong input
        boolean validChoice = false;
        while(!validChoice)
        {
          try
          {
            //Interface
            System.out.printf("------------------------------------------------\n");
            System.out.printf("                Battleship Menu !\n");
            System.out.printf("1) Restart a new game\n");
            System.out.printf("2) Fire at a given position\n");
            System.out.printf("3) See the list of already proposed tiles\n");
            System.out.printf("4) Quit\n");
            System.out.printf("------------------------------------------------\n\n");

            sc = new Scanner(System.in);
            System.out.printf("Your choice : ");
            x = sc.nextInt();
            validChoice = true;
          }
          catch(InputMismatchException e)
          {
            System.out.printf("Wrong input ... try again !\n\n");
          }
          catch(NumberFormatException e)
          {
            System.out.printf("Wrong input ... try again !\n");
          }
        }

        if(x==1)
        {
          ///Option 1 : Ask for new game///
          byte[] askForNewGame = {1,0};
          out.write(askForNewGame);
          out.flush();

          //Check for response
          len = 0;
          bufIn.mark(2);
          startTime = System.currentTimeMillis();
          elapsedTime = 0L;
          while (len == 0 || len == 1)
          {
            bufIn.reset();
            len = bufIn.read(header, 0, 2);

            //Time Out Check
            elapsedTime = (new Date()).getTime() - startTime;
            if(elapsedTime > TIMEOUTDELAY)
            {
              throw new TimeOutException("Timed Out waiting for new game confirmation");
            }
          }

          //Verifing Response
          if(header[0]!=1 || header[1]!=1)
          {
            System.out.printf("New game was not started\n");
          }
          else
          {
            System.out.printf("New game started !\n");
          }
        }
        else if(x==2)
        {
          ///Option 2 : Fire at a position///
          boolean ready = false;
          while(!ready)
          {
            try
            {
              //Collect position
              System.out.printf("Enter tile position (ex:C6) : ");
              sc = new Scanner(System.in);
              String str = sc.nextLine();
              char row = str.charAt(0);
              int column = Integer.parseInt(str.replaceAll("[\\D]", ""));
              Coord c = new Coord(row, column);
              ready = true;

              //Send fire request
              byte[] fire = {1,1,(byte) c.getOrder()};
              out.write(fire);
              out.flush();
            }
            catch(InvalidCoordinatesException e)
            {
              System.out.println(e);
            }
            catch(NumberFormatException e)
            {
              System.out.printf("Wrong input ... try again !\n");
            }
          }

          //Check for response
          len = 0;
          bufIn.mark(2);
          startTime = System.currentTimeMillis();
          elapsedTime = 0L;
          while (len == 0 || len == 1)
          {
            bufIn.reset();
            len = bufIn.read(header, 0, 2);

            //Time Out Check
            elapsedTime = (new Date()).getTime() - startTime;
            if(elapsedTime > TIMEOUTDELAY)
            {
              throw new TimeOutException("Timed Out waiting for fire confirmation");
            }
          }

          //Verifing Response
          if(header[0]!=1 || header[1]!=2)
          {
            System.out.printf("Shot not registered\n");
          }
          else
          {
            //Read the type
            int type = bufIn.read();
            //Type 0 = Water
            //Type 1 = size 2 = Destroyer
            //Type 2 = size 3 = Submarine
            //Type 3 = size 3 = Cruiser
            //Type 4 = size 4 = Battleship
            //Type 5 = size 5 = Carrier

            //Display a miss or the name of the ship that was it
            if(type==0)
            {
              System.out.printf("\nSplash !! You hit the water...\n");
            }
            else if (type==1)
            {
              System.out.printf("\nBoom !! You hit the Destroyer !\n");
            }
            else if (type==2)
            {
              System.out.printf("\nBoom !! You hit the Submarine !\n");
            }
            else if (type==3)
            {
              System.out.printf("\nBoom !! You hit the Cruiser !\n");
            }
            else if (type==4)
            {
              System.out.printf("\nBoom !! You hit the Battleship !\n");
            }
            else if (type==5)
            {
              System.out.printf("\nBoom !! You hit the Carrier !\n");
            }
            else
            {
              System.out.printf("Invalid Target Hit\n");
            }
          }
        }
        else if(x==3)
        {
          ///Option 3 : Ask for the list///
          byte[] askForList = {1,2};
          out.write(askForList);
          out.flush();

          //Check for response
          len = 0;
          bufIn.mark(2);
          startTime = System.currentTimeMillis();
          elapsedTime = 0L;
          while (len == 0 || len == 1)
          {
            bufIn.reset();
            len = bufIn.read(header, 0, 2);

            //Time Out Check
            elapsedTime = (new Date()).getTime() - startTime;
            if(elapsedTime > TIMEOUTDELAY)
            {
              throw new TimeOutException("Timed Out waiting for list confirmation");
            }
          }

          //Verifing Response
          if(header[0]!=1 || header[1]!=3)
          {
            System.out.printf("List header not received\n");
          }
          else
          {
            //Read the number of shots (double counted) to get the size of the list
            System.out.printf("List of guessed positions :\n");
            int nbrOfShotsFired = bufIn.read();

            //Reading the list sent by the server
            len = 0;
            bufIn.mark(2*nbrOfShotsFired);
            startTime = System.currentTimeMillis();
            elapsedTime = 0L;
            byte[] list = new byte[2*nbrOfShotsFired];
            while(len < 2*nbrOfShotsFired)
            {
              bufIn.reset();
              len = bufIn.read(list, 0, 2*nbrOfShotsFired);

              //Time Out Check
              elapsedTime = (new Date()).getTime() - startTime;
              if(elapsedTime > TIMEOUTDELAY)
              {
                throw new TimeOutException("Timed Out waiting for list confirmation");
              }
            }

            //Translating answer
            for(int i=0; i<2*nbrOfShotsFired; i=i+2)
            {
              Coord c = new Coord(list[i]);
              System.out.printf("Position : " + c.getAlphaCoord() + " ==> ");
              //Type 0 = Water
              //Type 1 = size 2 = Destroyer
              //Type 2 = size 3 = Submarine
              //Type 3 = size 3 = Cruiser
              //Type 4 = size 4 = Battleship
              //Type 5 = size 5 = Carrier
              if(list[i+1]==0)
              {
                System.out.printf("Water\n");
              }
              else if (list[i+1]==1)
              {
                System.out.printf("Destroyer !\n");
              }
              else if (list[i+1]==2)
              {
                System.out.printf("Submarine !\n");
              }
              else if (list[i+1]==3)
              {
                System.out.printf("Cruiser !\n");
              }
              else if (list[i+1]==4)
              {
                System.out.printf("Battleship !\n");
              }
              else if (list[i+1]==5)
              {
                System.out.printf("Carrier !\n");
              }
              else
              {
                System.out.printf("Invalid Target Hit List\n");
              }
            }
          }
        }
        else if(x==4)
        {
          ///Option 4 : Quit///
          s.close();
          System.exit(0);
        }
        else
        {
          System.out.printf("Wrong input ... try again !\n\n");
        }
      }
    }
    catch(TimeOutException e)
    {
      System.err.println("Client Failed to connect : Timeout Exception" + e);
    }
    catch(Exception e)
    {
      System.err.println("Client Failed to connect : " + e);
    }
  }
}
