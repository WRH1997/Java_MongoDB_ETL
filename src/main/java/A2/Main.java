package A2;

import java.util.ArrayList;
import java.util.List;


/*
The Main class simply has a runnable main method that, in turn, starts the process of fetching, extracting,
reformatting, and saving data from the News API into files and a MongoDB database collection.

First, a list of Strings specifying the keywords is initialized. This is then passed to an object
of the NewsExtractor class (Code A), which fetches and parses response data from the News API.
Then, the NewsExtractor object creates an object of the FileInterface class (Code B), which reformats and
saves news stories into files. Finally, the NewsExtractor class create an object of the MongoInterface class
(Code C) to store the fetched News Articles into a MongoDB Database Collection (MyMongoNews.News)

@Author: Waleed R. Alhindi (B00919848)
 */
public class Main {
    public static void main(String[] args) throws Exception{
        List<String> keywords = new ArrayList<>();
        keywords.add("Canada");
        keywords.add("University");
        keywords.add("Dalhousie");
        keywords.add("Halifax");
        keywords.add("Canada Education");
        keywords.add("hockey");
        keywords.add("Fredericton");
        keywords.add("celebration");
        NewsExtract NE = new NewsExtract();
        NE.ApiCall(keywords);
    }

}