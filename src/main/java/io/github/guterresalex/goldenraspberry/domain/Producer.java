package io.github.guterresalex.goldenraspberry.domain;

import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "producer")
public class Producer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = ColumnLimits.TEXT_MAX_LENGTH)
	private String name;

	@Column(name = "name_key", nullable = false, unique = true, length = ColumnLimits.TEXT_MAX_LENGTH)
	private String nameKey;

	protected Producer() {
	}

	public Producer(String name) {
		this.name = name;
		this.nameKey = keyOf(name);
	}

	public static String keyOf(String name) {
		return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNameKey() {
		return nameKey;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || (o instanceof Producer other && Objects.equals(nameKey, other.nameKey));
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(nameKey);
	}

}
