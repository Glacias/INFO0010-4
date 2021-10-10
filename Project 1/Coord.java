import java.util.*;
/*
* This class is a representation of coordinates in the Battleship game.
* It has two variable : x and y both ranging from 0 to 9.
*/
class Coord
{
  private int x;
  private int y;

  //Classic construction (between (0,0) and (9,9))
  public Coord(int a, int b) throws InvalidCoordinatesException
  {
    if(coordExist(a,b))
    {
      this.x = a;
      this.y = b;
    }
    else
    {
      throw new InvalidCoordinatesException("("
      + String.valueOf(a) + "," + String.valueOf(b)
      + ") is not a valid tile. Choose between (0,0) and (9,9)");
    }
  }

  //Traditional construction (between A1 and J10)
  public Coord(char letter, int column) throws InvalidCoordinatesException
  {
    int line = (int) letter - (int) 'A';
    if(coordExist(line,column-1))
    {
      this.x = line;
      this.y = column-1;
    }
    else
    {
      throw new InvalidCoordinatesException("\""
      + letter + String.valueOf(column)
      + "\" is not a valid tile. Choose between A1 and J10");
    }
  }

  //Order construction (between 0 and 99)
  public Coord(int order) throws InvalidCoordinatesException
  {
    if(order >= 0 && order <= 99)
    {
      this.x = order/10;
      this.y = order%10;
    }
    else
    {
      throw new InvalidCoordinatesException("\""
      + String.valueOf(order)
      + "\" is not a valid order of tile. Argument must be : 0 <= order <= 99");
    }
  }

  //Get order of coordinates (between 0 and 99)
  public int getOrder()
  {
    return (x*10+y);
  }

  //Get traditional coordinates (between A1 and J10)
  public String getAlphaCoord()
  {
    char a = (char) (x + ((int) 'A' ));
    char b = (char)(y + 1 + '0');
    StringBuilder sb = new StringBuilder();
    sb.append(a);
    if(y+1 == 10)
    {
      sb.append('1');
      sb.append('0');
    }
    else
    {
      sb.append(b);
    }
    return sb.toString();
  }

  /*
  * Return -true if the coordinates are in the Batlleship grid
  *        -false if not
  */
  private boolean coordExist(int a, int b)
  {
    return (a >= 0 && a <= 9 && b >= 0 && b <= 9);
  }

  /*
  * Return -true if c is the same coordinate
  *        -false if not
  */
  public boolean equals(Coord c)
  {
    return (this.x == c.x && this.y == c.y);
  }

  /*
  * Return an array of coordiantes that are at a certain distance and exist
  * in the Batlleship grid (i.e. possible positions for the end of a ship)
  */
  public ArrayList<Coord> existingEndOfShip(int size) throws InvalidCoordinatesException
  {
    size--;
    ArrayList<Coord> listOfShipEnd = new ArrayList<Coord>();
    if(coordExist(this.x + size, this.y))
    {
      listOfShipEnd.add(new Coord(this.x + size, this.y));
    }
    if(coordExist(this.x - size, this.y))
    {
      listOfShipEnd.add(new Coord(this.x - size, this.y));
    }
    if(coordExist(this.x, this.y + size))
    {
      listOfShipEnd.add(new Coord(this.x, this.y + size));
    }
    if(coordExist(this.x, this.y - size))
    {
      listOfShipEnd.add(new Coord(this.x, this.y - size));
    }
    return listOfShipEnd;
  }

  /*
  * Return the list of coordinates that are between the object and
  * the coordinate c
  */
  public ArrayList<Coord> listOfCoordBetweenUs(Coord c) throws InvalidCoordinatesException
  {
    ArrayList<Coord> v = new ArrayList<Coord>();

    if(this.x == c.x)
    {
      int min = Math.min(this.y,c.y);
      int max = Math.max(this.y,c.y);

      //Avoid first coord
      min++;
      //If same coord don't get stuck
      if(min == max+1)
      {
        min--;
      }

      while(min!=max)
      {
        v.add(new Coord(this.x,min));
        min++;
      }
    }
    else if (this.y == c.y)
    {
      int min = Math.min(this.x,c.x);
      int max = Math.max(this.x,c.x);

      //Avoid first coord
      min++;
      //If same coord don't get stuck
      if(min == max+1)
      {
        min--;
      }

      while(min!=max)
      {
        v.add(new Coord(min,this.y));
        min++;
      }
    }
    else
    {
      throw new InvalidCoordinatesException("listOfCoordBetweenUs : Coordinates not aligned");
    }
    return v;
  }
}
