import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

//this class is used to write results into the output file
public class writeLog
{
  public static void method(String file, String conent) {
	   BufferedWriter out = null;
	   try {
	   out = new BufferedWriter(new OutputStreamWriter(
	   new FileOutputStream(file, true)));
	   out.write(conent);
	   } catch (Exception e) {
	   e.printStackTrace();
	   } finally {
	   try {
	   out.close();
	   } catch (IOException e) {
	   e.printStackTrace();
	   }
	 }
	}
}