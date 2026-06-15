package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de Testes Unitários - Requisito Reporte
 * Responsável: Sidarta Britto
 */
public class ReporteTest {

    @Test
    public void testCriacaoReporteStatusInicial() {
        // 1. Preparação: Cria um novo reporte
        Reporte reporte = new Reporte("Lâmpada queimada", "Bloco A", null, null);
        
        // 2. Verificação: O ID deve existir e o status deve ser Pendente
        assertNotNull(reporte.getId(), "O ID do reporte não deveria ser nulo.");
        assertEquals("Pendente", reporte.getStatus(), "O status inicial deve ser 'Pendente'.");
    }

    @Test
    public void testAprovarReporte() {
        // 1. Preparação: Reporte criado
        Reporte reporte = new Reporte("Lâmpada queimada", "Bloco A", null, null);
        
        // 2. Ação: Admin aprova o reporte
        extracted(reporte);
        
        // 3. Verificação: Status deve mudar para Aprovado
        assertEquals("Aprovado", reporte.getStatus(), "O status deveria mudar para 'Aprovado' após a aprovação.");
    }

    private void extracted(Reporte reporte) {
        reporte.aprovar();
    }

    @Test
    public void testRecusarReporte() {
        // 1. Preparação: Reporte criado
        Reporte reporte = new Reporte("Lâmpada queimada", "Bloco A", null, null);
        
        // 2. Ação: Admin recusa o reporte
        reporte.recusar();
        
        // 3. Verificação: Status deve mudar para Recusado
        assertEquals("Recusado", reporte.getStatus(), "O status deveria mudar para 'Recusado' após a recusa.");
    }
}