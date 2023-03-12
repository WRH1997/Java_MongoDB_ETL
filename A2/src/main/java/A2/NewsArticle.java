package A2;


/*
This class is used to encapsulate the data of each news article extracted by the NewsExtract class. This entails
storing each article's source, author, title, description, url, imageUrl, publishedAt, content, and keyword data
that was extracted in instance variables. This is done in order to more efficiently pass article data between different
classes and methods while only having to extract and reformat data once.

@Author: Waleed R. Alhindi (B00919848)
 */
public class NewsArticle {

    /*
    a set of instance variables to store an article's extracted data
     */
    private String source;
    private String author;
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String publishedAt;
    private String content;
    private String keyword;



    /*
    NewsArticle constructor that instantiates a NewsArticle object to encapsulate an individual article's data
    by being passed the extracted article's data

    @Param: String source --> The extracted article's source
            String author --> The extracted article's author
            String title --> The extracted article's title
            String description --> The extracted article's description
            String url --> The extracted article's URL
            String imageUrl --> The extracted article's image link
            String publishedAt --> The extracted article's date and time of publication
            String content --> The extracted article's content
            String keyword --> The keyword used when making the API call that returned this article
    @Return: no return value
     */
    NewsArticle(String source, String author, String title, String description, String url, String imageUrl, String publishedAt, String content, String keyword){
        this.source = source;
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
        this.content = content;
        this.keyword = keyword;
    }



    /*Setters and getters*/
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
