import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class redditParser {
    private String baseURL;
    private Document currentDoc;


    Map<String, String> countryLinks; // key is country, value is URL of country page
    private ArrayList<String> countryList;

    private LinkedList<String> capsPostLinks = new LinkedList();
    private LinkedList<String> academicPostLinks;

    /*
     * Constructor that initializes the base URL and loads
     * the document produced from that URL
     */
    public redditParser() {
        this.baseURL = "https://www.old.reddit.com/r/UPenn/";
        try {
            this.currentDoc = Jsoup.connect(this.baseURL).get();
            // System.out.println(this.currentDoc);
        } catch (IOException e) {
//            System.out.println("Could not connect to link");
        }
        this.countryList = new ArrayList<String>();
        this.countryLinks = new HashMap<String, String>();

        this.academicPostLinks = new LinkedList<String>();

        // setupCountries();
    }

    /*
     * Setup country map and list with URLs of country pages, called in constructor method
     */
    private void setupCountries() {
        String countryListURL = this.baseURL + "field/map-references/";
        System.out.println(countryListURL);    // check we have the right URL
        try {
            this.currentDoc = Jsoup.connect(countryListURL).get();
        }
        catch (IOException e) {
            System.out.println("Couldn't connect to map references page");
        }

        Elements countries = this.currentDoc.select("h2.h3");  //gets all <h3> country tags
        for (Element country : countries) {
            Elements aTag = country.select("a");  //links come in <a> tags typically
            Element a = aTag.get(0);
            String countryURL = a.attr("href");
            String countryName = a.text();

            this.countryLinks.put(countryName, "https://www.cia.gov" + countryURL);
            this.countryList.add(countryName);
        }
    }

    public void getCapsPosts() {
        String capsURL = "https://old.reddit.com/r/UPenn/search?q=caps&restrict_sr=on&include_over_18=on&sort=relevance&t=all";
        try {
            this.currentDoc = Jsoup.connect(capsURL).get();
            // System.out.println(this.currentDoc);
        } catch (IOException e) {
            //System.out.println("Could not connect to link");
        }
        Elements postElems = this.currentDoc.select("a[href*=https://old.reddit.com/r/UPenn/comments/]");
        for (Element post : postElems) {
            String postURL = post.attr("href");
            if (this.capsPostLinks.size() == 0) {
                this.capsPostLinks.add(postURL);
            } else if (!this.capsPostLinks.getLast().equals(postURL)) {
                this.capsPostLinks.add(postURL);
            }
        }

        System.out.println(this.capsPostLinks);
    }

    public void getAcademicPosts() {
        String url = "https://old.reddit.com/r/UPenn/new/";
        this.academicPostLinks = new LinkedList<String>();
        try {
            this.currentDoc = Jsoup.connect(url).get();
            // System.out.println(this.currentDoc);
        } catch (IOException e) {
            //System.out.println("Could not connect to link");
        }
        Elements aTags = this.currentDoc.select("span:contains(Academic/Career)");
        for (Element aTag : aTags) {
            Element postTag = aTag.parent().parent().select("a").first();
            String postLink = postTag.attr("href");
            String nextPostURL = "https://old.reddit.com" + postLink;
            if (this.academicPostLinks.size() == 0 || !nextPostURL.equals(this.academicPostLinks.getLast())) {
                this.academicPostLinks.add(nextPostURL);
            }
        }
        System.out.println(this.academicPostLinks);

    }


}
