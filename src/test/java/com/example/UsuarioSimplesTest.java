package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de Testes Unitários - Requisito Gamificação
 * Responsável: Vithor de Castro Souza
 */
public class UsuarioSimplesTest {

    @Test
    public void testAdicionarCoins() {
        // 1. Preparação (Setup): Cria um aluno zerado
        UsuarioSimples aluno = new UsuarioSimples("Vithor Castro", "v@ufrpe.br", "123456789", "senha123");
        
        // 2. Ação (Action): Simula o sistema dando 50 moedas
        aluno.adicionarCoins(50);
        
        // 3. Verificação (Assert): O saldo final DEVE ser 50
        assertEquals(50, aluno.getSaldoCoins(), "O saldo deveria ser exatamente 50 após a adição.");
    }

    @Test
    public void testDebitarCoinsComSaldoSuficiente() {
        // 1. Preparação: Aluno com 100 moedas
        UsuarioSimples aluno = new UsuarioSimples("Vithor Castro", "v@ufrpe.br", "123456789", "senha123");
        aluno.adicionarCoins(100);
        
        // 2. Ação: Aluno tenta gastar 40 na loja
        boolean compraAprovada = aluno.debitarCoins(40);
        
        // 3. Verificação: Compra deve ser autorizada (true) e sobrar 60 de saldo
        assertTrue(compraAprovada, "A transação deveria ser aprovada, pois há saldo.");
        assertEquals(60, aluno.getSaldoCoins(), "O saldo restante deveria ser 60.");
    }

    @Test
    public void testDebitarCoinsSemSaldo() {
        // 1. Preparação: Aluno com apenas 20 moedas
        UsuarioSimples aluno = new UsuarioSimples("Vithor Castro", "v@ufrpe.br", "123456789", "senha123");
        aluno.adicionarCoins(20);
        
        // 2. Ação: Aluno tenta gastar 100
        boolean compraAprovada = aluno.debitarCoins(100);
        
        // 3. Verificação: Compra deve ser recusada (false) e saldo continua 20
        assertFalse(compraAprovada, "A transação deveria ser bloqueada por falta de saldo.");
        assertEquals(20, aluno.getSaldoCoins(), "O saldo não pode ser alterado em compras falhas.");
    }
}