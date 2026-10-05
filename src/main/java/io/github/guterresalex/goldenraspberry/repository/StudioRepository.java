package io.github.guterresalex.goldenraspberry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.guterresalex.goldenraspberry.domain.Studio;

public interface StudioRepository extends JpaRepository<Studio, Long> {
}
