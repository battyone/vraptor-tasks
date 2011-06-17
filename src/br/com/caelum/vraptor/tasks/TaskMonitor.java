package br.com.caelum.vraptor.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@Component
@ApplicationScoped
public class TaskMonitor implements JobListener {

	private static Logger logger = LoggerFactory.getLogger(TaskMonitor.class);
	private Map<String, TaskStatistics> statistics = new HashMap<String, TaskStatistics>();

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		logger.debug("Task " + taskName(context) + " was vetoed");
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		logger.debug("Executing Task " + taskName(context));
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {

		logger.debug("Task " + taskName(context) + " was executed");

		TaskStatistics stats = getStatisticsFor(taskName(context));
		stats.update(context);

		if(exception != null){
			stats.increaseFailCount();
			logger.debug("Task " + taskName(context) + " failed", exception);
		}

	}

	private String taskName(JobExecutionContext context){
		return context.getJobDetail().getKey().getName();
	}

	private TaskStatistics getStatisticsFor(String taskName){
		if(!statistics.containsKey(taskName))
			statistics.put(taskName, new TaskStatistics(taskName));
		return statistics.get(taskName);
	}

	public TaskStatistics getStatisticsFor(Task task){
		return statistics.get(task.getClass().getName());
	}

	public Collection<TaskStatistics> getStatistics(){
		return statistics.values();
	}


}
