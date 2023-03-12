package A2;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/*
This class (which addresses "Code A" in the assignment instructions) is makes an API call to the News API
to fetch responses that contain articles relating to the set of keywords specified. It then extracts each
article's relevant data, then create an object of the FileInterface class (Code B) to store the extracted data
into files. After which, it creates an object of the MongoInterface class (Code C) to pre-process (clean) and
store the extracted articles into a MongoDB database collection.

@Author: Waleed R. Alhindi (B00919848)
 */
public class NewsExtract {

    //News API base URL reference
    private String baseUrl;


    /*
    NewsExtract constructor that initializes the News API base URL variable

    @Param: no parameters
    @Return: no return values
     */
    NewsExtract(){
        this.baseUrl = "https://newsapi.org/v2/top-headlines?apiKey=2684c416d5bf4273948e90f76d5f2f59&q=";
    }


    /*
    The ApiCall method takes in a list of keywords. For each keyword, it makes a call to the New API to fetch
    the data of articles involving that keyword. The response data is then passed to a separate method (parseResponse(..))
    to extract each article's data into NewsArticle objects, which it returns as a list of NewsArticle objects.

    This list of NewsArticle objects is then passed into an instance of the FileInterface class to write into txt files.
    Finally, the list of NewsArticle objects is passed into an instance of the MongoInterface class to be inserted into
    a MongoDB database collection

    @Param: List<String> keywords --> a set of keywords to make API calls against (i.e., "Canada", "University", etc.)
    @Return: no return value
     */
    public void ApiCall(List<String> keywords) throws Exception{
        for(String keyword: keywords){
            keyword = keyword.trim().replaceAll(" ", "%20");
            String response = "";
            try{
                /*
                @CITATION NOTE: The following source was referenced to compose the code below that
                makes the API call to the News API.
                URL: https://medium.com/swlh/getting-json-data-from-a-restful-api-using-java-b327aafb3751
                [Accessed: March 10, 2023]
                 */
                URL url = new URL(baseUrl+keyword);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responseCode);
                }
                Scanner reader = new Scanner(url.openStream());
                while(reader.hasNext()){
                    response += reader.nextLine();
                }
                reader.close();
            }
            catch(Exception e){
                throw e;
            }
            if(response.contains("source")){
                List<NewsArticle> articles = parseResponse(response, keyword);
                FileInterface fileInterface = new FileInterface();
                fileInterface.writeToFiles(articles);
                MongoInterface mongoInterface = new MongoInterface();
                mongoInterface.addArticlesToMongoDb(articles);
            }
        }
    }


    /*
    This method is called during the ApiCall method to parse response data (i.e., to extract the data of
    each article in that response). It does this by splitting the response's content using regex patterns at pre-defined
    response structures that are specific to the News API being used.

    In other words, the method parses out each individual article's data from the response data. It then encapsulates
    each article's data into a NewsArticle object, which it adds to a list of NewsArticle objects.

    @Param: String responseStr --> a stringified form of the response data fetched from News API
            String keyword --> the keyword used when making the API call
    @Return: a list of NewsArticle objects extracted from the response data
     */
    private List<NewsArticle> parseResponse(String responseStr, String keyword){
        List<NewsArticle> newsArticles = new ArrayList<>();
        String[] articles = responseStr.split("\"source\"");
        for(String article: articles){
            if(!article.contains("id")){
                continue;
            }
            String source = article.split("\"name\":")[1].split("\\}")[0].replaceAll("\"","").trim();
            String author = article.split("\"author\":")[1].split(",\"title\"")[0].replaceAll("\"","").trim();
            String title = article.split("\"title\":")[1].split(",\"description\"")[0].replaceAll("\"","").trim();
            String description = article.split("\"description\":")[1].split(",\"url\"")[0].replaceAll("\"","").trim();
            String url = article.split("\"url\":")[1].split(",\"urlToImage\"")[0].replaceAll("\"","").trim();
            String imageUrl = article.split("\"urlToImage\":")[1].split(",\"publishedAt\"")[0].replaceAll("\"","").trim();
            String publishedAt = article.split("\"publishedAt\":")[1].split(",\"content\"")[0].replaceAll("\"","").trim();
            String content = article.split("\"content\":")[1].split("\\}")[0].replaceAll("\"","").trim();
            NewsArticle newsArticle = new NewsArticle(source, author, title, description, url, imageUrl, publishedAt, content, keyword);
            newsArticles.add(newsArticle);
        }
        return newsArticles;
    }
}
