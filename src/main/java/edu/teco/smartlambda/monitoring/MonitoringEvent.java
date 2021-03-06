package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.AbstractLambda;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Calendar;

/**
 * A data class for monitoring information
 */
@Entity
@Table(name = "MonitoringEvent")
public class MonitoringEvent {
	
	@Temporal(TemporalType.DATE)
	@Getter
	@Setter
	private Calendar            time;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lambdaOwner")
	@Getter
	private User                lambdaOwner;
	@Getter
	private String              lambdaName;
	@Getter
	@Setter
	private long                duration;
	@Getter
	@Setter
	private long                CPUTime;
	@Getter
	@Setter
	private String              error;
	@Getter
	@Enumerated(EnumType.STRING)
	private MonitoringEventType type;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "key")
	@Getter
	private Key                 key;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int                 id;
	
	public MonitoringEvent() {}
	
	public MonitoringEvent(final AbstractLambda lambda, final MonitoringEventType type, final Key key) {
		
		this.time = Calendar.getInstance();
		this.lambdaOwner = lambda.getOwner();
		this.lambdaName = lambda.getName();
		this.type = type;
		this.key = key;
	}
	
	/**
	 * Saves the event to the database
	 */
	public void save() {
		Application.getInstance().getSessionFactory().getCurrentSession().save(this);
	}
	
	enum MonitoringEventType {
		EXECUTION, DELETION, DEPLOYMENT
	}
}


