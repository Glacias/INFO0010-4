import java.util.*;

/*
* This class is a representation of a single player Battleship game.
* It has 5 variables : - A list of boats
*                      - A list of coordinates which have been fired at (may contain doubles)
*                      - A list of types of ships that have been hit or 0 for
*                        when hitting water (may contain doubles)
*                      - The number of hits (without doubles)
*                      - The number of shots (without doubles)
*
* Doubles happens when the user shoots at the same place twice (or more).
* It is dealt with by storing the shot (i.e. request "1 2" will show doubles)
* but not incrementing the number of hits and/or of shots.
*/
class Game
{
  private Boat[] ships;
  private ArrayList<Coord> shotsFired;
  private ArrayList<Integer> typeOfShipHit;
  private int nbrHits;
  private int nbrShots;

  public Game() throws InvalidCoordinatesException, InvalidArgumentException
  {
    ships = new Boat[5];
    nbrHits = 0;
    nbrShots = 0;
    shotsFired = new ArrayList<Coord>();
    typeOfShipHit = new ArrayList<Integer>();
    ArrayList<Coord> listOfBoatCoord = new ArrayList<Coord>();
    for(int i = 0; i < 5; i++)
    {
      ships[i] = placeBoat(i+1, listOfBoatCoord);
      listOfBoatCoord.addAll(listOfBoatCoord.size(), ships[i].position());
    }
  }

  /*
  * Return a boat that was randomly put into the grid without conflicting with
  * an other ship.
  * It has 2 arguments : - The type of the boat
  *                      - The list of coordinates that are already taken
  */
  private Boat placeBoat(int type, ArrayList<Coord> list) throws InvalidCoordinatesException, InvalidArgumentException
  {
    /*
    * Reminder :
    * Type 1 = size 2 = Destroyer
    * Type 2 = size 3 = Submarine
    * Type 3 = size 3 = Cruiser
    * Type 4 = size 4 = Battleship
    * Type 5 = size 5 = Carrier
    */

    //Find size
    int size = type;
    if(type == 1 || type == 2)
    {
      size++;
    }

    Coord randomTile;
    ArrayList<Coord> listOfShipEnd;
    ArrayList<Coord> shipPositions;
    while (true)
    {
      //Generate a random coordinate
      randomTile = new Coord((int) (Math.random()*100));

      //Verify that it is not in the list of already taken coordinates
      if(!IsIn(randomTile, list))
      {
        //Generate the list of possible coordinates for the end of the ship
        listOfShipEnd = randomTile.existingEndOfShip(size);
        for(int i = 0; i < listOfShipEnd.size(); i++)
        {
          //Take a possible end at random from the list
          Coord randomEndShip = listOfShipEnd.get((int) (Math.random()*listOfShipEnd.size()));

          //Verify that it is not in the list of already taken coordinates
          if(!IsIn(randomEndShip, list))
          {
            //Generate the coordinates between the head and tail of the ship
            shipPositions = randomTile.listOfCoordBetweenUs(randomEndShip);

            //Verify that they are not in the list of already taken coordinates
            int sucess = 0;
            for(int j = 0; j < shipPositions.size(); j++)
            {
              if(!IsIn(shipPositions.get(j), list))
              {
                sucess++;
              }
              else { break; }
            }

            //If every step succeded, create the boat
            if(sucess == shipPositions.size())
            {
              shipPositions.add(0, randomTile);
              shipPositions.add(randomEndShip);
              return new Boat(shipPositions, type);
            }
          }
          //Remove the possible end of ship from the list
          listOfShipEnd.remove(0);
        }
      }
    }
  }

  /*
  * Return -true if the coordinate c is in list of coordinates
  *        -false if not
  */
  private boolean IsIn(Coord c, ArrayList<Coord> list)
  {
    for(int i = 0; i < list.size(); i++)
    {
      if(list.get(i).equals(c))
      {
        return true;
      }
    }
    return false;
  }

  //Return the list of shots fired
  public ArrayList<Coord> listShotsFired()
  {
    return new ArrayList<Coord>(shotsFired);
  }

  //Return the list of types of ships (or 0 for water)
  public ArrayList<Integer> listTypeOfShipHit()
  {
    return new ArrayList<Integer>(typeOfShipHit);
  }

  //Return the number of shots
  public int nbrShots()
  {
    return this.nbrShots;
  }

  //Return the number of hits
  public int nbrHits()
  {
    return this.nbrHits;
  }

  /*
  * Register the shot (doubles are stored but not counted) and return the type
  * of the ship that has been hit or 0 if the shot landed in water
  */
  public int fire(Coord c)
  {
    //Check that position was not already fired at to count a shot
    if(!IsIn(c, shotsFired))
    {
      nbrShots++;
    }
    //Add position to the list of shots
    shotsFired.add(c);

    //Add 0 to the list of types
    typeOfShipHit.add(0);

    for(int i=0; i<5;i++)
    {
      //If a ship is hit
      if(ships[i].IsIn(c))
      {
        //Check that position was not already fired at to count a hit
        shotsFired.remove(shotsFired.size()-1);
        if(!IsIn(c, shotsFired))
        {
          nbrHits++;
        }
        shotsFired.add(c);

        //Replace 0 by the type of the ship that was hit
        typeOfShipHit.remove(typeOfShipHit.size()-1);
        typeOfShipHit.add(ships[i].type());
        return ships[i].type();
      }
    }
    return 0;
  }
}
