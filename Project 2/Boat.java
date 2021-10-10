import java.util.*;
/*
* This class is a representation of a boat in the Battleship game.
* It has 2 variables : - A list of coordinates which determine the boat position
*                      - The type of ship ranging from 1 to 5
* Type 1 = size 2 = Destroyer
* Type 2 = size 3 = Submarine
* Type 3 = size 3 = Cruiser
* Type 4 = size 4 = Battleship
* Type 5 = size 5 = Carrier
*/
class Boat
{
  private ArrayList<Coord> position;
  private int type;

  public Boat(ArrayList<Coord> pos, int type) throws InvalidArgumentException
  {
    if(pos.size()>0 && type >=1 && type <= 5)
    {
      this.position = new ArrayList<Coord>(pos);
      this.type = type;
    }
    else
    {
      throw new InvalidArgumentException("Invalid Ship Creation");
    }
  }

  /*
  * Return -true if the boat is in coordinate c
  *        -false if not
  */
  public boolean IsIn(Coord c)
  {
    for(int i = 0; i < position.size(); i++)
    {
      if(position.get(i).equals(c))
      {
        return true;
      }
    }
    return false;
  }

  //Return the list of coordinates
  public ArrayList<Coord> position()
  {
    return new ArrayList<Coord>(position);
  }
  //Return the type
  public int type()
  {
    return type;
  }
}
