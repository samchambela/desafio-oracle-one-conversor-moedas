package modelos;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GeradorDeArquivo {

    public void salvarLog(String mensagem) {
        try (FileWriter fw = new FileWriter("historico.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(mensagem);
        } catch (IOException e) {
            System.out.println("Erro ao salvar histórico: " + e.getMessage());
        }
    }
}