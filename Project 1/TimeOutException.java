/**
* Subclass of Exception that handles time out exceptions.
*/
class TimeOutException extends Exception
{

  private static final long serialVersionUID = 404040l;

  public TimeOutException() { super(); }
  public TimeOutException(String s) { super(s); }
}
