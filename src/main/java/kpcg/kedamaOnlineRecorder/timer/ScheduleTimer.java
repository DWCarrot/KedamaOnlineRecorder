package kpcg.kedamaOnlineRecorder.timer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ScheduleTimer implements Runnable {

	public final class ScheduleTask {

		private ScheduleTask() {
			
		}
		
		private long target = 0;

		private long interval = 0;
		
		private Runnable task = null;
		
		public Runnable cancel() {
			if(tasklist.remove(this))
				return this.task;
			return null;
		}
	}
	
	
	public static final Comparator<ScheduleTask> comparator = new Comparator<ScheduleTask>() {
		@Override
		public int compare(ScheduleTask o1, ScheduleTask o2) {
			long diff = o1.target - o2.target;
			if(diff > 0)
				return 1;
			if(diff < 0)
				return -1;
			return 0;
		}
	};
	
	private static final ZoneOffset offset = OffsetDateTime.now().getOffset();
	
	
	private Executor executor;
	
	private SortedSet<ScheduleTask> tasklist;
	
	private AtomicLong target;
	
	private long checkpoint;
	
	private long step;
	
	private Thread t;
	
	public ScheduleTimer() {
		
		tasklist = new TreeSet<>(comparator);
	}
	
	
	public void synchronize(LocalDateTime begin, long step, TimeUnit uint) {
		if(uint == TimeUnit.NANOSECONDS)
			throw new IllegalArgumentException("Accuracy can not reach " + TimeUnit.NANOSECONDS);
		checkpoint = begin.toInstant(offset).toEpochMilli();
		this.step = uint.toMillis(step);
	}
	
	public Set<Runnable> stop() {
		Set<Runnable> res = null;
		if(t != null) {
			t.interrupt();
			res = new HashSet<>();
			tasklist = new TreeSet<>(comparator);
		}
		return res;
	}
	
	public void schedule(Runnable task, long delay, TimeUnit uint) {
		long real = System.currentTimeMillis();
		synchronized (tasklist) {
			ScheduleTask t = new ScheduleTask();
			t.target = real + uint.toMillis(delay);
			t.task = task;
			tasklist.add(t);
			target.set(t.target);
			tasklist.notify();
		}
	}
	
	public void schedule(Runnable task, long delay, TimeUnit delayUint, long interval, TimeUnit intervalUint) {
		long real = System.currentTimeMillis();
		synchronized (tasklist) {
			ScheduleTask t = new ScheduleTask();
			t.target = real + delayUint.toMillis(delay);
			t.interval = intervalUint.toMillis(interval);
			t.task = task;
			tasklist.add(t);
			target.set(t.target);
			tasklist.notify();
		}
	}
	
	@Override
	public void run() {
		ScheduleTask task;
		long targetV;
		long dt;
		long real;
		t = Thread.currentThread();
		target.set(Long.MAX_VALUE);
		while(!t.isInterrupted()) {
			try {
				synchronized (tasklist) {
					targetV = target.get();					
					if(checkpoint < targetV) {
						dt = checkpoint - System.currentTimeMillis();
					} else {
						dt = targetV - System.currentTimeMillis();
					}
					if(dt > 0)
						tasklist.wait(dt);					
					real = System.currentTimeMillis();
					if(checkpoint <= real) {
						check(checkpoint);
						checkpoint += (((real - checkpoint) / step + 1) * step);
					}
					task = tasklist.first();
					if(task != null && task.target <= real) {
						if(task.interval > 0)
							task.target += task.interval;		
						else
							tasklist.remove(task);
						executor.execute(task.task);
						task = tasklist.first();
						if(task != null) {
							target.set(task.target);
						} else {
							target.set(Long.MAX_VALUE);
						}
					}					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		t = null;
	}
	
	protected void check(long checkpoint) {
		
	}
	
	
	
	
}
