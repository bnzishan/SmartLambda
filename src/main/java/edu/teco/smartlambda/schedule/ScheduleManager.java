package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import org.hibernate.Session;

import javax.persistence.LockModeType;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * Created by Melanie on 01.03.2017.
 */
public class ScheduleManager extends Thread {
	private static ScheduleManager instance;
	
	public static ScheduleManager getInstance() {
		if (instance == null) {
			instance = new ScheduleManager();
		}
		return instance;
	}
	
	private void ScheduleManager() {}
	
	public void run() {
		Event                                                                        event;
		List<AbstractMap.SimpleEntry<Event, ListenableFuture<ExecutionReturnValue>>> futures = new LinkedList<>();
		while (true) {
			for (AbstractMap.SimpleEntry<Event, ListenableFuture<ExecutionReturnValue>> future : futures) {
				if (!future.getValue().isDone()) {
					future.getKey().setLock(Calendar.getInstance());
					Application.getInstance().getSessionFactory().getCurrentSession().update(future.getKey());
				} else {
					future.getKey().save();
					futures.remove(future);
				}
			}
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.beginTransaction();
			Event    query         = from(Event.class);
			Calendar lockTolerance = Calendar.getInstance();
			lockTolerance.add(Calendar.MINUTE, -2);
			where(query.getNextExecution()).lte(Calendar.getInstance()).and(query.getLock()).isNull().or(query.getLock())
					.lte(lockTolerance);
			event = select(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1).get(session).orElse(null);
			if (event != null) {
				event.setLock(Calendar.getInstance());
				session.update(event);
				session.getTransaction().commit();
				futures.add(new AbstractMap.SimpleEntry<>(event, event.execute()));
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
	}
}
