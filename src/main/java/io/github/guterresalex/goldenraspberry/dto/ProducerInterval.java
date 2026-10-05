package io.github.guterresalex.goldenraspberry.dto;

public record ProducerInterval(String producer, int interval, int previousWin, int followingWin) {
}
