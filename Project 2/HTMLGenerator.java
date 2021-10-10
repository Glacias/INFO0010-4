import java.net.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.nio.charset.StandardCharsets;

public class HTMLGenerator
{
  private String htmlCode;
  private String waterImage;
  private String missImage;
  private String hitImage;

  public HTMLGenerator()
  {
    waterImage = imageGeneration("water.png");
    missImage = imageGeneration("miss.png");
    hitImage = imageGeneration("hit.png");
  }

  public void generateEndPage(Boolean win)
  {
    htmlCode = head();
    if(win)
    {
      htmlCode += "<h1>You win</h1>\n";
    }
    else
    {
      htmlCode += "<h1>You lose</h1>\n";
    }
    htmlCode += "<div style='display: flex; justify-content: center; align-items: center;'>\n";
    htmlCode += "<div style='display: flex; justify-content: center; align-items: center;padding:20px'>\n";
    htmlCode += "<form action='/play.html'>\n";
    htmlCode += "<input type='submit' value='Start a new game' id='newGame'/>\n";
    htmlCode += "</form>\n </div>\n";
    htmlCode += "<div style='display: flex; justify-content: center; align-items: center;padding:20px'>\n";
    htmlCode += "<form action='/halloffame.html'>\n";
    htmlCode += "<input type='submit' value='Go to the Hall Of Fame' id='hallOfFame'/>\n";
    htmlCode += "</form>\n </div>\n";
    htmlCode += "</div>\n</body>\n</html>\n";
  }

  public void generate(Game g)
  {
    htmlCode = head();
    htmlCode += "<h1>Battleship</h1>\n";
    htmlCode += "<div style='display: flex; justify-content: center; align-items: center;'>\n";
    htmlCode += grid();
    htmlCode += "<div style='justify-content: center; align-items: center;padding:20px'>\n";
    htmlCode += id(g);
    htmlCode += "<br>\n";
    htmlCode += "<br>\n";
    htmlCode += score();
    htmlCode += "<br>\n";
    htmlCode += "<br>\n";
    htmlCode += noscript();
    htmlCode += "</div>\n";
    htmlCode += "</div>\n";
    htmlCode += instructions();
    htmlCode += script();
    htmlCode += "</body>\n</html>\n";
  }

  public void generateHall(ArrayList<Game> hallOfFame)
  {
    synchronized(hallOfFame)
    {
      htmlCode = head();
      htmlCode += "<h1>Battleship : Hall Of Fame</h1>\n";

      //Button
      htmlCode += "<div style='display: flex; justify-content: center; align-items: center;padding:20px'>\n";
      htmlCode += "<form action='/play.html'>\n";
      htmlCode += "<input type='submit' value='Go back to play' id='newGame'/>\n";
      htmlCode += "</form>\n </div>\n";
      htmlCode += "<div style='display: flex; justify-content: center; align-items: center;'>\n";

      //Table
      htmlCode += "<table>\n<tr>\n<th>Rank</th>\n<th>Cookie</th>\n<th>Score</th>\n</tr>\n";
      for(int i = 0; i < hallOfFame.size(); i++)
      {
        htmlCode += "<tr>\n";
        htmlCode += "<td>"+ (i+1) +"</td>\n";
        htmlCode += "<td>"+ hallOfFame.get(i).getCookie() +"</td>\n";
        if(hallOfFame.get(i).nbrShots()>70)
          htmlCode += "<td>"+ "Lose" +"</td>\n";
        else
          htmlCode += "<td>"+ hallOfFame.get(i).nbrShots() +"</td>\n";
        htmlCode += "</tr>\n";
      }
      htmlCode += "</table>\n";
      htmlCode += "</div>\n";
      htmlCode += "</body>\n</html>";
    }
  }

  public void generateCurrentGame(Game g)
  {
    synchronized(g)
    {
      htmlCode = head();
      htmlCode += "<h1>Battleship</h1>\n";
      htmlCode += "<div style='display: flex; justify-content: center; align-items: center;'>\n";
      htmlCode += grid(g);
      htmlCode += "<div style='justify-content: center; align-items: center;padding:20px'>\n";
      htmlCode += id(g);
      htmlCode += "<br>\n";
      htmlCode += "<br>\n";
      htmlCode += score(g);
      htmlCode += "<br>\n";
      htmlCode += "<br>\n";
      htmlCode += noscript();
      htmlCode += "</div>\n";
      htmlCode += "</div>\n";
      htmlCode += instructions();
      htmlCode += script();
      htmlCode += "</body>\n</html>";
    }
  }

  public String id(Game bs)
  {
    return "<fieldset>\n<legend>ID</legend>\n<div id='cookie'>\n "+ bs.getCookie() +" \n</div>\n</fieldset>\n";
  }

  public String html()
  {
    return htmlCode;
  }

  public String head()
  {
    return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset='utf-8'  />\n<title>Battleship</title>\n <link rel='icon' type='image/png' href=" + "data:image/png;base64,"+imageToBase64("water.png") + ">\n</head>\n<style>" + inFile("default.css") + "</style>\n<body>\n";
  }

  public String instructions()
  {
    return "<div class='box' style='display: flex; justify-content: center; align-items: center;padding:20px'>\n<fieldset>\n<legend>Instructions</legend>\nClick on (or select) a tile to fire at that position.\n<br>\nClick <a href='halloffame.html'>here</a> to access the Hall Of Fame.\n<br>\nYou win if you manage to sink all the ships with 70 tries or less.\n</fieldset>\n</div>\n";
  }

  public String score()
  {
    return "<fieldset>\n<legend>Score</legend>\n<div id='tries'>\nNumber of tries : &nbsp 0\n</div>\n<br>\n<div id='ship' >\nNumber of ships left : &nbsp 5\n</div>\n</fieldset>\n";
  }

  public String score(Game bs)
  {
    return "<fieldset>\n<legend>Score</legend>\n<div id='tries'>\nNumber of tries : &nbsp "+ bs.nbrShots() +"\n</div>\n<br>\n<div id='ship' >\nNumber of ships left : &nbsp "+ bs.nbrShipAlive() +"\n</div>\n</fieldset>\n";
  }

  public String imageGeneration(String path)
  {
    return "<img src='data:image/png;base64,"+imageToBase64(path)+"'/ style='width:40px;height:40px;'>";
  }

  public static String imageToBase64(String path)
  {
    String s = "";
    try
    {
      File file = new File(path);
      FileInputStream input = new FileInputStream(file);
      byte data[] = new byte[(int) file.length()];
      input.read(data);
      s = Base64.getEncoder().encodeToString(data);
    } catch (Exception e)
    {
      System.err.println("Unable to find image to encode (base64) : "+e);
    }
    return s;
  }

  public String grid()
  {
    String s = "<div class='grid-container'>\n";

    //First Cell
    s += "<div class='grid-item-info'>" + "</div>\n";
    //First Row
    for(int i=1; i<=10;i++)
    {
      s += "<div class='grid-item-info'>" + i + "</div>\n";
    }
    //Rest of the grid
    for(int i=1; i<=10;i++)
    {
      for(int j=0; j<=10;j++)
      {
        if(j==0)
        {
          s += "<div class='grid-item-info'>" + Character.toString ((char) (i+64)) + "</div>\n";
        }
        else
        {
          s += "<div class='grid-item-cell' id='cell" + ((i-1)*10+j) + "' onclick='display(this.id)' >" + waterImage + "</div>\n";
        }
      }
    }

    s += "</div>\n";
    return s;
  }

  public String grid(Game bs)
  {
    String s = "<div class='grid-container'>\n";

    //First Cell
    s += "<div class='grid-item-info'>" + "</div>\n";
    //First Row
    for(int i=1; i<=10;i++)
    {
      s += "<div class='grid-item-info'>" + i + "</div>\n";
    }
    //Rest of the grid
    for(int i=1; i<=10;i++)
    {
      for(int j=0; j<=10;j++)
      {
        if(j==0)
        {
          s += "<div class='grid-item-info'>" + Character.toString ((char) (i+64)) + "</div>\n";
        }
        else
        {
          try
          {
            Coord c = new Coord(((i-1)*10+j-1));
            int k = bs.checkStateOfTile(c);
            if(k == -1)
            {
              s += "<div class='grid-item-cell' id='cell" + ((i-1)*10+j) + "' onclick='display(this.id)' >" + waterImage + "</div>\n";
            }
            else if(k==0)
            {
              s += "<div class='grid-item-cell' id='cell" + ((i-1)*10+j) + "' onclick='display(this.id)' >" +missImage + "</div>\n";
            }
            else
            {
              s += "<div class='grid-item-cell' id='cell" + ((i-1)*10+j) + "' onclick='display(this.id)' >" + hitImage + "</div>\n";
            }
          }
          catch(Exception e) { }
        }
      }
    }

    s += "</div>\n";
    return s;
  }

  public String script()
  {
    String s = "<script>\n";

    s += "var hitImage = \"" + hitImage + "\";\n";
    s += "var missImage = \"" + missImage + "\";\n";

    s += inFile("script.js");

    s += "</script>\n";
    return s;
  }

  public String noscript()
  {
    String s = "<noscript>\n";

    s += inFile("noscript.html");

    s += "</noscript>\n";
    return s;
  }

  public String inFile(String path)
  {
    String s = "";
    String l;
		try
		{
			BufferedReader buffer = new BufferedReader(new FileReader(new File(path)));
			while((l = buffer.readLine()) != null)
				s += l + "\n";
		}
		catch(IOException e) { }
		return s;
  }

  public String chunckEncoding(String html)
  {
    String s = "";
    int maxSize = 128;
    while(html.length() > maxSize)
    {
      s += Integer.toHexString(128) + "\r\n";
      s += html.substring(0,128);
      html = html.substring(128);
    }

    s += Integer.toHexString(html.length()) + "\r\n";
    s += html;
    s += Integer.toHexString(0) + "\r\n\r\n";
    return s;
  }

  public void chunckEncoding(byte[] byteArray, OutputStream out) throws IOException
	{
		byte[] bytedStream = new byte[128];
		String s = "";
		int maxSize = 128;
    int i = 0;
		while(byteArray.length - 128*i > maxSize)
		{
			s = Integer.toHexString(128) + "\r\n";
			out.write(s.getBytes());
			System.arraycopy(byteArray, 128*i, bytedStream, 0, 128);
			out.write(bytedStream);
			s = "\r\n";
			out.write(s.getBytes());
			i++;
		}

		s = Integer.toHexString(byteArray.length - 128*i) + "\r\n";
		out.write(s.getBytes());
		System.arraycopy(byteArray, 128*i, bytedStream, 0, byteArray.length - 128*i);
		out.write(bytedStream,0,byteArray.length - 128*i);
		s = "\r\n";
		out.write(s.getBytes());
		s = Integer.toHexString(0) + "\r\n\r\n";
		out.write(s.getBytes());
	}

  public byte[] compress(String data) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
    GZIPOutputStream gzip = new GZIPOutputStream(bos);
    gzip.write(data.getBytes(StandardCharsets.UTF_8));
    gzip.close();
    byte[] compressed = bos.toByteArray();
    bos.close();
    return compressed;
  }
}
