package st.notexi.trafficControl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public class Road
{
    String name;

    BlockingQueue<Car> onTheCrossing = new LinkedBlockingQueue<>();
    BlockingQueue<Car> violatorsOnTheCrossing = new LinkedBlockingQueue<>();
    BlockingQueue<Car> arrived = new LinkedBlockingQueue<>();

    Supplier<Car> carSupplier;

    volatile boolean stopFlag=false;

    private Road()
    {
    }

    public Road(String name, Supplier<Car> carSupplier)
    {
        this.name = name;
        this.carSupplier = carSupplier;
    }

    Thread trafficRunner = null;

    public int getOnTheCrossing()
    {
        return onTheCrossing.size();
    }

    public void startTraffic() throws InterruptedException
    {
        if (trafficRunner != null)
        {
            stopTraffic();
        }
        stopFlag=false;
        trafficRunner = new Thread(this::trafficProcessor);
        trafficRunner.start();
    }

    public void stopTraffic() throws InterruptedException
    {
        if (trafficRunner == null) return;
        stopFlag=true;

        trafficRunner.interrupt();
        trafficRunner.join(10000);
        if (trafficRunner.isAlive())
        {
            throw new RuntimeException("Fatal: trafficProcessor for road " + getName() + " cannot be stopped");
        }
        trafficRunner = null;
    }

    public void trafficProcessor()
    {
        while (!stopFlag)
        {
            Car car = carSupplier.get();
            car.setSource(getName());
            car.setArrived(LocalDateTime.now());

            if (car.getViolator().equals('Y'))
            {
                violatorsOnTheCrossing.add(car);
            } else
            {
                onTheCrossing.add(car);
            }

            try
            {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000));
            } catch (InterruptedException e)
            {
                break;
            }
        }
        getLogger().log(Level.INFO, "Traffic processor exited for road " + getName());
    }

    public void acceptArrivedCar(Car car)
    {
        arrived.add(car);
    }

    public String getName()
    {
        return (name);
    }

    public Car getWaitingCar()
    {
        return (onTheCrossing.poll());
    }

    public Car getWaitingViolatorCar()
    {
        return (violatorsOnTheCrossing.poll());
    }

    public void processArrived(Function<Car, Boolean> processor)
    {
        Car car;
        while ((car = arrived.poll()) != null)
        {
            // Do any arrived cars processing here
            processor.apply(car);
        }
    }

    private static final Logger logger = Logger.getLogger(Road.class);

    public static Logger getLogger()
    {
        return (logger);
    }
}
