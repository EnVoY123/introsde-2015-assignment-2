package introsde.rest.ehealth.model;

import introsde.rest.ehealth.dao.LifeCoachDao;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity  
@Table(name="HealthProfile") 
@NamedQuery(name="HealthProfile.findAll", query="SELECT hp FROM HealthProfile hp")
@XmlRootElement
public class HealthProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id 
    @GeneratedValue(generator="sqlite_healthprofile")
    @TableGenerator(name="sqlite_healthprofile", table="sqlite_sequence",
        pkColumnName="name", valueColumnName="seq",
        pkColumnValue="HealthProfile")
    
    @Column(name="idHealthProfile")
    private int idHealthProfile;

	@OneToOne
	@JoinColumn(name="idPerson",referencedColumnName="idPerson")
	private Person person;
	
    // mappedBy must be equal to the name of the attribute in Measure that maps this relation
    @OneToMany(mappedBy="healthProfile",cascade=CascadeType.ALL)
    private List<Measure> measure;
    

    /*                  GETTING INFO                */
    @XmlTransient
    public int getIdHealthProfile(){
        return idHealthProfile;
    }
    
    @XmlTransient
    public Person getPerson() {
    	return person;
    }
    
    // Get current measures for healthprofile
    public List<Measure> getMeasureType() {
    	if (measure == null) {
    		return measure;
    	}
    	DateFormat df = new SimpleDateFormat("yyyy-MM-DD"); 
    	List<Measure> currentMeasures = new ArrayList<Measure>();
    	// Loop through measures
    	for (int i = 0; i < measure.size(); i++) {
    		boolean currentMeasure = true;
    			// Loop through current measures
    		   for (int k = 0; k < currentMeasures.size(); k++) {
    			  // If same measureType already exist in current measures
    		      if(measure.get(i).getMeasure().equals(currentMeasures.get(k).getMeasure())) {
    		    	  try {
    		    		//  Compare creation dates and
						if (df.parse(measure.get(i).getCreated()).after(df.parse(currentMeasures.get(k).getCreated()))) {
							// If measure is newer than current one, remove the current one  
							currentMeasures.remove(k);
						  } else {
							 // Else set measure as not current 
							  currentMeasure = false;
						  }
					} catch (ParseException e) {
						e.printStackTrace();
					}
    		      }
    		   }
    		   // If measure is current measure, add to list
    		   if (currentMeasure) {
    			   currentMeasures.add(measure.get(i));
    		   }
    	}
        return currentMeasures;
    }
    
    @XmlTransient
    public List<Measure> getAllMeasures() {
    	return measure;
    }
    
    /*                  SETTING INFO                */
    public void setIdHealthProfile(int idHealthProfile){
        this.idHealthProfile = idHealthProfile;
    }
    
    public void setPerson(Person person) {
    	this.person = person;
    }
    
    public void setMeasureType(List<Measure> measure) {
    	this.measure = measure;
    }
    
    
    public static HealthProfile getHealthProfileById(int healthProfileId) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        HealthProfile hp = em.find(HealthProfile.class, healthProfileId);
        LifeCoachDao.instance.closeConnections(em);
        return hp;
    }

    public static List<HealthProfile> getAll() {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        List<HealthProfile> list = em.createNamedQuery("HealthProfile.findAll", HealthProfile.class)
            .getResultList();
        LifeCoachDao.instance.closeConnections(em);
        return list;
    }

    public static HealthProfile saveHealthProfile(HealthProfile hp) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(hp);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return hp;
    } 

    public static HealthProfile updateHealthProfile(HealthProfile hp) {
        EntityManager em = LifeCoachDao.instance.createEntityManager(); 
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        hp=em.merge(hp);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return hp;
    }

    public static void removeHealthProfile(HealthProfile hp) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        hp=em.merge(hp);
        em.remove(hp);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
    }
    
}