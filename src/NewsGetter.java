import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*
Class created by George, adapted by Louis.
 */

public class NewsGetter {
    private NodeList items;
    private int currentItem = 0;

    /*
    Constructor runs on program launch and fetches the data from the URL into the NodeList object
     */
    public NewsGetter() {
        String url = "https://www.cnbc.com/id/10000664/device/rss/rss.html";
        try {
            DocumentBuilderFactory f =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder b = f.newDocumentBuilder();
            Document doc = b.parse(url);

            doc.getDocumentElement().normalize();

            // loop through each item
            items = doc.getElementsByTagName("item");

        } catch (Exception e) {
            System.out.println("Could not fetch news document (NewsGetter)");
        }
    }

    public String[] getNews() {
        try{
            String title = "Could not get news";
            String description;
            String link;
            Node n = items.item(currentItem);

            Element e = (Element) n;
            //newsFound = true;

            //for the link
            // get the "title elem" in this item (only one)
            NodeList linkList =
                    e.getElementsByTagName("link");
            Element linkElem = (Element) linkList.item(0);

            // get the "text node" in the title (only one)
            Node linkNode = linkElem.getChildNodes().item(0);
            link = linkNode.getNodeValue();


            // get the "title elem" in this item (only one)
            NodeList titleList =
                    e.getElementsByTagName("title");
            Element titleElem = (Element) titleList.item(0);

            // get the "text node" in the title (only one)
            Node titleNode = titleElem.getChildNodes().item(0);
            title = titleNode.getNodeValue();


            //again for the description
            // get the "title elem" in this item (only one)
            NodeList descriptionNode =
                    e.getElementsByTagName("description");
            Element descElem = (Element) descriptionNode.item(0);

            // get the "text node" in the title (only one)
            Node descNode = descElem.getChildNodes().item(0);
            description = descNode.getNodeValue() + "...";
            return (new String[]{link, title, description});
        }
        catch (NullPointerException e){
            return new String[]{"", "Could not fetch news...", "Please check your internet connection"};
        }
    }

    /*
    Increments the local counter so that the next item can be retrieved.
     */
    public void getNextStory(){
        if(this.currentItem < items.getLength())
        this.currentItem++;
    }

    public void getPreviousStory(){
        if(this.currentItem > 0){
        this.currentItem--;}
    }

    public int getPos(){
        return this.currentItem;
    }

    public static void main(String[] args) {
        NewsGetter newsGetter = new NewsGetter();
        String[] news = newsGetter.getNews();
        System.out.println(news[0] + "\n" + news[1] + "\n" + news[2]);
        newsGetter.getNextStory();
        news = newsGetter.getNews();
        System.out.println(news[0] + "\n" + news[1] + "\n" + news[2]);

    }
}