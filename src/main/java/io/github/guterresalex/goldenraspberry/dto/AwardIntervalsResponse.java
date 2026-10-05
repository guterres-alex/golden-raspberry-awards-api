package io.github.guterresalex.goldenraspberry.dto;

import java.util.List;

public record AwardIntervalsResponse(List<ProducerInterval> min, List<ProducerInterval> max) {
}
