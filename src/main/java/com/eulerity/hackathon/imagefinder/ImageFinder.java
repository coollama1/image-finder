package com.eulerity.hackathon.imagefinder;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.eulerity.hackathon.imagefinder.model.ImageData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	private List<ImageData> imageDataList;
 
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");
		
		imageDataList = new ArrayList<>();
		findImages(url, new ArrayList<>(), new HashSet<String>());
		List<ImageData> reducedImageDataList = reduceImageDataList(imageDataList);
		resp.getWriter().print(GSON.toJson(generateImageDataArray(reducedImageDataList)));
	}
	
	protected static String [] generateImageDataArray(List<ImageData> imageDataList) {
	    String [] imageDataArray = new String[imageDataList.size()*3];
	    int count = 0;
	    for(ImageData imageData : imageDataList) {
	        imageDataArray[count++] = Arrays.stream(imageData.getPathToLink()).collect(joining(","));
	        imageDataArray[count++] = imageData.getImageUrl();
	        imageDataArray[count++] = Integer.toString(imageData.getFrequency());
	    }
	    return imageDataArray;
	}
	
	protected static List<ImageData> reduceImageDataList(List<ImageData> imageDataList){
       List<ImageData> reducedList = imageDataList
                                           .stream()
                                           .collect(groupingBy(ImageData::getImageUrl, 
                                                       reducing(ImageFinder::reduceByShortestPathLength)))
                                           .values()
                                           .stream()
                                           .map(optional -> optional.get())
                                           .collect(toList());
	    return reducedList;
	}
	
	private static ImageData reduceByShortestPathLength(ImageData i1, ImageData i2) {
	    int i1PathLength = i1.getPathToLink().length;
        int i2PathLength = i2.getPathToLink().length;
        if(i1PathLength < i2PathLength) {
            i1.setFrequency(i1.getFrequency() + i2.getFrequency());
            return i1;
        }
        else {
            i2.setFrequency(i1.getFrequency() + i2.getFrequency());
            return i2;
        }
	}
	
	protected void findImages(String pageURL, List<String> pathToURL, Set<String> visitedLinks) {
	    boolean visitedLink = false;
 
	    synchronized (this) {
	        visitedLink = visitedLinks.contains(pageURL);
	        if(!visitedLink) {
	            visitedLinks.add(pageURL);
	        }
        }
	    
	    if(!visitedLink) {
	        List<String> newPath = new ArrayList<String>(pathToURL);
            newPath.add(pageURL);

	        List<ImageData> newImageDataList = getImagesFromURL(pageURL)
	                                                .stream()
	                                                .map(imgUrl -> new ImageData(imgUrl, pageURL, newPath.toArray(new String[0])))
	                                                .collect(toList());
	        imageDataList.addAll(newImageDataList);
	        
             List<String> sublinks = getSublinksFromURL(pageURL); 
             sublinks.parallelStream()
                     .forEach(sublink -> findImages(sublink,newPath,visitedLinks));
	    }
	}
	
	protected static List<String> getImagesFromURL(String url){
	    List<String> images = new ArrayList<>();
	    String imageRegexPattern = ".*\\.(jpeg|jpg|png|gif).*";
	    boolean urlIsImage = url.matches(imageRegexPattern);
	    if(urlIsImage) {
	        images.add(url);
	    }
	    else {
	        Document document;
	        try {
	            document = Jsoup.connect(url).get();
	            Elements imageElements = document.select("img");
	            imageElements
	                .stream()
	                .filter(imgElement -> imgElement.absUrl("src").matches(imageRegexPattern))
	                .forEach(imgElement -> images.add(imgElement.absUrl("src")));
	        }
	        catch(IOException e) {
	            e.printStackTrace();
	        }
	    }

	    return images;
	}
	
	protected static List<String> getSublinksFromURL(String url){
	    List<String> links = new ArrayList<>();
	    Document document;
	    try {
	        document = Jsoup.connect(url).get();
	        Elements linkElements = document.select("a");
	        
	        for(Element linkElement : linkElements) {
	            String link = linkElement.absUrl("href");
	            String domain = url.split("\\.(com|net|org|uk|info|blog)")[0];
	            boolean isNotImage = !link.matches(".*\\.(jpeg|png|gif).*");
	            boolean isNotJumpLink = !link.matches(url + "#.*");
	            if(link.contains(domain) && isNotImage && isNotJumpLink) {
	                links.add(link);
	            }
	        }
	    }
	    catch(IOException e) {
	        e.printStackTrace();
	    }
	    return links;
	}
}
