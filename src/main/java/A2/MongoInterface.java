package A2;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.List;



/*
This class (which addresses "Code C" in the assignment instructions) is called during NewsExtract's ApiCall to insert
the extracted articles into a MongoDB database collection (database = MyMongoNews, collection = News).
Simply put, this class is passed a list of extracted NewsArticle objects, then establishes a connection with a
Mongo Atlas cluster and inserts each of those extracted articles in the specified database collection within that cluster.

Additionally, this class will only insert an article into the collection if it does not already exist. This is to ensure
that no duplicates are stored even if the program is run several times against the same set of keywords.

@Author: Waleed R. Alhindi (B00919848)
 */
public class MongoInterface {

    /*MongoDB connection configuration instance variables*/
    private ConnectionString connectionString;
    private MongoClientSettings settings;
    private MongoClient mongoClient;
    private MongoDatabase database;
    MongoCollection<Document> collection;


    /*
    MongoInterface constructor that initializes the connection configuration variables to connect
    to a Mongo Atlas cluster's MyMongoNews database's News collection

    @Param: no parameters
    @Return: no return values
     */
    MongoInterface(){
        connectionString = new ConnectionString("mongodb://waleed:pokemon@ac-4driqs9-shard-00-00.ypxq3nm.mongodb.net:27017,ac-4driqs9-shard-00-01.ypxq3nm.mongodb.net:27017,ac-4driqs9-shard-00-02.ypxq3nm.mongodb.net:27017/?ssl=true&replicaSet=atlas-30rx39-shard-0&authSource=admin&retryWrites=true&w=majority");
        settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("MyMongoNews");
        collection = database.getCollection("News");
    }


    /*
    A public method called after extraction by NewsExtract. It is fed the extracted list of NewsArticle objects
    and, for each of those articles, it calls a separate private method to actually insert the article into
    the MongoDB collection.

    @Param: List<NewsArticle> articles --> a list of the extract news articles
    @Return: no return value
     */
    void addArticlesToMongoDb(List<NewsArticle> articles) throws MongoException{
        for(NewsArticle article: articles){
            try{
                insertArticle(article);
            }
            catch(MongoException e){
                throw e;
            }
        }
    }


    /*
    This method inserts an extracted article's data into the MongoDB News collection. However, before inserting an article,
    it first checks whether that article is a duplicate of an existing entry. If it is a duplicate, then this method will
    not insert it. But, if it is not a duplicate, then it will go ahead and insert that article as a new document in the
    collection.

    @Param: NewsArticle article --> The individual NewsArticle object to be inserted
    @Return: no return value
     */
    private void insertArticle(NewsArticle article) throws MongoException{
        try{
            /*
            @CITATION NOTE: The following source was referenced to compose the filter search query code below.
            URL: //https://www.mongodb.com/docs/drivers/java/sync/v4.3/fundamentals/builders/filters/
            [Accessed: March 10, 2023]
             */
            Bson query = Filters.and(
                Filters.eq("title", sanitize(article.getTitle())),
                Filters.eq("source", sanitize(article.getSource())),
                Filters.eq("keyword",article.getKeyword()),
                Filters.eq("author", sanitize(article.getAuthor())),
                Filters.eq("description", sanitize(article.getDescription())),
                Filters.eq("url", article.getUrl()),
                Filters.eq("imageUrl", article.getImageUrl()),
                Filters.eq("publishedAt", article.getPublishedAt()),
                Filters.eq("content", sanitize(article.getContent()))
            );
            Document existingDoc = collection.find(query).first();
            if(existingDoc==null){
                Document insertArticle = new Document();
                insertArticle.append("_id", new ObjectId());
                insertArticle.append("title", sanitize(article.getTitle()));
                insertArticle.append("source", sanitize(article.getSource()));
                insertArticle.append("keyword", article.getKeyword());
                insertArticle.append("author", sanitize(article.getAuthor()));
                insertArticle.append("description", sanitize(article.getDescription()));
                insertArticle.append("url", article.getUrl());
                insertArticle.append("imageUrl", article.getImageUrl());
                insertArticle.append("publishedAt", article.getPublishedAt());
                insertArticle.append("content", sanitize(article.getContent()));
                InsertOneResult insertRes = collection.insertOne(insertArticle);
            }
        }
        catch(MongoException e){
            throw e;
        }
    }


    /*
    This method is used the insertArticle method to sanitize an article's data (i.e., remove special characters and the like).

    @Param: String unsanitizedStr --> one of the article's preprocessed data points
    @Return: a sanitized String that has had its special characters removed and reformatted
     */
    private String sanitize(String unsanitizedStr){
        String sanitizedStr = unsanitizedStr.replaceAll("[^a-zA-Z0-9\\s+.:,]"," ").replaceAll("\\s+", " ").trim();
        return sanitizedStr;
    }

}


