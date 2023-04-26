package restapi;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
//import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
// import org.json.JSONObject;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;


class ServerThread extends Thread
{
    protected BufferedReader is;
    protected PrintWriter os;
    protected Socket s;
    private String urlstring;
    private String line = new String();
    private String lines = new String();
    String output="";
    String result="";
    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s)
    {
        this.s = s;
    }

    public static String Connection (String surl) throws IOException { //çalışıyor
        URL url = new URL(surl); 
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        String inline = "";
        Scanner scanner = new Scanner(url.openStream());

        while (scanner.hasNext()) {
            inline += scanner.nextLine();
         }

         scanner.close();  
        System.out.println(inline);
        return inline;

        

    }
    public static String newURL (ArrayList<String> idarr) {
        String ids="";
        
        for (int i = 0; i < idarr.size(); i++) {
            if (i==(idarr.size())-1) {
                ids+=idarr.get(i);
            }
            else {
                ids+=idarr.get(i)+",";
            }

        }
        

         String newurl="https://api.coingecko.com/api/v3/simple/price?ids="+ids+"&vs_currencies=try";
        return newurl;
     }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        try
        {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());

        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        try
        {
            line = is.readLine();
            while (line.compareTo("QUIT") != 0)
            {
		        String sub=line.substring(0,1);
                if ((sub.compareTo("0"))==0) {
                    urlstring= "https://api.coingecko.com/api/v3/coins/list";
                    
                    String in=Connection(urlstring);
                    JSONArray jsonObject = new JSONArray(in);
                    for (int i = 0; i < jsonObject.length(); i++)
                    {
                        String id = jsonObject.getJSONObject(i).getString("id");
                        
                        String name = jsonObject.getJSONObject(i).getString("name");
                        output+=(id + "\t" + name+" ");
                        
                        
                    
                        
                    }
                    os.println(output);
                    os.flush();
                }
                else if(sub.compareTo("1")==0) { //çalışıyor
                    
                    //Scanner sc=new Scanner(System.in);
                    //os.println("enter coinnames ");
                    //os.flush();
                    String[] p=line.split(":");


                    String coins= p[1]; //"zyro,verso";
                    
                    
                    //make the inputs seperate lists;
                    ArrayList<String> idlist = new ArrayList<String>(Stream.of(coins.split(",")).collect(Collectors.toList())); //user input to arraylist
                    
                    
                    urlstring=newURL(idlist);
                    String inl=Connection(urlstring); //taking inline from the method
                    JSONObject obj=new JSONObject(inl);
                    JSONObject curval;
                    String coinname;
                    String resp;
                    
                    for (int i = 0; i < obj.length(); i++) {
                        coinname=idlist.get(i);
                        curval=obj.getJSONObject(coinname);
                        resp=curval.toString();
                        result+=(coinname+": "+resp+" ");
                        System.out.println(result);
                       
                        
                    }
                    os.println(result);
                    os.flush();
                }
                
                
                line = is.readLine();
            } 
        }
            catch (IOException e)
            {
                line = this.getName(); //reused String line for getting thread name
                System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
            }
            catch (NullPointerException e)
            {
                line = this.getName(); //reused String line for getting thread name
                System.err.println("Server Thread. Run.Client " + line + " Closed");
            } finally
            {
                try
                {
                    System.out.println("Closing the connection");
                    if (is != null)
                    {
                        is.close();
                        System.err.println(" Socket Input Stream Closed");
                    }
                    
                    if (os != null)
                    {
                    os.close();
                    System.err.println("Socket Out Closed");
                }
                if (s != null)
                {
                    s.close();
                    System.err.println("Socket Closed");
                }

            }
            catch (IOException ie)
            {
                System.err.println("Socket Close Error");
            }
        }//end finally
    }
}
