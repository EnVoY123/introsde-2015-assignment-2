package introsde.rest.ehealth.resources;

import introsde.rest.ehealth.model.Measure;
import introsde.rest.ehealth.model.Person;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless // only used if the the application is deployed in a Java EE container
@LocalBean // only used if the the application is deployed in a Java EE container
public class MeasureResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    int personId;
    String measure;
    int mid;

    EntityManager entityManager; // only used if the application is deployed in a Java EE container

    public MeasureResource(UriInfo uriInfo, Request request,int personId, String measure, int mid, EntityManager em) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.personId = personId;
        this.measure = measure;
        this.entityManager = em;
        this.mid = mid;
    }

    public MeasureResource(UriInfo uriInfo, Request request,int personId, String measure, int mid) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.personId = personId;
        this.measure = measure;
        this.mid = mid;
    }

    // Application integration
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Measure getMeasure() {
    	Measure measureType = this.getMeasureById(mid);
        if (measureType == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return measureType;
    }

    // for the browser
    @GET
    @Produces(MediaType.TEXT_XML)
    public Measure getMeasureHTML() {
    	Measure measureType = this.getMeasureById(mid);
        if (measureType == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return measureType;
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Measure putMeasure(Measure measureType) {
        System.out.println("--> Updating Measure ... " +this.mid);
        System.out.println("--> "+measureType.toString());
        Measure existing = this.getMeasureById(this.mid);
        if (existing == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
        	// Set missing attributes
            measureType.setMid(this.mid);
            Person person = Person.getPersonById(this.personId);
            measureType.setPerson(person);
            measureType.setHealthProfile(person.getHealthProfile());
            measureType.setMeasure(this.measure);
            if (measureType.getCreated() == null) {
            	measureType.setCreated(existing.getCreated());
            }
            if (measureType.getValue() == 0) {
            	measureType.setValue(existing.getValue());
            }
            Measure.updateMeasure(measureType);
        }
        return measureType;
    }

    @DELETE
    public void deleteMeasureType() {
    	Measure m = this.getMeasureById(mid);
        if (m == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Measure.removeMeasure(m);
    }

    public Measure getMeasureById(int mid) {
        Measure measureType = Measure.getMeasureById(mid);
        // Check if the requested measure fits with the uri path attributes
        if (measureType.getMeasure().equals(measure) && measureType.getPerson().getIdPerson() == personId) {
            return measureType;
        } else {
        	System.out.println("Could not find measure " + measure + " with id " + mid);
        	return null;
        }
    }
}