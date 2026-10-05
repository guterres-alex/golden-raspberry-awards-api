package io.github.guterresalex.goldenraspberry.dto;

import java.util.List;

public record MovieCsvRow(int year, String title, List<String> studios, List<String> producers, boolean winner, int lineNumber) {
}
