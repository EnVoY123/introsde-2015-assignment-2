package introsde.rest.ehealth.resources;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import introsde.rest.ehealth.model.Measure;
import introsde.rest.ehealth.model.Person;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless // only used if the the application is deployed in a Java EE container
@LocalBean // only used if the the application is deployed in a Java EE container
public class MeasureHistoryResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    int id;
    String measure;
    String beforeDate;
    String afterDate;

    EntityManager entityManager; // only used if the application is deployed in a Java EE container

    public MeasureHistoryResource(UriInfo uriInfo, Request request,int id, String measure, EntityManager em) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.measure = measure;
        this.entityManager = em;
    }

    public MeasureHistoryResource(UriInfo uriInfo, Request request,int id, String measure) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.measure = measure;
    }
    
    public MeasureHistoryResource(UriInfo uriInfo, Request request,int id, String measure, String beforeDate, String afterDate) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.measure = measure;
        this.beforeDate = beforeDate;
        this.afterDate = afterDate;
    }

    // Application integration
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Measure> getHealthMeasureHistory(@QueryParam("before") String beforeDate, @QueryParam("after") String afterDate) {
    	List<Measure> measureHistory = Measure.getMeasureHistory(id, measure, beforeDate, afterDate);
        if (measureHistory == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return measureHistory;
    }

    // for the browser
    @GET
    @Produces(MediaType.TEXT_XML)
    public List<Measure> getHealthMeasureHistoryHTML(@QueryParam("before") String beforeDate, @QueryParam("after") String afterDate) {
    	List<Measure> measureHistory = Measure.getMeasureHistory(id, measure, beforeDate, afterDate);
        if (measureHistory == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return measureHistory;
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    public Measure newMeasure(Measure measureType) throws IOException, ParseException {
    	// Link person and healthprofile to measure and add missing attributes
        Person person = Person.getPersonById(this.id);
        measureType.setPerson(person);
        measureType.setHealthProfile(person.getHealthProfile());
        measureType.setMeasure(this.measure);
        if (measureType.getCreated() == null) {
        	measureType.setCreated(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
        return Measure.saveMeasure(measureType);
    }
    
}