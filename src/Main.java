import api.ApiKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner leitura = new Scanner(System.in);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HttpClient client = HttpClient.newHttpClient();

        String moedaBase = "";

        while (true) {
            System.out.println("--- CONVERSOR DE MOEDAS ---");
            System.out.println("Digite o código da moeda base (ex: USD) ou 'codigos' para ver a lista, ou 'sair' para encerrar:");
            moedaBase = leitura.nextLine().toUpperCase();

            if (moedaBase.equalsIgnoreCase("SAIR")) {
                break;
            }

            if (moedaBase.equalsIgnoreCase("CODIGOS")) {
                try {
                    String urlCodigos = "https://v6.exchangerate-api.com/v6/" + ApiKey.EXCHANGE_RATE + "/codes";
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlCodigos)).build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    ConsultaCodigos lista = gson.fromJson(response.body(), ConsultaCodigos.class);

                    System.out.println("--- MOEDAS SUPORTADAS ---");
                    for (var item : lista.supported_codes()) {
                        System.out.println(item.get(0) + " - " + item.get(1));
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("Erro ao buscar códigos: " + e.getMessage());
                }
                continue;
            }

            System.out.println("Digite o código da moeda para qual deseja converter (ex: BRL): ");
            String moedaAlvo = leitura.nextLine().toUpperCase();

            System.out.println("Digite o valor que deseja converter: ");
            double valorReferencia = leitura.nextDouble();
            leitura.nextLine();

            String enderecoUrl = "https://v6.exchangerate-api.com/v6/" + ApiKey.EXCHANGE_RATE + "/pair/"
                    + moedaBase + "/" + moedaAlvo + "/" + valorReferencia;

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(enderecoUrl))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                DadosConversao conversao = gson.fromJson(response.body(), DadosConversao.class);

                System.out.println("------------------------------------------------");
                System.out.printf("Valor: %.2f [%s] corresponde a %.2f [%s]%n",
                        valorReferencia, conversao.base_code(),
                        conversao.conversion_result(), conversao.target_code());
                System.out.println("Taxa de câmbio: " + conversao.conversion_rate());
                System.out.println("------------------------------------------------");

            } catch (IOException | InterruptedException e) {
                System.out.println("Erro na conexão ou na conversão: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Erro: verifique se o código da moeda existe).");
            }
        }
        leitura.close();
    }
}