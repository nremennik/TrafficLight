package st.notexi.trafficControl;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

interface CarRepository extends CrudRepository<Car, Long>
{
    List<Car> findByCrossedBetween(LocalDateTime from, LocalDateTime to);
}