package io.github.guterresalex.goldenraspberry.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.guterresalex.goldenraspberry.dto.AwardIntervalsResponse;
import io.github.guterresalex.goldenraspberry.dto.ProducerInterval;
import io.github.guterresalex.goldenraspberry.dto.ProducerWinYear;
import io.github.guterresalex.goldenraspberry.repository.ProducerRepository;

@Service
public class AwardIntervalService {

	private static final Comparator<ProducerInterval> ORDER = Comparator
			.comparingInt(ProducerInterval::previousWin)
			.thenComparing(ProducerInterval::producer);

	private final ProducerRepository producerRepository;

	public AwardIntervalService(ProducerRepository producerRepository) {
		this.producerRepository = producerRepository;
	}

	public AwardIntervalsResponse findAwardIntervals() {
		List<ProducerInterval> min = new ArrayList<>();
		List<ProducerInterval> max = new ArrayList<>();
		int minInterval = Integer.MAX_VALUE;
		int maxInterval = Integer.MIN_VALUE;

		ProducerWinYear previous = null;
		for (ProducerWinYear current : producerRepository.findWinYearsOrderedByProducer()) {
			if (previous != null && previous.producerId().equals(current.producerId())) {
				int interval = current.year() - previous.year();
				ProducerInterval pair = new ProducerInterval(current.producer(), interval, previous.year(),
						current.year());

				if (interval < minInterval) {
					minInterval = interval;
					min.clear();
				}
				if (interval == minInterval) {
					min.add(pair);
				}

				if (interval > maxInterval) {
					maxInterval = interval;
					max.clear();
				}
				if (interval == maxInterval) {
					max.add(pair);
				}
			}
			previous = current;
		}

		min.sort(ORDER);
		max.sort(ORDER);
		return new AwardIntervalsResponse(min, max);
	}

}
