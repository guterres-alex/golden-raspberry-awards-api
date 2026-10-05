package io.github.guterresalex.goldenraspberry.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "movie")
public class Movie {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "release_year", nullable = false)
	private int year;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private boolean winner;

	@ManyToMany
	@JoinTable(name = "movie_producer",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "producer_id"))
	private Set<Producer> producers = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "movie_studio",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "studio_id"))
	private Set<Studio> studios = new HashSet<>();

	protected Movie() {
	}

	public Movie(int year, String title, boolean winner) {
		this.year = year;
		this.title = title;
		this.winner = winner;
	}

	public void addProducer(Producer producer) {
		producers.add(producer);
	}

	public void addStudio(Studio studio) {
		studios.add(studio);
	}

	public Long getId() {
		return id;
	}

	public int getYear() {
		return year;
	}

	public String getTitle() {
		return title;
	}

	public boolean isWinner() {
		return winner;
	}

	public Set<Producer> getProducers() {
		return Collections.unmodifiableSet(producers);
	}

	public Set<Studio> getStudios() {
		return Collections.unmodifiableSet(studios);
	}

}
