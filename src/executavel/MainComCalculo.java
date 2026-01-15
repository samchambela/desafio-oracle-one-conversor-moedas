package executavel;

import api.ApiKey;
import calculos.CalculaConversao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modelos.ConsultaCodigos;
import modelos.DadosConversao;
import modelos.GeradorDeArquivo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainComCalculo {
    public static void main(String[] args) {
        Scanner leitura = new Scanner(System.in);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HttpClient client = HttpClient.newHttpClient();

        GeradorDeArquivo gerador = new GeradorDeArquivo();
        CalculaConversao calculadora = new CalculaConversao();

        List<String> historicoEmMemoria = new ArrayList<>();
        String moedaBase = "";

        while (true) {
            System.out.println("\n*** CONVERSOR DE MOEDAS [V2 - REALIZA CÁLCULOS LOCALMENTE E FORNECE LOGS] ***");
            System.out.println("Digite o código da moeda de origem (ex: USD), 'codigo' para ver lista de moedas suportadas, ou 'sair' para encerrar:");
            moedaBase = leitura.nextLine().toUpperCase().trim();

            if (moedaBase.equalsIgnoreCase("SAIR")) {
                System.out.println("Aplicação encerrada.");
                break;
            }

            if (moedaBase.equalsIgnoreCase("HISTORICO")) {
                System.out.println("*** SESSÃO ATUAL ***");
                if (historicoEmMemoria.isEmpty()) {
                    System.out.println("Nenhuma conversão realizada ainda.");
                } else {
                    historicoEmMemoria.forEach(System.out::println);
                }
                continue;
            }

            if (moedaBase.equalsIgnoreCase("CODIGO") || moedaBase.equalsIgnoreCase("CODIGOS")) {
                try {
                    String url = "https://v6.exchangerate-api.com/v6/"
                            + ApiKey.EXCHANGE_RATE
                            + "/codes";
                    HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).build();
                    HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                    ConsultaCodigos lista = gson.fromJson(resp.body(), ConsultaCodigos.class);

                    System.out.println("*** LISTAGEM DE MOEDAS SUPORTADAS ***");
                    lista.supported_codes().forEach(c -> System.out.println(c.get(0) + " - " + c.get(1)));
                } catch (Exception e) {
                    System.out.println("Erro ao buscar lista de códigos.");
                }
                continue;
            }

            System.out.println("Digite o código da moeda para qual deseja converter (ex: BRL):");
            String moedaAlvo = leitura.nextLine().toUpperCase().trim();

            System.out.println("Digite o valor que deseja converter: ");
            double valorRef = 0;
            try {
                valorRef = leitura.nextDouble();
                leitura.nextLine();
            } catch (Exception e) {
                System.out.println("ERRO: Valor digitado não é um número válido.");
                leitura.nextLine();
                continue;
            }

            String enderecoUrl = "https://v6.exchangerate-api.com/v6/"
                    + ApiKey.EXCHANGE_RATE
                    + "/pair/"
                    + moedaBase
                    + "/"
                    + moedaAlvo;

            try {
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(enderecoUrl)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    String urlTesteBase = "https://v6.exchangerate-api.com/v6/"
                            + ApiKey.EXCHANGE_RATE
                            + "/latest/"
                            + moedaBase;
                    HttpRequest requestTeste = HttpRequest.newBuilder().uri(URI.create(urlTesteBase)).build();
                    HttpResponse<String> responseTeste = client.send(requestTeste, HttpResponse.BodyHandlers.ofString());

                    if (responseTeste.statusCode() == 200) {
                        throw new IllegalArgumentException("A moeda alvo (" + moedaAlvo + ") não foi encontrada.");
                    } else {
                        throw new IllegalArgumentException("A moeda de origem (" + moedaBase + ") não foi encontrada.");
                    }
                }

                DadosConversao conversao = gson.fromJson(response.body(), DadosConversao.class);

                double valorConvertido = calculadora.converter(valorRef, conversao.conversion_rate());

                String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

                String mensagemLog = "[" + dataHora + "] "
                        + valorRef
                        + " ["
                        + conversao.base_code()
                        + "] corresponde a "
                        + valorConvertido
                        + " ["
                        + conversao.target_code()
                        + "] (Taxa de conversão: "
                        + conversao.conversion_rate()
                        + ")";
                System.out.println("------------------------------------------------");
                System.out.println(mensagemLog);
                System.out.println("------------------------------------------------");

                historicoEmMemoria.add(mensagemLog);
                gerador.salvarLog(mensagemLog);
                System.out.println("Registro salvo no histórico.");

            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
        leitura.close();
    }
}