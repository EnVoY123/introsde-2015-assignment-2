package introsde.rest.ehealth.resources;

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
public class PersonResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    int id;

    EntityManager entityManager; // only used if the application is deployed in a Java EE container

    public PersonResource(UriInfo uriInfo, Request request,int id, EntityManager em) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.entityManager = em;
    }

    public PersonResource(UriInfo uriInfo, Request request,int id) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
    }

    // Application integration
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Person getPerson() {
        Person person = Person.getPersonById(id);
        if (person == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return person;
    }

    // for the browser
    @GET
    @Produces(MediaType.TEXT_XML)
    public Person getPersonHTML() {
        Person person = Person.getPersonById(id);
        if (person == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return person;
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Person putPerson(Person person) {
        Person existing = Person.getPersonById(this.id);
        if (existing == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
        	// Set missing attributes for person
            person.setIdPerson(this.id);
            if (person.getFirstname() == null) {
            	person.setFirstname(existing.getFirstname());
            }
            if (person.getLastname() == null) {
            	person.setLastname(existing.getLastname());
            }
            if (person.getBirthdate() == null) {
            	person.setBirthdate(existing.getBirthdate());
            }
            if (person.getHealthProfile() == null) {
            	person.setHealthProfile(existing.getHealthProfile());
            }
            Person.updatePerson(person);
        }
        return person;
    }

    @DELETE
    public void deletePerson() {
        Person p = Person.getPersonById(id);
        if (p == null) {
        	throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Person.removePerson(p);
    }
}