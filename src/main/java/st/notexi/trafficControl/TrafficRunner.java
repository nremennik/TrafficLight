package st.notexi.trafficControl;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class TrafficRunner
{
    @Autowired
    Crossing crossing;

    private volatile boolean stopTraffic = false;

    public void runTraffic(CarRepository carRepository)
    {
        getLogger().log(Level.INFO, "Starting traffic ");

        AtomicLong carNumber = new AtomicLong(0);

        Road r1 = new Road("Road 1", () ->
        {
            Car car = new Car("Car-" + carNumber.incrementAndGet(), "Road 3");
            getLogger().log(Level.INFO, "New car on road 1: " + car);
            return (car);
        });
        Road r2 = new Road("Road 2", () ->
        {
            Car car = new Car("Car-" + carNumber.incrementAndGet(), "Road 4");
            getLogger().log(Level.INFO, "New car on road 2: " + car);
            return (car);
        });
        Road r3 = new Road("Road 3", () ->
        {
            Car car = new Car("Car-" + carNumber.incrementAndGet(), "Road 1");
            getLogger().log(Level.INFO, "New car on road 3: " + car);
            return (car);
        });
        Road r4 = new Road("Road 4", () ->
        {
            Car car = new Car("Car-" + carNumber.incrementAndGet(), "Road 2");
            getLogger().log(Level.INFO, "New car on road 4: " + car);
            return (car);
        });

        Vector<Road> roads = new Vector<>();
        roads.add(r1);
        roads.add(r2);
        roads.add(r3);
        roads.add(r4);

        // crossing = new Crossing();

        for (int i = 0; i < roads.size(); i++)
            crossing.addRoad(roads.get(i));

        crossing.setAllowedStates(new String[][]{
                {"Road 1", "Road 3"},
                {"Road 2", "Road 4"}
        });
        try
        {
            crossing.startTraffic();
            for (int i = 0; i < roads.size(); i++)
                roads.get(i).startTraffic();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();
        while (!stopTraffic)
        {

            for (int i = 0; i < roads.size(); i++)
            {
                final Road road = roads.get(i);
                road.processArrived((arrivedCar) ->
                {
                    // Process arrived cars (insert to database)
                    carRepository.save(arrivedCar);
                    getLogger().log(Level.INFO, "Car arrived to road " + road.getName() + ": " + arrivedCar);
                    return (true);
                });
            }

            if (stopTraffic != false)
            {
                try
                {
                    for (int i = 0; i < roads.size(); i++)
                        roads.get(i).stopTraffic();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        getLogger().log(Level.INFO, "Exit flagged, exiting");
        try
        {
            crossing.stopTraffic();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(TrafficRunner.class);

    public static Logger getLogger()
    {
        return (logger);
    }
}
