package io.github.guterresalex.goldenraspberry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.github.guterresalex.goldenraspberry.domain.Producer;
import io.github.guterresalex.goldenraspberry.dto.ProducerWinYear;

public interface ProducerRepository extends JpaRepository<Producer, Long> {

	@Query("""
			select new io.github.guterresalex.goldenraspberry.dto.ProducerWinYear(p.id, p.name, m.year)
			from Movie m join m.producers p
			where m.winner = true
			order by p.id, m.year""")
	List<ProducerWinYear> findWinYearsOrderedByProducer();

}
