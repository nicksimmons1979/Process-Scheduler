//*****************************************************************************************************************************
// PWP.java - August 10 2016
// Nick Simmons T00033019
// CPU Scheduling Priority with preemption
//*****************************************************************************************************************************

import java.util.ArrayList;
import java.util.List;

public class PWP
{
	public static void main(String args[])
	{
		PriorityQueue readyQ = new PriorityQueue();
		ProcessControlBlock pcb = null;
		ProcessControlBlock cpu = null;
		SampleReader sr;
		String sampleFile;
		int process = 0, arrival = 0, priority = 0, burst = 0;
		
		final int SIMULATION_TIME = 7200000; // 2 hrs
		
		// for computations of averages
		long averageTurnAroundTime = 0;
		long averageWaitingTime = 0;
		int cpuLoadTime = 0; // use as countdown timer
		int processCount = 0; // completed processes
		int processLoaded = 0; // for waiting time
		final int AVERAGE_INTERVAL = 60000; // 1 min
		final int DISPLAY_INTERVAL = 600000; // 10 min
		List<Integer> previousTAT = new ArrayList<Integer>();
		List<Integer> previousWT = new ArrayList<Integer>();

		System.out.print("Priority with Pre-emption Algorithm\n");
		
		// load simulation file
		sampleFile = "simulation";
		sr = new SampleReader(sampleFile);

		// for loop, run for total simulation time, execute in milliseconds
		for (int currentTime = 0; currentTime < SIMULATION_TIME; currentTime++)
		{		
			// compute averages every AVERAGE_INTERVAL
			if ((currentTime % AVERAGE_INTERVAL == 0) && (processCount > 0) && (processLoaded > 0))
			{			
				//remove n units of WT and TAT time from totals.
				if (currentTime > DISPLAY_INTERVAL)
				{
					for (int index = 0; index < processCount; index++)
						previousTAT.remove(0);
					
					for (int index = 0; index < processLoaded; index++)
						previousWT.remove(0);							
				}
				
				// sum all TAT for previous interval
				for (int index = previousTAT.size() - 1; index >= 0; index--)
				{
				averageTurnAroundTime += previousTAT.get(index);

				}
		
				// sum all WT for previous interval
				for (int index = previousWT.size() - 1; index >= 0; index--)
				{
					averageWaitingTime += previousWT.get(index);
				}
			
				// compute averages
				averageTurnAroundTime = averageTurnAroundTime / previousTAT.size();
				averageWaitingTime = averageWaitingTime / previousWT.size();
				
				// output every 10 minutes
				if (currentTime % DISPLAY_INTERVAL == 0)
					System.out.println("Minute: " + currentTime / AVERAGE_INTERVAL  + " - Average TAT: " + averageTurnAroundTime + " - Average WT: " + averageWaitingTime);
				
				//reset counters
				processCount = 0;
				processLoaded = 0;
				averageTurnAroundTime = 0;
				averageWaitingTime = 0;
			}

			// if queue is empty, read process from simulation file
			if (pcb == null)
			{
				// load process
				process = sr.readProcess();
				if (process < 0)
					break;
				arrival = sr.readArrival();
				if (arrival < 0)
					break;
				priority = sr.readPriority();
				if (priority < 0)
					break;
				burst = sr.readBurst();
				if (burst < 0)
					break;
				
				// create process control block
				pcb = new ProcessControlBlock(process, arrival, priority, burst);
			}
		
			// When arrival time == current time, load a process to the ready queue
			if (pcb.getArrivalTime() <= currentTime)
			{
				readyQ.putQueue(pcb, pcb.getPriority());
				pcb = null;
			}
		
			// if there is a process running on the cpu
			if (cpu != null)
			{
				cpu.setcpuBurstTime(cpu.getCpuBurstTime() - 1);

				// if cpu has lower priority then queue head, return cpu to queue
				if ((cpu.getPriority() > readyQ.getHighestPriority()) && (readyQ.getHighestPriority() != -1))
				{
					// retain "context" remaining burst time, otherwise process will restart at initial instruction every loading					
					// set new arrival time
					cpu.setArrivalTime(currentTime);
					
					// save context
					readyQ.putQueue(cpu, cpu.getPriority());
					
					// load head to cpu
					cpu = (ProcessControlBlock) readyQ.getQueue();
					cpuLoadTime = currentTime;

					// time waiting on queue
					previousWT.add(cpuLoadTime - cpu.getArrivalTime());
					processLoaded++;
				}
				
				// if process on cpu is finished
				if (cpu.getCpuBurstTime() <= 0)
				{
					// compute turn around time for single process
					previousTAT.add(cpuLoadTime + cpu.getCpuBurstTime() - cpu.getArrivalTime());  
					processCount++;
					
					// load next process if one exists, else wait
					if (!readyQ.isEmpty())
					{
						// load head to cpu
						cpu = (ProcessControlBlock) readyQ.getQueue();
						cpuLoadTime = currentTime;

						// time waiting on queue
						previousWT.add(cpuLoadTime - cpu.getArrivalTime());
						processLoaded++;
					}
					
					// cpu idle, queue is empty
					else
						cpu = null;	
				}	
			}
						
			// if the cpu is idle
			else
			{
				if (!readyQ.isEmpty())
				{
					// get process from queue and dispatch to the cpu
					cpu = (ProcessControlBlock) readyQ.getQueue();
					cpuLoadTime = currentTime;
					
					// time waiting on queue
					previousWT.add(cpuLoadTime - cpu.getArrivalTime());
					processLoaded++;
				}
			}
		}
	}
}