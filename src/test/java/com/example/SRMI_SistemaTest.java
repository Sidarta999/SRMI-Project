package com.example;

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SRMI_SistemaTest {

    @Test
    void deveAceitarEmailInstitucionalValido() {
        SRMI_Sistema sistema = new SRMI_Sistema();

        assertEquals(true, sistema.emailValido("aluno@ufrpe.br"));

        sistema.dispose();
    }

    @Test
    void deveRecusarEmailInstitucionalInvalido() {
        SRMI_Sistema sistema = new SRMI_Sistema();

        assertEquals(false, sistema.emailValido("teste@exemplo.com"));
        assertEquals(false, sistema.emailValido("emailsemarroba"));

        sistema.dispose();
    }

    @Test
    void deveCriarPainelRolavelComBarrasAdequadas() {
        SRMI_Sistema sistema = new SRMI_Sistema();

        JPanel painel = new JPanel();
        JScrollPane scroll = sistema.criarScrollPane(painel);

        assertNotNull(scroll);
        assertEquals(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, scroll.getVerticalScrollBarPolicy());
        assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, scroll.getHorizontalScrollBarPolicy());
        assertEquals(16, scroll.getVerticalScrollBar().getUnitIncrement());

        sistema.dispose();
    }
}
