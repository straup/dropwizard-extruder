package info.aaronland.extruder;

import info.aaronland.extruder.Upload;
import info.aaronland.extruder.Document;
import info.aaronland.extruder.DocumentView;

import java.io.InputStream;
import java.io.File;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.MediaType;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.l3s.boilerpipe.extractors.DefaultExtractor;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

@Path(value = "/boilerpipe")
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
public class BoilerpipeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoilerpipeResource.class);

    @GET
    public Response extrudeThisURL(@QueryParam("url") String url){

	Document doc;
	DocumentView view;

	try {
	    doc = extrudeThis(url);
	    view = new DocumentView(doc);
	}

	// TODO: trap MalformedURLExceptions and return NOT_ACCEPTABLE here (20130901/straup)

	catch (Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
	}

	return Response.status(Response.Status.OK).entity(view).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response extrudeThisFile(@FormDataParam("file") InputStream input){

	Upload upload = new Upload();
	File tmpfile = upload.writeTmpFile(input);

	String uri = "file://" + tmpfile.getAbsolutePath();

	Document doc;
	DocumentView view;

	try {
	    doc = extrudeThis(uri);
	    view = new DocumentView(doc);
	}

	catch (Exception e){
	    tmpfile.delete();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
	}

	tmpfile.delete();

	return Response.status(Response.Status.OK).entity(view).build();
    }

    private Document extrudeThis(String uri){

	URL url;
	String text;

	try {
	    url = new URL(uri);
	}

	catch (Exception e){
	    throw new RuntimeException(e);
	}

	try {
	    text = ArticleExtractor.INSTANCE.getText(url);
	}

	catch (Exception e){
	    throw new RuntimeException(e);
	}

	Document doc = new Document(text);
	return doc;
    }

}
