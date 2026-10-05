package io.github.guterresalex.goldenraspberry.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        List<ProducerWinYear> wins = producerRepository.findWinYearsOrderedByProducer();

        TreeMap<Integer, List<ProducerInterval>> byInterval = IntStream.range(1, wins.size())
                .filter(i -> sameProducer(wins.get(i - 1), wins.get(i)))
                .mapToObj(i -> toInterval(wins.get(i - 1), wins.get(i)))
                .sorted(ORDER)
                .collect(Collectors.groupingBy(ProducerInterval::interval, TreeMap::new, Collectors.toList()));

        return new AwardIntervalsResponse(
                valuesOf(byInterval.firstEntry()),
                valuesOf(byInterval.lastEntry()));
    }

    private static boolean sameProducer(ProducerWinYear previous, ProducerWinYear current) {
        return previous.producerId().equals(current.producerId());
    }

    private static ProducerInterval toInterval(ProducerWinYear previous, ProducerWinYear current) {
        return new ProducerInterval(current.producer(), current.year() - previous.year(),
                previous.year(), current.year());
    }

    private static List<ProducerInterval> valuesOf(Map.Entry<Integer, List<ProducerInterval>> entry) {
        return entry == null ? List.of() : entry.getValue();
    }

}