package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;

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

   private Optional<Serie> serieBusca;

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
                    7 - Buscar série por categoria
                    8 - Filtrar série por temporada e avaliação
                    9 - Buscar por trecho de episódio
                    10 - Top episódios por série
                    11 - Buscar Episodios por Data
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

                case 7:
                    buscarPorGenero();
                    break;

                case 8:
                    buscarPorTemporadaeAvaliacao();

                case 9:
                    buscarEpisodioPorTrecho();
                    break;

                case 10:
                    topEpisodiosPorSerie();
                    break;

                case 11:
                        buscarEpisodioPorData();
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
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()) {
            System.out.println("Dados da serie: " + serieBusca.get());

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

    private void buscarPorGenero() {
        System.out.println("Qual categoria voce deseja buscar: ");
        var nomeCategoria = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeCategoria);
    List<Serie>  seriePorCategoria= repositorio.findByGenero(categoria);
        System.out.println("Series da categoria : " +" " + nomeCategoria);
        seriePorCategoria.forEach(System.out::println);




    }
    private void buscarPorTemporadaeAvaliacao() {
        System.out.println("Digite a quantidade de temporadas: ");
        var maxTemporada = leitura.nextInt();

        System.out.println("Digite a avaliação desejada: ");
        var minAvaliacao = leitura.nextDouble();

        List<Serie> seriesFiltradas = repositorio.seriesPorTemporadaEAvaliacao(maxTemporada,minAvaliacao);

        System.out.println("Séries com até: " + maxTemporada + " temporadas e avaliacao minima de : " + minAvaliacao);

        if (seriesFiltradas.isEmpty()) {
            System.out.println("Nenhuma série encontrada'");
        } else {
            seriesFiltradas.forEach(System.out::println);
        }



    }
//Código omitido

    private void buscarEpisodioPorTrecho(){
        System.out.println("Qual o nome do episódio para busca?");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));
    }



    private void topEpisodiosPorSerie(){
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s Avaliação %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo() , e.getAvaliacao()));
        }
    }

    private void buscarEpisodioPorData() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lancaçamento: ");
            var anoLancamento = leitura.nextInt();
            List<Episodio> episodiosAno = repositorio.episodiosPorSerieEAno( serie ,  anoLancamento);
            episodiosAno.forEach(System.out::println);
        }



    }

    }









