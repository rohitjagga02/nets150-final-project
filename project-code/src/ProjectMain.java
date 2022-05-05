import java.util.ArrayList;

public class ProjectMain {
    public static void main(String[] args) {
        System.out.println("*** Getting Caps and Academic Post Links ***");
        redditParser rparse = new redditParser();
        System.out.println("*** Getting Caps Post Information... ***");
        rparse.getPostPointsAndUpvoteRatio(true);
        System.out.println("*** Getting Academic Post Information... ***");
        rparse.getPostPointsAndUpvoteRatio(false);


        ArrayList<String> capsText = rparse.getPostsText(true);
        System.out.println(capsText);
        System.out.println(capsText.size());

        ArrayList<String> academicText = rparse.getPostsText(false);
        System.out.println(academicText.size());

        rparse.getUsersForGraph();
        rparse.createAffiliationNetwork();

    }
}
