package A2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Scanner;


/*
This class (which address "Code B" in the assignment instructions) is called after article data is extracted
in the NewsExtract class's ApiCall method. Given a list of NewsArticle objects (which encapsulate an article's
extracted data), this class writes the articles' data into txt files which are located under the "Output" folder
of this project. Furthermore, since each file must only store a maximum of 5 articles, this class either appends
article data to an existing file that contains less than 5 articles, or creates new file(s) to store groups of 5 articles.

@Author: Waleed R. Alhindi (B00919848)
 */
public class FileInterface {


    /*
    FileInterface constructor, left empty since there are no instance variables to initialize
     */
    FileInterface(){}


    /*
    This method is called by the NewsExtractor class's ApiCall method once it has completed extracting article data.
    This method takes in a list of NewsArticle objects, then writes each article's data into text files found under the
    "Output" folder of this project.

    However, it makes sure that each file stores a maximum of 5 articles by checking
    whether the most recent file contains fewer than 5 articles. If it does, then it appends the current article to that file.
    However, if it does not, then it will create a new file to store the current article. This verification process is done
    for each iteration in the list of NewsArticles, thus ensuring that no file houses more than 5 articles.

    @Param: List<NewsArticle> newsArticle --> a list of the extracted NewsArticle objects
    @Return: no return value
     */
    void writeToFiles(List<NewsArticle> newsArticles) throws Exception{
        for(NewsArticle article: newsArticles){
            String outputFolderPath = new File("Output/").getAbsolutePath();
            File outputFolder = new File(outputFolderPath);
            int counter = 1;
            try{
                File[] listOfFiles = outputFolder.listFiles();
                if(listOfFiles.length==0){
                    File firstFile = new File(outputFolderPath + "/" + String.valueOf(counter) + ".txt");
                    firstFile.createNewFile();
                }
                else{
                    counter = listOfFiles.length;
                }
            }
            catch(Exception e){
                throw e;
            }
            String textFilePath = new File(outputFolderPath + "/" + String.valueOf(counter)+".txt").getAbsolutePath();
            File textFile = new File(textFilePath);
            Scanner fileReader;
            String fileContent = "";
            try{
                fileReader = new Scanner(textFile);
                while(fileReader.hasNext()){
                    fileContent += fileReader.nextLine();
                }
                fileReader.close();
                if(existingArticlesInFile(fileContent)<5){
                    appendToFile(textFile, article);
                }
                else{
                    File newFile = new File(outputFolderPath + "/" + String.valueOf(counter+1) + ".txt");
                    newFile.createNewFile();
                    appendToFile(newFile, article);
                }
            }
            catch(Exception e){
                throw e;
            }
        }
    }



    /*
    This method is used during the writeToFiles method to check whether the most recently created file has reached
    the limit of 5 articles stored.

    @Param: String fileContent --> The most recent file's contents in String form
    @Return: an integer denoting how many articles are currently stored in that file
     */
    private int existingArticlesInFile(String fileContent){
        if(fileContent.trim().equals("")){
            return 0;
        }
        String[] articleCount = fileContent.split("Title: ");
        return articleCount.length - 1;
    }



    /*
    This method is called during the writeToFile method to append an article's data into an existing file
    or create a new file and write the article's data there.

    Note that each article is stored in the following format within these text files:
        Title: <title>
        Source: <source>
        Keyword: <keyword>
        Author: <author>
        Description: <description>
        URL: <url>
        Image URL: <imageUrl>
        Published At: <publishedAt>
        Content: <content>
        <empty line>

    @Param: File target --> The file to append article data to, or to create and write to
            NewsArticle newsArticle --> The extracted article data which needs to be stored in that file
    @Return: no return value
     */
    private void appendToFile(File target, NewsArticle newsArticle) throws Exception{
        String content = "Title: " + newsArticle.getTitle();
        content += "\nSource: " + newsArticle.getSource();
        content += "\nKeyword: " + newsArticle.getKeyword();
        content += "\nAuthor: " + newsArticle.getAuthor();
        content += "\nDescription: " + newsArticle.getDescription();
        content += "\nURL: " + newsArticle.getUrl();
        content += "\nImage URL: " + newsArticle.getImageUrl();
        content += "\nPublished At: " + newsArticle.getPublishedAt();
        content += "\nContent: " + newsArticle.getContent() + "\n\n";
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(target, true));
            writer.append(content);
            writer.close();
        }
        catch(Exception e){
            throw e;
        }
    }
}
