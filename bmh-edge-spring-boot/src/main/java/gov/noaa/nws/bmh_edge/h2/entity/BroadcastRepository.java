package gov.noaa.nws.bmh_edge.h2.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// persistence technology-specific abstractions 
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
// https://www.concretepage.com/spring-5/spring-data-crudrepository-example
@Repository
public interface BroadcastRepository extends JpaRepository<BroadcastMessage, Long>{

}
