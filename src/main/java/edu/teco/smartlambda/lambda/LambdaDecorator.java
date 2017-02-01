package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.schedule.Event;

import java.util.Optional;
import java.util.List;

/**
 * Created by Melanie on 01.02.2017.
 */
public abstract class LambdaDecorator extends AbstractLambda {
	protected AbstractLambda lambda;
	
	public Optional<String> execute(String params, boolean async) {
		return null;
	}
	public void save() {}
	public void update() {}
	public void delete() {}
	public void schedule(Event event) {}
	public void deployBinary(byte[] content) {}
	public Event getScheduledEvent() {
		return null;
	}
	public List<Event> getScheduledEvents() {
		return null;
	}
	public List<MonitoringEvent> getMonitoringEvents() {
		return null;
	}
	public String getName() {
		return null;
	}
	public User getOwner() {
		return null;
	}
	public boolean getAsync() {
		return false;
	}
	public Runtime getRuntime() {
		return null;
	}
	public void setName(String name) {}
	public void setOwner(User owner) {}
	public void setAsync(boolean async) {}
}
