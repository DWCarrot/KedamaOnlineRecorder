package kpcg.kedamaOnlineRecorder.timer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class ScheduleTimer implements Runnable {

	public final class ScheduledTaskFuture implements ScheduledFuture<Void>, Runnable {

		private ScheduledTaskFuture() {
			
		}
		
		private void set1(Runnable task, long delay) {
			this.task = task;
			this.delay = delay;
			this.interval = 0;
			this.target = System.currentTimeMillis() + this.delay;
			this.canceled = false;
			this.done = false;
		}
		
		private void set1(Runnable task, long delay, long interval) {
			this.task = task;
			this.delay = delay;
			this.interval = interval;
			this.target = System.currentTimeMillis() + this.delay;
			this.canceled = false;
			this.done = false;
		}
		
		private void set2(Runnable task, long target) {
			this.task = task;
			this.interval = 0;
			this.target = target;
			this.delay = this.target - System.currentTimeMillis();
			this.canceled = false;
			this.done = false;
		}
		
		private void set2(Runnable task, long target, long interval) {
			this.task = task;
			this.interval = interval;
			this.target = target;
			this.delay = this.target - System.currentTimeMillis();
			this.canceled = false;
			this.done = false;
		}
		
		private long delay = 0;
		
		private long target = 0;

		private long interval = 0;
		
		private boolean canceled = false;
		
		private boolean done = false;
		
		private Runnable task = null;
		
		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(delay, TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o) {
			long dt = delay - o.getDelay(TimeUnit.MILLISECONDS);
			if(dt > 0)
				return 1;
			if(dt < 0)
				return -1;
			return 0;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return canceled = tasklist.remove(this);
		}

		@Override
		public boolean isCancelled() {
			return canceled;
		}

		@Override
		public boolean isDone() {
			return done;
		}

		@Override
		public Void get() throws InterruptedException, ExecutionException {
			synchronized (task) {
				task.wait();
			}
			return null;
		}

		@Override
		public Void get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			synchronized (task) {
				task.wait(unit.toMillis(timeout));
			}
			return null;
		}

		@Override
		public void run() {
			synchronized (task) {
				task.run();
				if(interval == 0) {
					done = true;
					task.notify();
				}
			}
		}
	}
	
	
	public static final Comparator<ScheduledTaskFuture> comparator = new Comparator<ScheduledTaskFuture>() {
		@Override
		public int compare(ScheduledTaskFuture o1, ScheduledTaskFuture o2) {
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
	
	private SortedSet<ScheduledTaskFuture> tasklist;
	
	private AtomicLong target;
	
	private long checkpoint;
	
	private long step;
	
	private Thread t;
	
	public ScheduleTimer() {
		
		tasklist = new TreeSet<>(comparator);
	}
	
	
	public void synchronize(LocalDateTime begin, long step, TimeUnit uint) {
		checkpoint = begin.toInstant(offset).toEpochMilli();
		this.step = uint.toMillis(step);
	}
	
	public void stop() {
		if(t != null) {
			t.interrupt();
			for(ScheduledTaskFuture f : tasklist)
				f.canceled = true;
			tasklist = new TreeSet<>(comparator);
		}
	}
	
	public void schedule(Runnable task, long delay, TimeUnit uint) {
		ScheduledTaskFuture t = new ScheduledTaskFuture();
		t.set1(task, uint.toMillis(delay));
		synchronized (tasklist) {
			this.tasklist.add(t);
			this.target.set(t.target);
			this.tasklist.notify();
		}
	}
	
	public void schedule(Runnable task, long delay, TimeUnit delayUint, long interval, TimeUnit intervalUint) {
		ScheduledTaskFuture t = new ScheduledTaskFuture();
		t.set1(task, delayUint.toMillis(delay), intervalUint.toMillis(interval));
		synchronized (tasklist) {
			this.tasklist.add(t);
			this.target.set(t.target);
			this.tasklist.notify();
		}
	}
	
	public void schedule(Runnable task, LocalDateTime target) {
		ScheduledTaskFuture t = new ScheduledTaskFuture();
		t.set2(task, target.toInstant(offset).toEpochMilli());
		synchronized (tasklist) {
			this.tasklist.add(t);
			this.target.set(t.target);
			this.tasklist.notify();
		}
	}
	
	public void schedule(Runnable task, LocalDateTime target, long interval, TimeUnit intervalUint) {
		ScheduledTaskFuture t = new ScheduledTaskFuture();
		t.set2(task, target.toInstant(offset).toEpochMilli(), intervalUint.toMillis(interval));
		synchronized (tasklist) {
			this.tasklist.add(t);
			this.target.set(t.target);
			this.tasklist.notify();
		}
	}
	
	@Override
	public void run() {
		ScheduledTaskFuture task;
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
						executor.execute(task);
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
