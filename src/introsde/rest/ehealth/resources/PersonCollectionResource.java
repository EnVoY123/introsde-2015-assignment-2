package introsde.rest.ehealth.resources;

import introsde.rest.ehealth.model.Person;
import java.io.IOException;
import java.util.List;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless // will work only inside a Java EE application
@LocalBean // will work only inside a Java EE application
@Path("/person")
public class PersonCollectionResource {

    // Allows to insert contextual objects into the class,
    // e.g. ServletContext, Request, Response, UriInfo
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    // will work only inside a Java EE application
    @PersistenceUnit(unitName="introsde-jpa")
    EntityManager entityManager;

    // will work only inside a Java EE application
    @PersistenceContext(unitName = "introsde-jpa",type=PersistenceContextType.TRANSACTION)
    private EntityManagerFactory entityManagerFactory;

    // Return the list of people to the user in the browser
    @GET
    @Produces({MediaType.TEXT_XML,  MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML })
    public List<Person> getPersonsBrowser(@QueryParam("measureType") String measureType, @QueryParam("max") int max, @QueryParam("min") int min) {
        List<Person> people = null;
        // If no query params, return all
        if (measureType == null && max == 0 && min == 0) {
        	people = Person.getAll();
        // With query params return perople taking parameters into account
        } else {
        	people = Person.getUsingQueryParams(measureType, max, min);
        }
        if (people == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return people;
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    public Person newPerson(Person person) throws IOException {           
        return Person.savePerson(person);
    }
    

    // Defines the resource handlers for other paths
    @Path("{personId}")
    public PersonResource getPerson(@PathParam("personId") int id) {
        return new PersonResource(uriInfo, request, id);
    }
    
    @Path("{personId}/{measureType}")
    public MeasureHistoryResource getMeasureHistory(@PathParam("personId") int personId, @PathParam("measureType") String measure) {
        return new MeasureHistoryResource(uriInfo, request, personId, measure);
    }
    
    @Path("{personId}/{measureType}/{mid}")
    public MeasureResource getMeasureHistory(@PathParam("personId") int personId, @PathParam("measureType") String measure, @PathParam("mid") int mid) {
        return new MeasureResource(uriInfo, request, personId, measure, mid);
    }
}