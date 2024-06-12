import org.json.*;

import java.io.*;

public class main2 {
    public final static String s = "src/test.json";

        public static void main(String[] args) throws IOException {

           JSONObject j = new JSONObject();

           j.put("deine", "mutter");
           j.put("zahl",1);
           j.put("zahl2",2);

           Writer w = new FileWriter(s);

           j.write(w);

           w.flush();

           try (FileReader reader = new FileReader(s)){
               JSONObject f = new JSONObject();

               f.put("name", "basti");

               String t = f.toString();

           }

        }
    }

