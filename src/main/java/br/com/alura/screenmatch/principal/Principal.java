package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private ConverteDados conversor = new ConverteDados();
    private ConsumoApi consumo = new ConsumoApi();
    private final String ENDERECO = "https://omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private Scanner leitura = new Scanner(System.in);

    // Lista para armazenar as séries buscadas
    private List<DadosSerie> dadosSeries = new ArrayList<>();


   private SerieRepository repositorio;
   private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repositorio) {
   this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1; // Inicializa opcao com -1 para entrar no loop
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por titulo
                    5 - Buscar Série por ator
                    6 - Top 5 séries 
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine(); // Consome a linha pendente

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;

                case 6:
                    buscarTop5Series();
                    break;

                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }




    // Método para obter os dados de uma série da web
    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para Busca: ");
        var nomesSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomesSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    // Método para buscar uma série na web e adicioná-la à lista
    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
               repositorio.save(serie);            // Adiciona a série à lista
        System.out.println(dados);
    }

    // Método para buscar episódios de uma série
    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);


        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);

            System.out.println("Episódios da série " + serieEncontrada.getTitulo() + " salvos com sucesso!");

        } else {
            System.out.println("Série não encontrada!");
        }
    }


    // Método para listar as séries que foram buscadas
    private void listarSeriesBuscadas() {
        series = series =repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma serie pelo nome : ");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBuscada.isPresent()) {
            System.out.println("Dados da serie: " + serieBuscada.get());

        }else
            System.out.println("Série nao encontrada!");





        }

    private void buscarSeriePorAtor() {
        System.out.println("Qual é o nome para busca: ");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliaçoes a partir de que  valor: ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByatoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Séries em que:  " +  nomeAtor + " " + " trabalhou: ");
        seriesEncontradas.forEach( s->
                System.out.println(s.getTitulo() +" " + "Avaliação: " + s.getAvaliacao() )

        );


    }
    private void buscarTop5Series() {
    List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
    serieTop.forEach(s->
            System.out.println(s.getTitulo() + " " + "avaliação : " + s.getAvaliacao()));

    }

    }



