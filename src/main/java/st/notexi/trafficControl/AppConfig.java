package st.notexi.trafficControl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig
{
    @Bean
    public TrafficRunner getTrafficRunner()
    {
        return (new TrafficRunner());
    }

    @Bean
    public Crossing getCrossing()
    {
        return (new Crossing());
    }
}
