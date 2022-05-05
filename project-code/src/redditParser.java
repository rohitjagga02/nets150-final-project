import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class redditParser {
    private String baseURL;
    private Document currentDoc;


    Map<String, String> countryLinks; // key is country, value is URL of country page
    private ArrayList<String> countryList;

    private LinkedList<String> capsPostLinks = new LinkedList<String>();
    private LinkedList<String> academicPostLinks = new LinkedList<String>();

    // for storing the upvotes and points
    private LinkedList<Integer> capsPostUpvoteRatios = new LinkedList<Integer>();
    private LinkedList<Integer> capsPostPoints = new LinkedList<Integer>();
    private LinkedList<Integer> academicPostUpvoteRatios = new LinkedList<Integer>();
    private LinkedList<Integer> academicPostPoints = new LinkedList<Integer>();

    // for extracting the posts' text
    private ArrayList<String> capsPostText = new ArrayList<String>();
    private ArrayList<String> academicPostText = new ArrayList<String>();

    // for the affiliation network graph
    private HashSet<String> capsUsers = new HashSet<String>();
    private HashSet<String> academicUsers = new HashSet<String>();
    private ArrayList<String> allUsers = new ArrayList<String>();
    private int[][] affiliationNetwork;

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

        getCapsPosts();
        getAcademicPosts();
    }

    /*
     * Get all the posts that refer to CAPS
     */
    public void getCapsPosts() {
        String nextPageLink = "https://old.reddit.com/r/UPenn/search?q=caps&restrict_sr=on&include_over_18=on&sort=relevance&t=all";
        boolean firstTime = true;
        while (nextPageLink != null) {
            try {
                this.currentDoc = Jsoup.connect(nextPageLink).get();
            } catch (IOException e) {
                return;
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
            Elements nextButton = this.currentDoc.getElementsByClass("nextprev");
            if (nextButton.size() == 0) {
                nextPageLink = null;
            } else {
                Elements buttonATag = nextButton.first().select("a");
                if (buttonATag.size() == 2) {
                    nextPageLink = buttonATag.get(buttonATag.size()-1).attr("href");
                } else if (firstTime) {
                    nextPageLink = buttonATag.get(0).attr("href");
                    firstTime = false;
                } else {
                    nextPageLink = null;
                }
            }
        }
    }

    /*
     * pass in true for CAPS posts, false for academic posts
     */
    public void getPostPointsAndUpvoteRatio(boolean caps) {
        LinkedList<String> postLinks = new LinkedList<String>();
        if (caps) {
            postLinks = this.capsPostLinks;
        } else {
            postLinks = this.academicPostLinks;
        }

        for (String postLink : postLinks) {
            try {
                this.currentDoc = Jsoup.connect(postLink).get();
            } catch (IOException e) {
                return;
            }

            Elements scoreTags = this.currentDoc.getElementsByClass("score");
            for (Element sTag : scoreTags) {
                if (sTag.text().contains("upvoted")) {
                    String[] splitByPoints = sTag.text().split(" point");
                    String[] splitByP = sTag.text().split("\\(");
                    if (caps) {
                        capsPostPoints.add(Integer.parseInt(splitByPoints[0]));
                        capsPostUpvoteRatios.add(Integer.parseInt(splitByP[1].split("%")[0]));
                    } else {
                        academicPostPoints.add(Integer.parseInt(splitByPoints[0]));
                        academicPostUpvoteRatios.add(Integer.parseInt(splitByP[1].split("%")[0]));
                    }
                }
            }
        }

        if (caps) {
            System.out.println("Caps Points: " + capsPostPoints);
            System.out.println("Caps Upvote Ratios: " + capsPostUpvoteRatios);
        } else {
            System.out.println("Academic Points: " + academicPostPoints);
            System.out.println("Academic Upvote Ratios: " + academicPostUpvoteRatios);
        }
    }

    /*
     * pass in true for CAPS posts, false for academic posts
     */
    public ArrayList<String> getPostsText(boolean caps) {
        LinkedList<String> postLinks = new LinkedList<String>();
        ArrayList<String> postText = new ArrayList<String>();
        if (caps) {
            postLinks = this.capsPostLinks;
        } else {
            postLinks = this.academicPostLinks;
        }
        for (String postLink : postLinks) {
            try {
                this.currentDoc = Jsoup.connect(postLink).get();
            } catch (IOException e) {
                System.out.println("failed to connect to post page");
                return null;
            }
            Element titleTag = this.currentDoc.select("a[data-event-action='title']").first();
            Element bodyDivTag = this.currentDoc.getElementsByClass("expando").first();
            if (titleTag == null) {
                postText.add("||SEPARATOR||");
                continue;
            }
            String currPostText = "||SEPARATOR||" + titleTag.text().replaceAll("\"\'", "");
            if (bodyDivTag != null) {
                Element formTag = bodyDivTag.select("form").first();
                if (formTag == null) { postText.add("||SEPARATOR||" + currPostText); continue; }
                Element div1Tag = formTag.select("div").first();
                if (div1Tag == null) { postText.add("||SEPARATOR||" + currPostText); continue; }
                Element div2Tag = div1Tag.select("div").first();
                if (div2Tag == null) { postText.add("||SEPARATOR||" + currPostText); continue; }
                Element pTag = div2Tag.select("p").first();
                if (pTag == null) { postText.add("||SEPARATOR||" + currPostText); continue; }
                currPostText = currPostText + " " + pTag.text().replaceAll("\"\'", "");
            }
            postText.add(currPostText);
        }
        return postText;
    }

    public void getAcademicPosts() {
        this.academicPostLinks = new LinkedList<String>();
        String nextPageLink = "https://old.reddit.com/r/UPenn/new/";
        while (nextPageLink != null) {
            try {
                this.currentDoc = Jsoup.connect(nextPageLink).get();
            } catch (IOException e) {
                return;
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

            Elements nextButton = this.currentDoc.getElementsByClass("next-button");
            if (nextButton.size() == 0) {
                nextPageLink = null;
            } else {
                Elements buttonATag = nextButton.first().select("a");
                nextPageLink = buttonATag.first().attr("href");
            }
        }
        System.out.println(this.academicPostLinks);
        System.out.println(this.academicPostLinks.size());
    }

    public void getUsersForGraph() {
        for (String postLink : this.capsPostLinks) {
            try {
                this.currentDoc = Jsoup.connect(postLink).get();
            } catch (IOException e) {
                return;
            }
//            Element aTag = this.currentDoc.select("a[href*=https://old.reddit.com/user/]").first();
            Element pTag = this.currentDoc.getElementsByClass("tagline").first();
            if (pTag == null) continue;
            Element aTag = pTag.select("a").first();
            if (aTag == null) continue;
//            if (aTag == null) {
//                System.out.println("caps user tag is null");
//                return;
//            }
            this.capsUsers.add(aTag.text());
            this.allUsers.add(aTag.text());

        }
        for (String postLink : this.academicPostLinks) {
            try {
                this.currentDoc = Jsoup.connect(postLink).get();
            } catch (IOException e) {
                return;
            }
            Element pTag = this.currentDoc.getElementsByClass("tagline").first();
            if (pTag == null) continue;
            Element aTag = pTag.select("a").first();
            if (aTag == null) continue;
//            if (aTag == null) {
//                System.out.println("academic user tag is null");
//                return;
//            }
            this.academicUsers.add(aTag.text());
            this.allUsers.add(aTag.text());
        }
        this.allUsers = new ArrayList<String>(new HashSet<String>(this.allUsers)); // removing duplicates across the users
        System.out.println("length of allUsers: " + this.allUsers.size());
        System.out.println(this.allUsers);
    }

    public void createAffiliationNetwork() {
        this.affiliationNetwork = new int[this.allUsers.size()][2];
        ArrayList<Integer> userCategories = new ArrayList<Integer>();
        int index = 0;
        for (String user : this.allUsers) {
            if (this.capsUsers.contains(user) && this.academicUsers.contains(user)) {
                this.affiliationNetwork[index][1] = 3; // user made both an academic and a CAPS post
                userCategories.add(3);
            } else if (this.academicUsers.contains(user)) {
                this.affiliationNetwork[index][1] = 2; // user only made an academic post
                userCategories.add(2);
            } else if (this.capsUsers.contains(user)) {
                this.affiliationNetwork[index][1] = 1; // user only made a CAPS post
                userCategories.add(1);
            }
            index++;
        }
        System.out.println(this.affiliationNetwork);
        System.out.println(userCategories);
    }

}
