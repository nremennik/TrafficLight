package st.notexi.trafficControl;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Crossing
{

    private Map<String, Road> roads = new HashMap<>();

    // Possible states - array of roads with green light
    // Each state is an array of Roads that have green light currently set
    private List<List<Road>> greenLights = new ArrayList<>();
    private AtomicInteger currentGreenLight = new AtomicInteger(0);
    static final int GREEN_DURATION = 20000; // Green light duration (millis)
    static final int GREEN_DURATION_QUANTUM_DELAY = 100; // Green light duration granularity in millis
    private AtomicLong lastCrossedCars = new AtomicLong(0);

    public Crossing()
    {
    }

    public void addRoad(Road road) // Add road to the crossing
    {
        roads.put(road.getName(), road);
    }

    public long getLastCrossedCars()
    {
        // for rest crossed cars
        long res = lastCrossedCars.get();
        return (lastCrossedCars.get());
    }

    public long getWaitingOnRedCars()
    {
        // for rest waiting cars
        long res = 0;
        int currentGreen = currentGreenLight.get();

        for (Road road : roads.values())
        {
            if (!greenLights.get(currentGreen).contains(road))
            {
                res += road.getOnTheCrossing();
            }
        }
        return (res);
    }

    public List<String> getCurrentlyGreenRoadsNames()
    {
        List<String> greenRoadNames = new ArrayList<>();
        List<Road> greenRoads = greenLights.get(currentGreenLight.get());

        greenLights.get(currentGreenLight.get()).forEach(road -> greenRoadNames.add(road.getName()));
        return (greenRoadNames);
    }

    public List<String> getRoadsNames()
    {
        return (new ArrayList<String>(roads.keySet()));
    }

    // Set possible states by road names. Road shall be already added to the crossing
    public void setAllowedStates(String[][] roadsStates)
    {
        greenLights.clear();
        for (int i = 0; i < roadsStates.length; i++)
        {
            List<Road> state = new ArrayList<>();

            for (int j = 0; j < roadsStates[i].length; j++)
            {
                Road road = roads.get(roadsStates[i][j]);
                if (road == null)
                {
                    throw new RuntimeException("Crossing::setAllowedStates: no such road: " + roadsStates[i][j]);
                }
                state.add(road);
            }
            greenLights.add(state);
        }
    }

    Thread crossingRunner = null;

    public void startTraffic() throws InterruptedException
    {
        if (crossingRunner != null)
        {
            stopTraffic();
        }
        crossingRunner = new Thread(this::crossingProcessor);
        crossingRunner.start();
    }

    public void stopTraffic() throws InterruptedException
    {
        if (crossingRunner == null) return;
        crossingRunner.interrupt();
        crossingRunner.join(10000);
        if (crossingRunner.isAlive())
        {
            throw new RuntimeException("Fatal: crossingProcessor cannot be stopped");
        }
        crossingRunner = null;
    }

    // Traffic processor
    void crossingProcessor()
    {
        boolean stopFlag = false;
        getLogger().log(Level.INFO, "Crossing processor started");

        while (!Thread.interrupted() && !stopFlag)
        {
            // Iterate through green light states
            while (!stopFlag)
            {
                if (!currentGreenLight.compareAndSet(greenLights.size() - 1, 0))
                {
                    currentGreenLight.incrementAndGet();
                }

                List<Road> currentlyGreen = greenLights.get(currentGreenLight.get());

                if (stopFlag) break;

                lastCrossedCars.set(0);

                // Print debug info
                StringBuilder outBuffer = new StringBuilder();
                for (Road road : currentlyGreen)
                {
                    if (outBuffer.length() != 0)
                        outBuffer.append(", ");
                    outBuffer.append(road.getName());
                }
                outBuffer.append("\" at ").append(LocalDateTime.now());
                outBuffer.insert(0, "Crossing switched to the new state. Green light for roads: \"");
                getLogger().log(Level.INFO, outBuffer.toString());
                // End print debug ino

                long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < GREEN_DURATION)
                {
                    // Process waiting cars
                    for (Road currentRoad : currentlyGreen)
                    {
                        Car car = currentRoad.getWaitingCar();
                        if (car != null)
                        {
                            Road destination = roads.get(car.getDestination());
                            if (destination == null)
                            {
                                // No such destination, nowhere to route
                                continue;
                            }
                            car.setCrossed(LocalDateTime.now());
                            destination.acceptArrivedCar(car);
                            lastCrossedCars.incrementAndGet();
                        } else
                        {
                            // No cars in queue
                        }
                    }

                    // Process violators
                    for (Road violatorsRoad : roads.values())
                    {
                        Car car = violatorsRoad.getWaitingViolatorCar();
                        if (car != null)
                        {
                            // Process violators here. Currently it is the same as for usual cars but might be changed.
                            Road destination = roads.get(car.getDestination());
                            if (destination == null)
                            {
                                // No such destination, nowhere to route
                                continue;
                            }
                            if (currentlyGreen.contains(violatorsRoad))
                            {
                                // Violator runs on the green light
                                car.setViolator('N');
                            }
                            car.setCrossed(LocalDateTime.now());
                            destination.acceptArrivedCar(car);
                            lastCrossedCars.incrementAndGet();

                        }
                    }

                    // Define waiting strategy here:
                    long remainingTime = GREEN_DURATION - (System.currentTimeMillis() - start);
                    if (remainingTime > GREEN_DURATION_QUANTUM_DELAY)
                    {
                        remainingTime = GREEN_DURATION_QUANTUM_DELAY;
                    }
                    try
                    {
                        Thread.sleep(remainingTime);
                    } catch (InterruptedException e)
                    {
                        stopFlag = true; // To break outer loop
                        break;
                    }
                    // End waiting
                }
            }
        }
        getLogger().log(Level.INFO, "Crossing processor exited");
    }

    public Map<String, Road> getRoads()
    {
        return (Collections.unmodifiableMap(roads));
    }

    private static final Logger logger = Logger.getLogger(Crossing.class);

    public static Logger getLogger()
    {
        return (logger);
    }
}
