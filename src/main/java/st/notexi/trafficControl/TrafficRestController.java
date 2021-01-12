package st.notexi.trafficControl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
public class TrafficRestController
{
    @Autowired
    private CarRepository carRepository;

    @Autowired
    Crossing crossing;

    @GetMapping("/cars/{strDateFrom}/{strDateTo}")
    public List<Car> getList(@PathVariable String strDateFrom,
                             @PathVariable String strDateTo)
    {
        LocalDateTime dateFrom = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        LocalDateTime dateTo = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try
        {
            dateFrom = LocalDateTime.parse(strDateFrom, dateTimeFormatter);
        } catch (Exception e)
        {
        }
        try
        {
            dateTo = LocalDateTime.parse(strDateTo, dateTimeFormatter);
        } catch (Exception e)
        {
        }
        List<Car> cars = carRepository.findByCrossedBetween(dateFrom, dateTo);
        return (cars);
    }

    @GetMapping("/cars")
    public CarsCount getPassedCars()
    {
        CarsCount carsCount = new CarsCount(crossing.getLastCrossedCars(), crossing.getWaitingOnRedCars());

        return (carsCount);
    }

    @GetMapping("/roads")
    public List<String> getRoads()
    {
        return (crossing.getRoadsNames());
    }

    @GetMapping("/roads/green")
    public List<String> getGreenRoads()
    {
        return (crossing.getCurrentlyGreenRoadsNames());
    }

    public class CarsCount
    {
        long crossedCarsCount;
        long waitingCarsCount;

        public CarsCount()
        {
            crossedCarsCount = 0;
            waitingCarsCount = 0;
        }

        public CarsCount(long crossedCarsCount, long waitingCarsCount)
        {
            this.crossedCarsCount = crossedCarsCount;
            this.waitingCarsCount = waitingCarsCount;
        }

        public long getCrossedCarsCount()
        {
            return crossedCarsCount;
        }

        public void setCrossedCarsCount(long crossedCarsCount)
        {
            this.crossedCarsCount = crossedCarsCount;
        }

        public long getWaitingCarsCount()
        {
            return waitingCarsCount;
        }

        public void setWaitingCarsCount(long waitingCarsCount)
        {
            this.waitingCarsCount = waitingCarsCount;
        }
    }
}
