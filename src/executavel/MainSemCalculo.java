package executavel;

import api.ApiKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modelos.ConsultaCodigos;
import modelos.DadosConversao;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MainSemCalculo {
    public static void main(String[] args) {
        Scanner leitura = new Scanner(System.in);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HttpClient client = HttpClient.newHttpClient();

        String moedaBase = "";

        while (true) {
            System.out.println("\n*** CONVERSOR DE MOEDAS [V1 - OBTÉM A CONVERSÃO DIRETAMENTE DA API] ***");
            System.out.println("Digite o código da moeda de origem (ex: USD), 'codigo' para ver lista de moedas suportadas, ou 'sair' para encerrar:");
            moedaBase = leitura.nextLine().toUpperCase().trim();

            if (moedaBase.equalsIgnoreCase("SAIR")) {
                System.out.println("Aplicação encerrada.");
                break;
            }

            if (moedaBase.equalsIgnoreCase("CODIGO") || moedaBase.equalsIgnoreCase("CODIGOS")) {
                try {
                    String urlCodigos = "https://v6.exchangerate-api.com/v6/"
                            + ApiKey.EXCHANGE_RATE
                            + "/codes";
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlCodigos)).build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    ConsultaCodigos lista = gson.fromJson(response.body(), ConsultaCodigos.class);

                    System.out.println("*** LISTAGEM DE MOEDAS SUPORTADAS ***");
                    for (var item : lista.supported_codes()) {
                        System.out.println(item.get(0) + " - " + item.get(1));
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("Erro ao buscar lista de códigos.");
                }
                continue;
            }

            System.out.println("Digite o código da moeda para qual deseja converter (ex: BRL): ");
            String moedaAlvo = leitura.nextLine().toUpperCase().trim();

            double valorReferencia = 0;
            try {
                System.out.println("Digite o valor que deseja converter: ");
                valorReferencia = leitura.nextDouble();
                leitura.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("ERRO: Valor digitado não é um número válido.");
                leitura.nextLine();
                continue;
            }

            String enderecoUrl = "https://v6.exchangerate-api.com/v6/"
                    + ApiKey.EXCHANGE_RATE + "/pair/"
                    + moedaBase
                    + "/"
                    + moedaAlvo
                    + "/"
                    + valorReferencia;

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(enderecoUrl))
                        .build();

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

                System.out.println("------------------------------------------------");
                System.out.println("RESULTADO: " + valorReferencia
                        + " ["
                        + conversao.base_code()
                        + "] corresponde a "
                        + conversao.conversion_result()
                        + " ["
                        + conversao.target_code()
                        + "]");
                System.out.println("Taxa de conversão: " + conversao.conversion_rate());
                System.out.println("------------------------------------------------");

            } catch (IllegalArgumentException e) {
                System.out.println("ERRO NA BUSCA: " + e.getMessage());
            } catch (IOException | InterruptedException e) {
                System.out.println("ERRO DE CONEXÃO: Verifique sua internet.");
            } catch (Exception e) {
                System.out.println("ERRO INESPERADO: " + e.getMessage());
            }
        }
        leitura.close();
    }
}