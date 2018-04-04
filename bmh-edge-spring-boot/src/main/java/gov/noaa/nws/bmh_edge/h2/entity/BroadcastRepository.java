package gov.noaa.nws.bmh_edge.h2.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BroadcastRepository extends JpaRepository<BroadcastMessage, Long>{

}
