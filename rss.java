import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.NodeList;
import java.io.File;

public class rss {
    public static File file = new File("data.txt");

    public static void main(String[] args) throws Exception {
        Menu();
    }

    public static void Menu() throws Exception {
        while (true) {
            System.out.println("[1] add url\n[2] remove url\n[3] update url\n[4] exit");

            Scanner scanner = new Scanner(System.in);
            int MenuInput = scanner.nextInt();
            if(MenuInput == 1){
                if(checkConnection()) {
                    System.out.println("Type Url:");
                    String url = scanner.next();
                    AddUrl(url);
                }
            }
            else if (MenuInput == 2){
                if(checkConnection()) {
                    System.out.println("Type Url:");
                    String url = scanner.next();
                    RemoveUrl(url);
                }
            }
            else if (MenuInput == 3){
                if(checkConnection()) {
                    ShowUpdate();
                }
            }
            else if(MenuInput == 4){
                break;
            }
            else{
                System.out.println("Invalid input!");
                Menu();
                break;
            }
        }
    }

    public static void AddUrl(String url) throws Exception {

        // check if data.txt is existed
        if(!file.exists()){
            file.createNewFile();
        }

        // check if Url is repeated
        Scanner scanner = new Scanner(file);
        boolean UrlExist=false;
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineInfo = line.split(";");
            if(lineInfo[1].equals(url)){
                System.out.println("This url has already exist!");
                UrlExist=true;
                break;
            }
        }
        scanner.close();

        // add Url to data.txt
        try {
            if(UrlExist == false) {
                PrintStream printStream = new PrintStream(new FileOutputStream(file, true));
                printStream.append(extractPageTitle(fetchPageSource(url))).append(";");
                printStream.append(url).append(";");
                printStream.append(extractRssUrl(url)).append(";").append("\n");
                printStream.close();
            }
        }
        catch (Exception e){
            System.out.println("Invalid Url!");
        }


    }

    public static void RemoveUrl(String url) throws Exception {

        // check if data.txt is existed
        if(!file.exists()){
            file.createNewFile();
        }

        // copy content of file in a string
        String content = GetContent(file);
        Scanner scanner = new Scanner(content);

        // empty file
        PrintStream printStream = new PrintStream(new FileOutputStream(file, false));

        // check if Url is existed
        boolean UrlExist=false;
        while (scanner.hasNextLine()){
            // write in file if Url is not same with input Url for remove
            String line = scanner.nextLine();
            String[] lineInfo = line.split(";");
            if(!(lineInfo[1].equals(url))){
                    AddUrl(lineInfo[1]);
            }
            else{
                UrlExist=true;
            }
        }
        scanner.close();

        if(UrlExist == false){
            System.out.println("This url doesn't exist!");
        }

    }

    public static void ShowUpdate() throws IOException {

        // check if data.txt is existed
        if(!file.exists()){
            file.createNewFile();
        }

        // print Urls existed in file
        Scanner filescanner = new Scanner(file);
        int number=1;
        while (filescanner.hasNextLine()){
            String line = filescanner.nextLine();
            String[] lineInfo = line.split(";");
            System.out.println("["+number+"]"+" "+lineInfo[0]);
            number++;
        }

        // check if file is empty or not
        if(number != 1) {
            System.out.println("press -1 to return to menu");
        }
        else {
            System.out.println("There isn't any Url");
            return;
        }

        // get input for this menu
        Scanner input = new Scanner(System.in);
        int RssNumber = input.nextInt();
        if(RssNumber == -1){
            return;
        }
        else if (RssNumber <= 0 || RssNumber>number){
            System.out.println("Invalid input!");
            return;
        }

        // if input is valid find the Url and print contents
        filescanner.close();
        Scanner filescanner2 = new Scanner(file);
        for(int i=1; i<number; i++){
            if(i != RssNumber){
                filescanner2.nextLine();
            }
            else{
                String line = filescanner2.nextLine();
                String[] lineInfo = line.split(";");
                retrieveRssContent(lineInfo[2]);
            }
        }
    }

    public static boolean checkConnection(){
        try {
            URL url = new URL("https://www.geeksforgeeks.org/");
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        }
        catch (Exception e) {
            System.out.println("Internet Not Connected");
            return false;
        }
    }

    public static void retrieveRssContent(String rssUrl) {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");
            for (int i = 0; i < 5; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
        }

    public static String extractPageTitle(String html) {
        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
            }
        catch (Exception e)
        {
            return "Error: no title tag found in page source!";
            }
        }

    public static String extractRssUrl(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }

    public static String GetContent(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuilder content= new StringBuilder();
        while (scanner.hasNextLine()){
            content.append(STR."\{scanner.nextLine()}\n");
        }
        return content.toString();
    }
}

