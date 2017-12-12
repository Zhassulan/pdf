package kz.ugs.callisto.mis.reports.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
public class App  {
	
	public static Logger logger = LogManager.getLogger(App.class);
	public static final String htmlEncoding = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />";
	
    public static void main( String[] args )    {
    	
    	App app = new App();
    	
    	try {
    		String pathName = "c:/temp/file1/";
    		String file = "22.pdf";
    		Path path = Paths.get(pathName + file);
    		app.extractImg(Files.readAllBytes(path), pathName, file);
    		String res = app.parseToHTML(pathName + file);
    		File in = new File(pathName + file + ".html");
    		FileUtils.writeStringToFile(in, res, "UTF-8");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    }
    
    private String charsetDetect(String filePath)	{
    	Path path = Paths.get(filePath);
    	CharsetDetector charDetect = new CharsetDetector();
    	try {
    		charDetect.setText(Files.readAllBytes(path));	
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		String charSet = charDetect.detect().getName();
		logger.info("charset: " + charSet);
		return charSet;
    }

    
    public String parseToHTML(String filePath) throws IOException, SAXException, TikaException {
        ContentHandler handler = new ToXMLContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        InputStream is = new FileInputStream(filePath);
        parser.parse(is, handler, metadata);
        return handler.toString();
    }	
    
    public ContentHandler extractImg(byte[] content, final String path, String filename)throws IOException, SAXException, TikaException{           
    	
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        ContentHandler handler =   new ToXMLContentHandler();
        PDFParser parser = new PDFParser(); 

        PDFParserConfig config = new PDFParserConfig();
        config.setExtractInlineImages(true);
        config.setExtractUniqueInlineImagesOnly(true);

        parser.setPDFParserConfig(config);


        EmbeddedDocumentExtractor embeddedDocumentExtractor = 
                new EmbeddedDocumentExtractor() {
            @Override
            public boolean shouldParseEmbedded(Metadata metadata) {
                return true;
            }
            @Override
            public void parseEmbedded(InputStream stream, ContentHandler handler, Metadata metadata, boolean outputHtml)
                    throws SAXException, IOException {
                Path outputFile = new File(path + metadata.get(Metadata.RESOURCE_NAME_KEY)).toPath();
                Files.copy(stream, outputFile);
            }
        };

        context.set(PDFParser.class, parser);
        context.set(EmbeddedDocumentExtractor.class,embeddedDocumentExtractor );

        try (InputStream stream = new ByteArrayInputStream(content)) {
            parser.parse(stream, handler, metadata, context);
        }

        return handler;
    }
    
}
