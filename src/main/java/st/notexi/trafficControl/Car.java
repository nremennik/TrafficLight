package st.notexi.trafficControl;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "traffic_lights", schema = "NATA")
public class Car
{
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source", nullable = false, precision = 0)
    String source = null;

    @Column(name = "destination", nullable = false, precision = 0)
    String destination; // name of the destination road

    @Column(name = "car_id", nullable = false, precision = 0)
    private String name; // License plate

    @Column(name = "violator", nullable = false, precision = 0)
    private Character violator = 'N'; // Is it violator? Violators will run on red;

    static final int VIOLATION_PROBABILITY = 10;
    @Column(name = "arrived", nullable = false, precision = 0)
    LocalDateTime arrived = null;

    @Column(name = "crossed", nullable = false, precision = 0)
    LocalDateTime crossed = null;

    public Car(String name, String destination)
    {
        this.name = name;
        this.destination = destination;
        if (ThreadLocalRandom.current().nextInt(0, VIOLATION_PROBABILITY) == 0) // Violation probability
        {
            violator = 'Y';
        }
    }

    public Car(String name, String destination, String source, LocalDateTime arrived, LocalDateTime crossed, Character violator)
    {
        this.name = name;
        this.destination = destination;
        if (ThreadLocalRandom.current().nextInt(0, VIOLATION_PROBABILITY) == 0) // Violation probability
        {
            violator = 'Y';
        }
    }

    public Car()
    {

    }

    public String getDestination()
    {
        return (destination);
    }

    public String getName()
    {
        return (name);
    }

    public LocalDateTime getArrived()
    {
        return (arrived);
    }

    public void setArrived(LocalDateTime arrived)
    {
        this.arrived = arrived;
    }

    public LocalDateTime getCrossed()
    {
        return (crossed);
    }

    public void setCrossed(LocalDateTime crossed)
    {
        this.crossed = crossed;
    }

    public Character getViolator()
    {
        return (violator);
    }

    public void setViolator(Character violator)
    {
        this.violator = violator;
    }

    public String getSource()
    {
        return (source);
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public boolean equals(Object o)
    {

        if (this == o)
            return true;
        if (!(o instanceof Car))
            return false;
        Car car = (Car) o;
        return Objects.equals(this.id, car.id) && Objects.equals(this.name, car.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.name);
    }

    @Override
    public String toString()
    {
        return ("Car \"" + getName() + "\"" +
                (getSource() == null ? "" : " from " + getSource()) +
                (getDestination() == null ? "" : ", to " + getDestination()) +
                (getViolator().equals('Y') ? " (violator!) " : ". ") +
                (getArrived() == null ? "" : "Arrived at " + getArrived()) +
                (getCrossed() == null ? "" : ", crossed at " + getCrossed()));
    }

}
