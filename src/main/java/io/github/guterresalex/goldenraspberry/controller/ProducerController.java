package io.github.guterresalex.goldenraspberry.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.guterresalex.goldenraspberry.dto.AwardIntervalsResponse;
import io.github.guterresalex.goldenraspberry.service.AwardIntervalService;

@RestController
@RequestMapping("/api/producers")
public class ProducerController {

	private final AwardIntervalService awardIntervalService;

	public ProducerController(AwardIntervalService awardIntervalService) {
		this.awardIntervalService = awardIntervalService;
	}

	@GetMapping(value = "/award-intervals", produces = MediaType.APPLICATION_JSON_VALUE)
	public AwardIntervalsResponse awardIntervals() {
		return awardIntervalService.findAwardIntervals();
	}

}
