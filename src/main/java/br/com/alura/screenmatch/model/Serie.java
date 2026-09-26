package br.com.alura.screenmatch.model;

import br.com.alura.screenmatch.service.ConsultaChatGPT;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import org.hibernate.engine.internal.Cascade;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Entity
@Table(name = "séries")
public class Serie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String titulo;
    private Integer totalTemporadas;
    private Double avaliacao;
    @Enumerated(EnumType.STRING)
    private Categoria genero;
    @Column(length = 500) // Aumentando o tamanho para atores
    private String atores;
    @Column(length = 500) // Aumentando o tamanho para poster (URLs podem ser longas)
    private String poster;
    @Column(length = 2000) // Aumentando o tamanho para sinopse
    private String sinopse;

    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Episodio> episodios = new ArrayList<>();

    public Serie() {
        // Construtor padrão exigido pelo JPA/Hibernate
    }

    public Serie(DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.totalTemporadas = dadosSerie.totalTemporadas();

        // CORREÇÃO: Tratamento para "N/A" na avaliação
        if (dadosSerie.avaliacao() != null && !dadosSerie.avaliacao().equalsIgnoreCase("N/A")) {
            this.avaliacao = Double.valueOf(dadosSerie.avaliacao());
        } else {
            this.avaliacao = 0.0; // Valor padrão para quando a avaliação for "N/A" ou nula
        }

        this.genero = Categoria.fromString(dadosSerie.genero().split(",")[0].trim());
        this.atores = dadosSerie.atores();
        this.poster = dadosSerie.poster();
        this.sinopse = ConsultaChatGPT.obterTraducao(dadosSerie.sinopse()).trim();
    }

    public List<Episodio> getEpisodios() {
        return episodios;
    }

    public void setEpisodios(List<Episodio> episodios) {

        episodios.forEach(e -> e.setSerie(this));
        this.episodios = episodios;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getTotalTemporadas() {
        return totalTemporadas;
    }

    public void setTotalTemporadas(Integer totalTemporadas) {
        this.totalTemporadas = totalTemporadas;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Categoria getGenero() {
        return genero;
    }

    public void setGenero(Categoria genero) {
        this.genero = genero;
    }

    public String getAtores() {
        return atores;
    }

    public void setAtores(String atores) {
        this.atores = atores;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    @Override
    public String toString() {
        return
                "genero=" + genero +
                        ", titulo='" + titulo + '\'' +
                        ", totalTemporadas=" + totalTemporadas +
                        ", avaliacao=" + avaliacao +
                        ", atores='" + atores + '\'' +
                        ", poster='" + poster + '\'' +
                        ", sinopse='" + sinopse + '\'' +
                        ", episodios='" + episodios + '\'';
    }
}