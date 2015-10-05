/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hello;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.DOMReader;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import hello.service.HelloWorldService;


@SpringBootApplication
public class W2j implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(W2j.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	private HelloWorldService helloWorldService;

	@Value("${year:1}")
	private int year;
	int filesCount;
	private	int fileIdx = 0;
	//develop
	private static String basicDir ="/home/roman/jura/";
	private static String fileSeparator = "/";
	DOMReader domReader = new DOMReader();
	DateTime startMillis;
	String domain = "http://workshop-manuals.com";
	private String workDir, dirJsonName, dirLargeHtmlName, dirPdfName;
	String outputDir ;
	
	Path pathStart;
	private int debugSkip;

	@Override
	public void run(String... args) {
		System.out.println(this.helloWorldService.getHelloMessage());
		System.out.println(year);
		workDir = workDir();
		dirJsonName = workDir + "OUT1json/";
		dirLargeHtmlName = workDir+ "OUT1html/";
		dirPdfName = workDir+ "OUT1pdf/";
		pathStart = Paths.get(dirLargeHtmlName);
		outputDir = workDir;
		startMillis = new DateTime();
		System.out.println("--------------------");
		System.out.println(year);
		System.out.println("--------------------");
		
domReader.setDocumentFactory(new DOMDocumentFactory());
		
//		readAuto(ford1, "c-max_2003.75_06.2003", "ford");
//		if(true) return;
		
//		createAutoIndexDocument();
		
		Document document = getDomFromStream(domain);
		logger.debug(outputDir);
		File openOutputFolder = openCreateFolder(outputDir);
		
		logger.debug(""+openOutputFolder);
		List<Element> selectNodes = document.selectNodes("/html/body/div/table//a[contains(@href,'workshop-manuals')]");
		for (Element manufacturerAncorElement : selectNodes) {
//			addOl1li(element);
			String href = manufacturerAncorElement.attributeValue("href");
			if(href.equals(domain)) continue;
			String[] split = href.split("/");
			String manufacturer = split[split.length - 1];
			logger.debug(href+" :: "+manufacturer);
//			if(new File(outputDir+"/"+manufacturer).exists())
//				continue;
			File dm = openCreateFolder(outputDir+"/"+manufacturer);
			Document document2 = getDomFromStream(href);
			if(document2 != null)
			{
				readManufacturer(manufacturerAncorElement, href, manufacturer, document2);
			}
//			cnt1++;
		}
		String msg = "url "+domain + " :: overall "+selectNodes.size()+"/"+cntVehicles;
	}
	private int cnt1 = 0, cnt2 = 0, cntVehicles = 0;
	private void readManufacturer(Element manufacturerAncorElement, String href, String manufacturer, Document document2) {
		List<Element> allManufacturerAutos = document2.selectNodes("/html/body/div/table//a[contains(@href,'"
				+ manufacturer
				+ "')]");
//				addOl1ToLastLi(selectNodes2);
		logger.debug(href+" :: "+allManufacturerAutos.size());
		if(true){
			cnt2=0;
			for (Element autoElement : allManufacturerAutos) {
				//						addOl1Ol2Li(element2);
				String autoHref = autoElement.attributeValue("href");
				
				DOMElement parent = (DOMElement) autoElement.getParent();
				DOMElement previousSibling = (DOMElement) parent.getPreviousSibling();
				String autoName = autoElement.getText().trim();
				if(previousSibling != null)
				{
					if(previousSibling.getName().equals("h2"))
					{
						autoName = "-- "+ previousSibling.getText()+" -- "+ autoName;
					}
				}
				autoName = autoName.trim();
				String replace = autoHref.replace("/", "");
				if(replace.equals(manufacturer) 
						|| autoHref.equals(domain)
						) continue;
				//				if(!autoName.contains(yearMin))					continue;
				for (int y = year; y <= year; y++) {
					if(autoName.contains(""+y)){
//						autoDocument = createAutoDocument();

						autoTileAllIndexNr = 0;
						String htmlOutFileName = getHtmlOutFileName(autoName.replaceAll(" ", "_"), manufacturer);
						String htmlOutFileName2 = htmlOutFileName+".json";
						logger.debug(htmlOutFileName2);
						File f = new File(htmlOutFileName2);
						if(f.exists())
						{
							logger.debug("f.exists() --  "+htmlOutFileName2);
							continue;
						}

						readAutoIndexList(autoHref, autoName, manufacturer);
						//				readAuto(autoHref, autoName, manufacturer);
						cnt2++;
						System.out.println("-----------------------------------------------------"+cnt2);
						//				if(cnt2==1)
						//					break;
					}
				}
			}
		}
	}
	Map<String, Object> autoData;
	private void initAutoData() {
		autoData = new HashMap<String, Object>();
		autoData.put("workPath", new ArrayList<Integer>());
		initIndexList(autoData);
	}
	private List<Map<String, Object>> initIndexList(Map<String, Object> autoData) {
		if(!autoData.containsKey("indexList")){
			autoData.put("indexList", new ArrayList<Map<String, Object>>());
		}
		return (List<Map<String, Object>>) autoData.get("indexList");
	}
	private void readNextAutoIndexList(String autoTileHref) {
		DOMDocument autoTileContextDom = null;
		try{
			Document autoTileDom = getDomFromStream(autoTileHref);
			String indexHrefAdd = ((Attribute) autoTileDom.selectSingleNode("/html/body//iframe/@src")).getValue();
			String hrefContent = autoTileHref+""+indexHrefAdd;
			autoTileContextDom = getDomFromStream(hrefContent);
		}catch (Exception e){
			List<Map<String, Object>> workIndexList = initIndexList(autoData);
			Map<String, Object> workContentItem = workIndexList.get(workIndexList.size() - 1);
			HashMap<String, Object> contextItem = new HashMap<String, Object>();
			contextItem.put("text", "BAD PAGE");
			contextItem.put("error", e.getMessage());
			contextItem.put("url", autoTileHref);
			workIndexList.add(contextItem);
			logger.error(e.getMessage());
			nextAutoTileElement = null;
			return ;
		}
		if (autoTileContextDom == null)
			return ;
		List<DOMElement> myPagePosition = (List<DOMElement>) autoTileContextDom.selectNodes("/html/body/div/p[@style and not(a)]");
		if(myPagePosition.size() == 0)
		{//bad link auto tile page not exist
			logger.debug(nextAutoTileElement.asXML());
			nextAutoTileElement = (DOMElement) nextAutoTileElement.getNextSibling();
			logger.debug(nextAutoTileElement.asXML());
			return ;
		}
		Map<String, Object> contextItem = null;
		for (Element element : myPagePosition) {
			String text = element.getText();
			String style = element.attribute("style").getValue();
			String level = style.split(";")[0].split("padding-left:")[1].split("pt")[0];
			int levelInt = Integer.parseInt(level)/10;
			if(levelInt < 0 && !autoData.containsKey("autoName")){
				autoData.put("autoName", text);
			}else if(levelInt >= 0){
				int indexOfcurrent = text.indexOf(">>");
				if(indexOfcurrent == 0)
				{
					levelInt = levelInt + 1;
					text = text.replace(">>", "").replace("<<", "").trim();
					logger.debug(autoTileAllIndexNr+"/"+levelInt+" -- "+text);
//					logger.debug(autoTileIndexNr+"/"+autoTileAllIndexNr+"/"+levelInt+" -- "+text);
				}
				contextItem = new HashMap<String, Object>();
				contextItem.put("text", text);
				Map<String, Object> workContentItem = autoData;
				List<Map<String, Object>> workIndexList = initIndexList(workContentItem);
				for (int i = 0; i < levelInt; i++) {
					if(workIndexList.size() == 0)//not skip level
						break;
					workContentItem = workIndexList.get(workIndexList.size() - 1);
					workIndexList = initIndexList(workContentItem);
				}
				workIndexList.add(contextItem);
			}
		}
		
		DOMElement lastElement = myPagePosition.get(myPagePosition.size() - 1);
		contextItem.put("url", autoTileHref);
		nextAutoTileElement = (DOMElement) lastElement.getNextSibling();
//		nextAutoTileElement = getLastIndexElement(nextAutoTileElement);
		
		autoTileAllIndexNr++;
		
	}
	private void getNextTrueSibling() {
		Node selectSingleNode = nextAutoTileElement.selectSingleNode("a/@href");
		String stringValue = selectSingleNode.getStringValue();
		if(stringValue.equals(domain))
			nextAutoTileElement = (DOMElement) nextAutoTileElement.getNextSibling();
}
	void writeToJsonDbFile(Object java2jsonObject, String fileName) {
		File file = new File(fileName);
		logger.warn(""+file);
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writerWithDefaultPrettyPrinter = mapper.writerWithDefaultPrettyPrinter();
		try {
			//			logger.warn(writerWithDefaultPrettyPrinter.writeValueAsString(java2jsonObject));
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			writerWithDefaultPrettyPrinter.writeValue(fileOutputStream, java2jsonObject);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	DOMElement nextAutoTileElement = null;
	private void readAutoIndexList(String autoTileHref, String autoName, String manufacturer) {
		autoTileAllIndexNr = 0;
		initAutoData();
		readNextAutoIndexList(autoTileHref);
		while(nextAutoTileElement != null){
			getNextTrueSibling();
			if(nextAutoTileElement == null){
				break;
			}
			Attribute hrefAttribute = (Attribute) nextAutoTileElement.selectSingleNode("a/@href");
			String hrefAutoTileNext = hrefAttribute.getValue();
			readNextAutoIndexList(hrefAutoTileNext);
		}
		try{
//			buildBookmark(autoDocument);
			autoName = autoName.replaceAll(" ", "_");
			String htmlOutFileName = getHtmlOutFileName(autoName, manufacturer);
//			saveHtml(autoDocument, htmlOutFileName);
//			savePdf(htmlOutFileName, htmlOutFileName+".pdf");
			writeToJsonDbFile(autoData, htmlOutFileName+".json");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private String getHtmlOutFileName(String autoName, String manufacturer) {
		autoName = autoName.replace("/", ":");
		String htmlOutFileName = outputDir+"/"+manufacturer+"/"+manufacturer+"_"+autoName+".html";
		return htmlOutFileName;
	}
	int autoTileAllIndexNr = 0;
	private void makePdfFromHTML() throws IOException {
		Path pathHtmlLarge = Paths.get(dirLargeHtmlName);
		//		Path pathHtmlLarge = Paths.get(dirPdfName);
		logger.debug("Start folder : "+pathHtmlLarge);
		Files.walkFileTree(pathHtmlLarge, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				final FileVisitResult visitFile = super.visitFile(file, attrs);

				fileIdx++;
				logger.debug(fileIdx + "" + "/" + filesCount + procentWorkTime() + file);

				final String fileName = file.toString();
				logger.debug(fileName);
				final String[] splitFileName = fileName.split("\\.");
				final String fileExtention = splitFileName[splitFileName.length - 1];
				String[] splitPathFileName = fileName.split("/");
				logger.debug(""+splitPathFileName);
				final String fileNameShort = splitPathFileName[splitPathFileName.length - 1];
				logger.debug(""+fileNameShort);

				String hTML_TO_PDF = dirPdfName+ fileNameShort+".pdf";
				File f = new File(hTML_TO_PDF);
				if(f.exists())
				{
					logger.debug("f.exists() --  "+hTML_TO_PDF);
					return visitFile;
				}


				if("html".equals(fileExtention)){
					logger.debug(fileName);
					try {
						savePdf(fileName, hTML_TO_PDF);
						//Files.delete(file);
					} catch (com.lowagie.text.DocumentException | IOException e) {
						System.out.println(fileName);
						e.printStackTrace();
					}
				}
				return visitFile;
			}
		});}

	void savePdf(String htmlOutFileName, String HTML_TO_PDF) throws com.lowagie.text.DocumentException, IOException {
		String url = new File(htmlOutFileName).toURI().toURL().toString();
		logger.debug(procentWorkTime()+" - start - "+HTML_TO_PDF);
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocument(url);
		renderer.layout();
		OutputStream os = new FileOutputStream(HTML_TO_PDF);
		renderer.createPDF(os);
		os.close();
		logger.debug(procentWorkTime()+" - end - "+HTML_TO_PDF);
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication application = new SpringApplication(W2j.class);
		application.setApplicationContextClass(AnnotationConfigApplicationContext.class);
		SpringApplication.run(W2j.class, args);
	}
	
	public static int countFiles2(File directory) {
		int count = 0;
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				count += countFiles2(file); 
			}else
				count++;
		}
		return count;
	}
	private String workDir() {
		return basicDir + "workshop-manuals"
				+ year
				+ "-"
				+ year
				+ "/";
	}
	static PeriodFormatter hmsFormatter = new PeriodFormatterBuilder()
			.appendHours().appendSuffix("h ")
			.appendMinutes().appendSuffix("m ")
			.appendSeconds().appendSuffix("s ")
			.toFormatter();
	String procentWorkTime() {
		int procent = fileIdx*100/filesCount;
		String workTime = hmsFormatter.print(new Period(startMillis, new DateTime()));
		String procentSecond = " - html2pdf3 - (" + procent + "%, " + workTime + "s)";
		return procentSecond;
	}
	private DOMDocument getDomFromStream(String url) {
		HttpURLConnection urlConnection = getUrlConnection(url);
		try {
			return getDomFromStream(urlConnection);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private DOMDocument getDomFromStream(HttpURLConnection urlConnection) throws IOException {
		InputStream requestBody = urlConnection.getInputStream();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(requestBody));
		String line = null;
		StringBuilder responseData = new StringBuilder();
		while((line = in.readLine()) != null) {
			if(line.contains("font size>")){
				line = line.replace("font size>", "font>");
			}
			if(line.contains("<g:plusone size=\"small\" annotation=\"none\"></g:plusone>")){
				line = line.replace("<g:plusone size=\"small\" annotation=\"none\"></g:plusone>", "");
			}
			responseData.append(line);
		}
		InputStream byteArrayInputStream = new ByteArrayInputStream(responseData.toString().getBytes(StandardCharsets.UTF_8));
		
		org.w3c.dom.Document html2xhtml = tidy.parseDOM(byteArrayInputStream, null);
//		org.w3c.dom.Document html2xhtml = tidy.parseDOM(requestBody, null);
		DOMDocument document = (DOMDocument) domReader.read(html2xhtml);
		return document;
	}
	Tidy tidy = getTidy();
	private Tidy getTidy() {
		Tidy tidy = new Tidy();
		tidy.setShowWarnings(false);
		tidy.setXmlTags(false);
		tidy.setInputEncoding("UTF-8");
		tidy.setOutputEncoding("UTF-8");
		tidy.setXHTML(true);// 
		tidy.setMakeClean(true);
		tidy.setQuoteNbsp(false);
		return tidy;
	}
	private HttpURLConnection getUrlConnection(String url) {
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "text/html"); 
			con.setRequestProperty("charset", "utf-8");
			return con;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private File openCreateFolder(String dir) {
		File file = new File(dir);
		if(!file.exists())
		{
			file.mkdirs();
		}
		return file;
	}
}
