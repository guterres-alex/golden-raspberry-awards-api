package io.github.guterresalex.goldenraspberry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.guterresalex.goldenraspberry.domain.Producer;

public interface ProducerRepository extends JpaRepository<Producer, Long> {
}
