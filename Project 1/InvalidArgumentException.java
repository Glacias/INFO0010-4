/**
* Subclass of Exception that handles invalid argument exceptions.
*/
class InvalidArgumentException extends Exception
{

  private static final long serialVersionUID = 414141l;

  public InvalidArgumentException() { super(); }
  public InvalidArgumentException(String s) { super(s); }
}
