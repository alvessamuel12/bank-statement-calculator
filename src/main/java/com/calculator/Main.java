package com.calculator;

import com.calculator.model.Account;
import com.calculator.model.Operation;
import com.calculator.model.OperationOptions;
import com.calculator.model.Statement;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class Main {
    private static final int DATAHORAOPERCAO = 0;
    private static final int ID_DA_CONTA = 1;
    private static final int NOME_DO_BANCO = 2;
    private static final int NUMERO_DA_AGENCIA = 3;
    private static final int NUMERO_DA_CONTA = 4;
    private static final int OPERADOR = 5;
    private static final int TIPO = 6;
    private static final int VALOR = 7;

    public static <dir> void main(String[] args) {


        // TODO exportar para classe de import e export de informacoes
        Path path = Paths.get("./src/main/resources/operacoes.csv");
        Reader reader;
        List<Operation> operacoes = new LinkedList<>();
        String [] line;
        int count = 0;
        try {
            reader = Files.newBufferedReader(path);
            CSVReader csvReader = new CSVReader(reader);
            while((line = csvReader.readNext()) != null) {
                if (count == 0) {
                    count++;
                    continue;
                }
                operacoes.add(createOperation(line));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }

        // TODO exportar para metodo que transforma a lista de infos na estrutura correta
        Map<Account, Set<Operation>> teste = operacoes.stream()
                .collect(Collectors.toMap(
                        Operation::getAccount,
                        operation -> {
                            Set<Operation> operationSet = new TreeSet<>();
                            operationSet.add(operation);
                            return operationSet;
                        },
                        (acumulador, atual) -> {
                            acumulador.addAll(atual);
                            return acumulador;
                        }
                )
        );


        // TODO exportar para metodos de escrita, criacao e manutencao dos arquivos e diretorios
        String pathDir = "./statements";
        Path pathDirectory = Paths.get(pathDir);
        File dir;
        try {
            if(!new File(pathDirectory.toString()).exists()) {
                Files.createDirectory(pathDirectory);
            } else {
                dir = new File(pathDir);
                Arrays.stream(dir.listFiles()).forEach(File::delete);
                Files.delete(pathDirectory);
                Files.createDirectory(pathDirectory);
            }
            List<Statement> statements = teste.keySet().stream()
                    .map(account -> createStatement(account, teste.get(account))).toList();
            for (Statement statement : statements) {
                String fileName = statement.getAccountID()+".txt";
                Path filePath = Files.createFile(Paths.get(pathDirectory +"/"+fileName));
                Files.writeString(filePath, statement.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * carregar as operações para uma estrutura e organizar por conta,
         * ordenar por data e ir efetuando o manejamento da conta
         * somando e subtraindo o saldo até terminar as operações e criar um arquivo relacionado a conta.
         * Fazer isso para todas as contas.
         * */
    }

    private static Operation createOperation(String[] line) {
        String operador = line[OPERADOR];
        OperationOptions option = OperationOptions.valueOf(line[TIPO]);
        Double valor = Double.valueOf(line[VALOR]);
        LocalDateTime date = LocalDateTime.parse(line[DATAHORAOPERCAO], DateTimeFormatter.ISO_DATE_TIME);
        String idDaConta = line[ID_DA_CONTA];
        String nomeDoBanco = line[NOME_DO_BANCO];
        String numeroDaAgencia = line[NUMERO_DA_AGENCIA];
        String numeroDaConta = line[NUMERO_DA_CONTA];
        Account conta = new Account(idDaConta, nomeDoBanco, numeroDaAgencia, numeroDaConta);
        return new Operation(operador, option, valor, date, conta);
    }

    private static Statement createStatement(Account account, Set<Operation> operations) {
        return new Statement(account, operations);
    }
}
