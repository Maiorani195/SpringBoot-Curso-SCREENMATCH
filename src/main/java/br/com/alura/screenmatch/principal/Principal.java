package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

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


    public void exibeMenu() {
        var opcao = -1; // Inicializa opcao com -1 para entrar no loop
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    
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
        dadosSeries.add(dados); // Adiciona a série à lista
        System.out.println(dados);
    }

    // Método para buscar episódios de uma série
    private void buscarEpisodioPorSerie() {
        System.out.println("Digite o nome da série para buscar os episódios: ");
        var nomesSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomesSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumo.obterDados(ENDERECO + nomesSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        // Lógica para processar e exibir episódios
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                )
                .collect(Collectors.toList());

        episodios.forEach(System.out::println);

        // Lógica para buscar episódio por trecho do título
        System.out.println("Digite um trecho do titulo do episódio: ");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();

        if (episodioBuscado.isPresent()) {
            System.out.println("Episódio Encontrado: ");
            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
        } else {
            System.out.println("Episódio não encontrado.");
        }

        // Se você tinha outras lógicas como Top 10 episódios ou busca por ano,
        // elas podem ser adicionadas aqui ou em novos métodos.
        // Por exemplo, o trecho comentado do Top 10:
        /*
        System.out.println("\n Top 10 episódios");
        episodios.stream()
                .filter(e -> e.getAvaliacao() != null && !e.getAvaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                .limit(10)
                .map(e -> e.getTitulo().toUpperCase())
                .forEach(System.out::println);
        */
    }

    // Método para listar as séries que foram buscadas
    private void listarSeriesBuscadas() {
        List<Serie> series = new ArrayList<>();
        series = dadosSeries.stream()
                .map(d -> new Serie(d))
                .collect(Collectors.toList());
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }
}

