package introsde.rest.ehealth.model;

import introsde.rest.ehealth.dao.LifeCoachDao;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "Person", propOrder = {"idPerson", "firstname", "lastname", "birthdate", "healthProfile"})
@Entity 
@Table(name="Person")
@NamedQuery(name="Person.findAll", query="SELECT p FROM Person p")
@XmlRootElement
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id 
    @GeneratedValue(generator="sqlite_person")
    @TableGenerator(name="sqlite_person", table="sqlite_sequence",
        pkColumnName="name", valueColumnName="seq",
        pkColumnValue="Person")
    @Column(name="idPerson")

    private int idPerson;
    @Column(name="lastname")

    private String lastname;
    @Column(name="firstname")

    private String firstname;
    @Column(name="birthdate")

    private String birthdate; 
    
    // mappedBy must be equal to the name of the attribute in Measure that maps this relation
    @OneToMany(mappedBy="person",cascade=CascadeType.ALL,fetch=FetchType.EAGER)
    private List<Measure> measure;
    
    // mappedBy must be equal to the name of the attribute in HealthProfile that maps this relation
    @OneToOne(mappedBy="person",cascade=CascadeType.ALL,fetch=FetchType.EAGER)
    private HealthProfile healthProfile;
    
    /*                  GETTING INFO                */
    public int getIdPerson(){
        return idPerson;
    }

    public String getLastname(){
        return lastname;
    }
    public String getFirstname(){
        return firstname;
    }
    public String getBirthdate(){
        return birthdate;
    }
    
    @XmlTransient
    public List<Measure> getMeasure() {
        return measure;
    }
    
    public HealthProfile getHealthProfile() {
    	return healthProfile;
    }
    
    /*                  SETTING INFO                */
    public void setIdPerson(int idPerson){
        this.idPerson = idPerson;
    }
    public void setLastname(String lastname){
        this.lastname = lastname;
    }
    public void setFirstname(String firstname){
        this.firstname = firstname;
    }
    public void setBirthdate(String birthdate){
        this.birthdate = birthdate;
    }
    
    public void setHealthProfile(HealthProfile healthProfile) {
    	this.healthProfile = healthProfile;
    }
    
    public void setMeasure(List<Measure> measure) {
        this.measure = measure;
    }
    
    
    public static Person getPersonById(int personId) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        Person p = em.find(Person.class, personId);
        LifeCoachDao.instance.closeConnections(em);
        return p;
    }

    public static List<Person> getAll() {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        List<Person> list = em.createNamedQuery("Person.findAll", Person.class)
            .getResultList();
        LifeCoachDao.instance.closeConnections(em);
        return list;
    }
    
    public static List<Person> getUsingQueryParams(String measureType, int max, int min) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        List<Person> filteredList = new ArrayList<Person>();
        List<Person> list = em.createNamedQuery("Person.findAll", Person.class).getResultList();
        LifeCoachDao.instance.closeConnections(em);
        
    	for (int i=0; i<list.size(); i++) {
    		Person p = list.get(i);
    		List<Measure> measures = p.getHealthProfile().getMeasureType();
    		for (Measure measure: measures) {
    			if (measure.getMeasure().equals(measureType)) {
    				if (min == 0 || measure.getValue() >= min) {
    					if (max == 0 || measure.getValue() <= max) {
    						filteredList.add(p);
    					}
    				}
    			}
    		}
    	}
        return filteredList;
    }

    public static Person savePerson(Person p) {
    	// When saving person, link person to created healthProfile (if not null)
    	if (p.getHealthProfile() != null) {
	    	HealthProfile hp = p.getHealthProfile();
	    	hp.setPerson(p);
	    	for (Measure measure: hp.getAllMeasures()) {
	    		measure.setPerson(p);
	    		measure.setHealthProfile(hp);
	    		measure.setCreated(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	    	}
    	}
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return p;
    } 

    public static Person updatePerson(Person p) {
        EntityManager em = LifeCoachDao.instance.createEntityManager(); 
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        p=em.merge(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return p;
    }

    public static void removePerson(Person p) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        p=em.merge(p);
        em.remove(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
    }
    
}