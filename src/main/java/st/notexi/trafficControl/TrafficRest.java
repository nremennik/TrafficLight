package st.notexi.trafficControl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class TrafficRest
{
    public static void main(String[] args)
    {
        SpringApplication.run(TrafficRest.class, args);
    }

    @Autowired
    TrafficRunner trafficRunner;

    @Bean
    public CommandLineRunner run(CarRepository carRepository)
    {
        return (args) ->
        {
            trafficRunner.runTraffic(carRepository);
        };
    }

    private static final Logger logger = Logger.getLogger(TrafficRest.class);

    public static Logger getLogger()
    {
        return (logger);
    }
}
