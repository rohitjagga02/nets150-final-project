import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class ciaParser {
    private String baseURL;
    private Document currentDoc;

    Map<String, String> countryLinks; // key is country, value is URL of country page
    private ArrayList<String> countryList;

    /*
     * Constructor that initializes the base URL and loads
     * the document produced from that URL
     */
    public ciaParser() {
        this.baseURL = "https://www.cia.gov/the-world-factbook/";
        try {
            this.currentDoc = Jsoup.connect(this.baseURL).get();
            // System.out.println(this.currentDoc);
        } catch (IOException e) {
            //System.out.println("Could not connect to link");
        }
        this.countryList = new ArrayList<String>();
        this.countryLinks = new HashMap<String, String>();

        setupCountries();
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


    /*
     * Prints out a list of all countries with flags containing both color1 and color2
     */
    public void questionOne(String color1, String color2) {
        ArrayList<String> validCountries = new ArrayList<String>();
        for (String country : this.countryList) {
            String countryURL = this.countryLinks.get(country) + "/flag";
            //System.out.println(countryURL);
            try {
                this.currentDoc = Jsoup.connect(countryURL).get();
            }
            catch (IOException e) {
                System.out.println("Couldn't connect to country flag page");
                System.out.println(country);
            }
            //System.out.println("About to get divTag of country" + country);
            Element divTag = this.currentDoc.select("div.mb0.image-detail-block-caption").first();
            Element pTag = divTag.select("p").first();

            if (pTag.text() != null && checkIfWordsInText(pTag.text(), color1, color2)) {
                validCountries.add(country);
            }
        }
        System.out.println(validCountries);
    }
    /*
     * Helper method for question 1
     */
    private boolean checkIfWordsInText(String text, String word1, String word2) {
        String lowerCaseText = text.toLowerCase();
        if (!lowerCaseText.contains(word1.toLowerCase())) {
            return false;
        }
        if (!lowerCaseText.contains(word2.toLowerCase())) {
            return false;
        }
        return true;
    }


    /*
     * Gets the lowest point in a region
     */
    public void questionTwo(String region) {
        String countryGeoURL = this.countryLinks.get(region) + "/#geography";
        try {
            this.currentDoc = Jsoup.connect(countryGeoURL).get();
        }
        catch (IOException e) {
            System.out.println("Couldn't connect to country geo page");
            System.out.println(region);
        }
        Element strongTag = this.currentDoc.select("strong:contains(lowest point:)").get(0);
        Element pTag = strongTag.parent();

        //System.out.println(pTag.text());
        boolean isSecondNumber = false;
        for (String chunk : pTag.text().replaceAll(",","").split(" ")) {
            //System.out.println(chunk);
            try {
                Integer lowestPoint = Integer.parseInt(chunk);
                if (isSecondNumber) {
                    System.out.println("Lowest point: " + lowestPoint + " m");
                    break;
                } else {
                    isSecondNumber = true;
                }
            } catch (NumberFormatException e) {

            }
        }
    }
    private ArrayList<String> getCountriesInContinent(String continent) {
        ArrayList<String> continentCountries = new ArrayList<String>();
        String countryListURL = this.baseURL + "field/map-references/";
        try {
            this.currentDoc = Jsoup.connect(countryListURL).get();
        }
        catch (IOException e) {
            System.out.println("Couldn't connect to map references page");
        }
        Elements countries = this.currentDoc.select("h2.h3");  //gets all <h3> country tags
        for (Element country : countries) {
            Element aTag = country.select("a").first();
            String countryName = aTag.text();
            Element pTag = country.parent().select("p").first();
            if (continent.equals(pTag.text())) {
                //System.out.println(countryName);
                continentCountries.add(countryName);
            }
        }
        return continentCountries;
    }


    /*
     * Prints the largest country in continent in terms of electricity production
     */
    public void questionThree(String continent) {
        ArrayList<String> continentCountries = getCountriesInContinent(continent);

        double highestEnergyProduction = 0;
        String ansCountry = null;
        for (String country : continentCountries) {
            double electricityProd = getElectricityProduction(country);
            if (electricityProd > highestEnergyProduction) {
                ansCountry = country;
                highestEnergyProduction = electricityProd;
            }
        }
        System.out.println("This continent's largest country in terms of electricity production: " + ansCountry);
    }
    private double getElectricityProduction(String countryName) {
        String countryGeoURL = this.countryLinks.get(countryName) + "/#geography";
        try {
            this.currentDoc = Jsoup.connect(countryGeoURL).get();
        } catch (IOException e) {
            System.out.println("Couldn't connect to country geo page");
            System.out.println(countryName);
        }
        Element divTag = this.currentDoc.getElementById("energy");
        Element pTag = divTag.select("div").get(2).select("p").first();
        try {
            Double electricityProd = Double.parseDouble(pTag.text().split(" ")[0]);
            //System.out.println(countryName + electricityProd);
            return electricityProd;
        } catch (NumberFormatException e) {
            //System.out.println("exception, country " + countryName);
        }
        return 0;
    }


    /*
     * Prints country in continent with the largest coastline to land area ratio
     */
    public void questionFour(String continent) {
        ArrayList<String> continentCountries = getCountriesInContinent(continent);
        double highestRatio = 0;
        String highestRatioCountry = null;
        for (String country : continentCountries) {
            int coastlineLength = getCoastLineLength(country);
            int landArea = getLandArea(country);
            double currRatio = ((double) coastlineLength) / landArea;
            if (currRatio > highestRatio) {
                highestRatio = currRatio;
                highestRatioCountry = country;
            }
        }
        System.out.println("Country with highest coastline to land area ratio is: " + highestRatioCountry);
    }
    private int getCoastLineLength(String countryName) {
        String countryGeoURL = this.countryLinks.get(countryName) + "/#geography";
        try {
            this.currentDoc = Jsoup.connect(countryGeoURL).get();
        } catch (IOException e) {
            System.out.println("Couldn't connect to country geo page");
            System.out.println(countryName);
        }
        Element divTag = this.currentDoc.getElementById("geography");
        Element pTag = divTag.select("div").get(12).select("p").first();
        try {
            int coastLineLength = Integer.parseInt(pTag.text().replaceAll(","," ").split(" ")[0]);
            //System.out.println(countryName + coastLineLength);
            return coastLineLength;
        } catch (NumberFormatException e) {
            //System.out.println("exception, country " + countryName);
        }
        return 0;
    }
    private int getLandArea(String countryName) {
        String countryGeoURL = this.countryLinks.get(countryName) + "/#geography";
        //System.out.println(countryURL);
        try {
            this.currentDoc = Jsoup.connect(countryGeoURL).get();
        } catch (IOException e) {
            System.out.println("Couldn't connect to country geo page");
            System.out.println(countryName);
        }
        Element strongTag = this.currentDoc.select("strong:contains(total:)").get(0);
        Element pTag = strongTag.parent();

        int valuesParsedSoFar = 0;
        for (String chunk : pTag.text().replaceAll(",","").split(" ")) {
            //System.out.println(chunk);
            try {
                Integer landArea = Integer.parseInt(chunk);
                if (valuesParsedSoFar == 1) {
                    //System.out.println(countryName + landArea);
                    return landArea;
                } else {
                    valuesParsedSoFar += 1;
                }
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }


    /*
     * Prints pop. of country in continent with the highest mean elevation
     */
    public void questionFive(String continent) {
        ArrayList<String> continentCountries = getCountriesInContinent(continent);

        int highestElev = 0;
        int highestCountryPop = 0;
        String highestCountry = null;
        for (String country : continentCountries) {
            int pop = getPopulation(country);
            int elevation = getElevation(country);
            if (elevation > highestElev) {
                highestElev = elevation;
                highestCountry = country;
                highestCountryPop = pop;
            }
        }
        System.out.println("Population of the country with highest mean elevation is: " + highestCountryPop +
                ", from country " + highestCountry);

    }
    private int getPopulation(String countryName) {
        String countryGeoURL = this.countryLinks.get(countryName) + "/#geography";
        try {
            this.currentDoc = Jsoup.connect(countryGeoURL).get();
        } catch (IOException e) {
            System.out.println("Couldn't connect to country geo page");
            System.out.println(countryName);
        }
        Element divTag = this.currentDoc.getElementById("people-and-society");
        Element pTag = divTag.select("div").first().select("p").first();
        for (String chunk : pTag.text().replaceAll(",","").split(" ")) {
            //System.out.println(chunk);
            try {
                Integer population = Integer.parseInt(chunk);
                //System.out.println(countryName + population);
                return population;
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }
    private int getElevation(String countryName) {
        String countryGeoURL = this.countryLinks.get(countryName) + "/#geography";
        try {
            this.currentDoc = Jsoup.connect(countryGeoURL).get();
        } catch (IOException e) {
            System.out.println("Couldn't connect to country geo page");
            System.out.println(countryName);
        }
        Element strongTag = this.currentDoc.select("strong:contains(point:)").first();
        Element pTag = strongTag.parent();
        int valuesParsedSoFar = 0;
        for (String chunk : pTag.text().replaceAll(",","").split(" ")) {
            //System.out.println(chunk);
            try {
                Integer elevation = Integer.parseInt(chunk);
                if (valuesParsedSoFar == 2) {
                    //System.out.println(countryName + elevation);
                    return elevation;
                } else {
                    valuesParsedSoFar += 1;
                }
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }


    /*
     * Prints a list of the imports partners for the third-largest island (by total area)
     */
    public void questionSix(String region) {
        ArrayList<String> regionIslands = getIslandsInRegion(region);
        String thirdLargest = getThirdLargestIsland(regionIslands);
        if (thirdLargest == null) {
            System.out.println("There are not at least 3 islands in this region");
        } else {
            ArrayList<String> importCountries = getImportCountries(thirdLargest);
            System.out.println("Import countries are: " + importCountries);
        }
        ArrayList<String> importCountries = getImportCountries(thirdLargest);
        System.out.println("Import countries are: " + importCountries);

    }
    private ArrayList<String> getIslandsInRegion(String region) {
        ArrayList<String> regionIslands = new ArrayList<String>();
        for (String country : this.countryList) {
            String countryGeoURL = this.countryLinks.get(country) + "/#geography";
            try {
                this.currentDoc = Jsoup.connect(countryGeoURL).get();
            } catch (IOException e) {
                System.out.println("Couldn't connect to country geo page");
                System.out.println(country);
            }
            Element divTag = this.currentDoc.getElementById("geography");
            Element pTag = divTag.select("div").get(1).select("p").first();

            if (checkIfWordsInText(pTag.text(), region, "island")) {
                regionIslands.add(country);
            }
        }
        return regionIslands;
    }
    private String getThirdLargestIsland(ArrayList<String> regions) {
        ArrayList<Map.Entry<String,Integer>> areaList = new ArrayList<>();
        for (String country : regions) {
            String countryGeoURL = this.countryLinks.get(country) + "/#geography";
            //System.out.println(countryURL);
            try {
                this.currentDoc = Jsoup.connect(countryGeoURL).get();
            } catch (IOException e) {
                System.out.println("Couldn't connect to country flag page");
                System.out.println(country);
            }
            Element strongTag = this.currentDoc.select("strong:contains(total:)").get(0);
            Element pTag = strongTag.parent();

            for (String chunk : pTag.text().replaceAll(",","").split(" ")) {
                //System.out.println(chunk);
                try {
                    Integer totalArea = Integer.parseInt(chunk);
                    //System.out.println(country + " Total area: " + totalArea + "sq km");
                    areaList.add(new AbstractMap.SimpleEntry<>(country, totalArea));
                    break;
                } catch (NumberFormatException e) {
                }
            }
        }
        areaList.sort(Comparator.comparing(entry -> -1 * entry.getValue())); // descending order of total land area
        System.out.println(areaList);
        if (areaList.size() < 3) {
            return null;
        } else {
            return areaList.get(2).getKey(); // third-largest country
        }
    }
    private ArrayList<String> getImportCountries(String country) {
        ArrayList<String> importCountries = new ArrayList<String>();
        String countryGeoURL = this.countryLinks.get(country) + "/#geography";
        try {
            this.currentDoc = Jsoup.connect(countryGeoURL).get();
        } catch (IOException e) {
            System.out.println("Couldn't connect to country flag page");
            System.out.println(country);
        }
        Element aTag = this.currentDoc.select("a[href=\"/the-world-factbook/field/imports-partners\"]").first();
        Element pTag = aTag.parent().parent().select("p").first();
        System.out.println(pTag.text());

        int i = 0;
        String currCountry = "";
        String[] text = pTag.text().split(" ");
        while (i < text.length) {
            String chunk = text[i];
            if (chunk.contains("%")) {
                if (this.countryList.contains(currCountry.trim())) {
                    System.out.println(currCountry.trim());
                    importCountries.add(currCountry.trim());
                }
                currCountry = ""; // reset string to start getting next import country
            } else {
                currCountry += " " + chunk;
            }
            i += 1;
        }
        return importCountries;
    }


    /*
     * Prints a list of all countries starting with letter, sorted by total area smallest to largest
     */
    public void questionSeven(String letter) {
        ArrayList<Map.Entry<String,Integer>> areaList = new ArrayList<>();
        for (String country : this.countryList) {
            if (!country.startsWith(letter)) continue;
            String countryGeoURL = this.countryLinks.get(country) + "/#geography";
            //System.out.println(countryURL);
            try {
                this.currentDoc = Jsoup.connect(countryGeoURL).get();
            } catch (IOException e) {
                System.out.println("Couldn't connect to country flag page");
                System.out.println(country);
            }
            Element strongTag = this.currentDoc.select("strong:contains(total:)").get(0);
            Element pTag = strongTag.parent();

            for (String chunk : pTag.text().replaceAll(",","").split(" ")) {
                //System.out.println(chunk);
                try {
                    Integer totalArea = Integer.parseInt(chunk);
                    //System.out.println(country + " Total area: " + totalArea + "sq km");
                    areaList.add(new AbstractMap.SimpleEntry<>(country, totalArea));
                    break;
                } catch (NumberFormatException e) {

                }
            }
        }
        areaList.sort(Comparator.comparing(entry -> entry.getValue()));
        //System.out.println(areaList);
        ArrayList<String> sortedCountriesList = new ArrayList<String>();
        for (int i=0; i < areaList.size(); i++) {
            sortedCountriesList.add(areaList.get(i).getKey());
        }
        System.out.println(sortedCountriesList);
    }


    /*
     * Prints the country in continent with the smallest value of expenditures as a % of revenues
     */
    public void questionEight(String continent) {
        ArrayList<String> continentCountries = getCountriesInContinent(continent);
        double smallestProportion = 1000;
        String smallestProportionCountry = null;
        for (String country : continentCountries) {
            double currProportion = getProportion(country);
            if (currProportion == -1) {
                continue; // skipping over the edge case (European Union), which doesn't have revenue & expenditures
            }
            if (currProportion < smallestProportion) {
                smallestProportion = currProportion;
                smallestProportionCountry = country;
            }
        }
        System.out.println(smallestProportionCountry + " is the country in continent with smallest proportion of " +
                "annual expenditures as a % of revenues");
        System.out.println("This value is: " + smallestProportion);
    }
    private double getProportion(String country) {
        String countryGeoURL = this.countryLinks.get(country) + "/#geography";
        try {
            this.currentDoc = Jsoup.connect(countryGeoURL).get();
        } catch (IOException e) {
            System.out.println("Couldn't connect to country geo page");
            System.out.println(country);
        }
        Element strongTag = this.currentDoc.select("strong:contains(expenditures:)").first();
        if (strongTag == null) return -1;
        Element pTag = strongTag.parent();

        double revenues = 0;
        double expenditures = 0;
        int valuesParsedSoFar = 0;
        //System.out.println(pTag.text());
        for (String chunk : pTag.text().split(" ")) {
            //System.out.println(chunk);
            try {
                double x = Double.parseDouble(chunk);
                if (valuesParsedSoFar == 0) {
                    //System.out.println("revenues: " + x);
                    revenues = x;
                } else if (valuesParsedSoFar == 1) {
                    //System.out.println("expenditures: " + x);
                    expenditures = x;
                }
                valuesParsedSoFar += 1;
            } catch (NumberFormatException e) {
            }
        }
        return ((double) expenditures / revenues);
    }
}