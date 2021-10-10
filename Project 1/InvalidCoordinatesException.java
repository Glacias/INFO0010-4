/**
* Subclass of Exception that handles invalid coordinates exceptions.
*/
class InvalidCoordinatesException extends Exception
{

  private static final long serialVersionUID = 424242l;

  public InvalidCoordinatesException() { super(); }
  public InvalidCoordinatesException(String s) { super(s); }
}
