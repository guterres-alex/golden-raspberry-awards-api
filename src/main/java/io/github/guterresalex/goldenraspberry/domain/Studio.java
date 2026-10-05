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
@Table(name = "studio")
public class Studio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "name_key", nullable = false, unique = true)
	private String nameKey;

	protected Studio() {
	}

	public Studio(String name) {
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
		return this == o || (o instanceof Studio other && Objects.equals(nameKey, other.nameKey));
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(nameKey);
	}

}
