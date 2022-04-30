public class hw3Main {
    public static void main(String[] args) {
//        ciaParser cparse = new ciaParser();

        // Comment out the questions you are not currently viewing, to improve the program's execution speed.
        // Change the question's parameters to evaluate that question for different regions/continents/colors/etc.
//        cparse.questionOne("red","white");
//        cparse.questionTwo("Afghanistan");
//        cparse.questionThree("Africa");
//        cparse.questionFour("Europe");
//        cparse.questionFive("South America");
//        cparse.questionSix("Caribbean");
//        cparse.questionSeven("D");
//        cparse.questionEight("Europe");

        redditParser rparse = new redditParser();

        rparse.getCapsPosts();
        System.out.println("end of caps posts");
        rparse.getAcademicPosts();
        System.out.println("end of academic posts");
    }
}
