import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * PROJETO SRMI - ZeloTech (UFRPE)
 * Versão: 2.0 - Consolidado Pós-Sprint 2
 * Módulos: Autenticação, Cadastro, Dashboard e Gamificação de Reportes.
 */

// --- MODELO DE DADOS: UTILIZADOR ---
class Usuario {
    private String nome;
    private String email;
    private String cpf;
    private String senha;
    private int saldoCoins;

    public Usuario(String nome, String email, String cpf, String senha) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        this.saldoCoins = 0;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public String getSenha() { return senha; }
    public int getSaldoCoins() { return saldoCoins; }
    public void adicionarCoins(int qtd) { this.saldoCoins += qtd; }
}

// --- MODELO DE DADOS: REPORTE ---
class Reporte {
    private String categoria;
    private String descricao;
    private String autorCpf;
    private String status;

    public Reporte(String cat, String desc, String cpf) {
        this.categoria = cat;
        this.descricao = desc;
        this.autorCpf = cpf;
        this.status = "Pendente";
    }

    public String getCategoria() { return categoria; }
    public String getStatus() { return status; }
}

// --- INTERFACE PRINCIPAL ---
public class SRMI_Sistema extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel container = new JPanel(cardLayout);
    
    // Simulação de Banco de Dados (Armazenamento em Memória)
    private static Map<String, Usuario> bancoUsuarios = new HashMap<>();
    private static List<Reporte> listaReportes = new ArrayList<>();
    private Usuario usuarioLogado = null;

    // Design Tokens (Identidade Visual ZeloTech Premium Dark)
    private final Color COLOR_BG = new Color(15, 23, 42);      // Navy Escuro (#0f172a)
    private final Color COLOR_CARD = new Color(30, 41, 59);    // Slate Blue (#1e293b)
    private final Color COLOR_ACCENT = new Color(16, 185, 129); // Emerald Green (#10b981)
    private final Color COLOR_TEXT = new Color(248, 250, 252);  // Off-White
    private final Color COLOR_DANGER = new Color(239, 68, 68);  // Vermelho Alerta

    public SRMI_Sistema() {
        setTitle("ZeloTech - SRMI UFRPE");
        setSize(450, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicialização das Janelas
        container.add(criarTelaLogin(), "LOGIN");
        container.add(criarTelaCadastro(), "CADASTRO");
        
        add(container);
        cardLayout.show(container, "LOGIN");
    }

    // --- TELA DE LOGIN ---
    private JPanel criarTelaLogin() {
        JPanel p = criarPainelBase();
        
        JLabel logo = new JLabel("SRMI");
        logo.setFont(new Font("SansSerif", Font.BOLD, 48));
        logo.setForeground(COLOR_ACCENT);
        logo.setAlignmentX(0.5f);
        p.add(logo);
        
        adicionarLabel(p, "Zeladoria Inteligente UFRPE", 14, Color.GRAY, false);
        p.add(Box.createVerticalStrut(40));

        JTextField txtLogin = criarCampoInput(p, "CPF ou E-mail");
        JPasswordField txtSenha = criarCampoSenha(p, "Senha");

        JButton btnEntrar = criarBotao(p, "ACESSAR SISTEMA", COLOR_ACCENT);
        btnEntrar.addActionListener(e -> {
            String login = txtLogin.getText();
            String senha = new String(txtSenha.getPassword());
            
            Usuario u = bancoUsuarios.get(login);
            if (u == null) {
                for (Usuario user : bancoUsuarios.values()) {
                    if (user.getEmail().equals(login)) { u = user; break; }
                }
            }

            if (u != null && u.getSenha().equals(senha)) {
                usuarioLogado = u;
                irParaDashboard();
            } else {
                mostrarAlerta("Dados inválidos. Verifica o teu login/senha.");
            }
        });

        JButton btnNovo = criarBotao(p, "CRIAR CONTA", COLOR_CARD);
        btnNovo.addActionListener(e -> cardLayout.show(container, "CADASTRO"));

        return p;
    }

    // --- TELA DE CADASTRO ---
    private JPanel criarTelaCadastro() {
        JPanel p = criarPainelBase();
        adicionarLabel(p, "Novo Cadastro", 24, COLOR_TEXT, true);
        p.add(Box.createVerticalStrut(30));

        JTextField txtNome = criarCampoInput(p, "Nome Completo");
        JTextField txtEmail = criarCampoInput(p, "E-mail Institucional");
        JTextField txtCpf = criarCampoInput(p, "CPF (Apenas números)");
        JPasswordField txtSenha = criarCampoSenha(p, "Senha de Acesso");

        JButton btnSalvar = criarBotao(p, "FINALIZAR REGISTRO", COLOR_ACCENT);
        btnSalvar.addActionListener(e -> {
            String cpf = txtCpf.getText();
            if (bancoUsuarios.containsKey(cpf)) {
                mostrarAlerta("Este CPF já está registado no sistema.");
            } else if (cpf.isEmpty() || txtNome.getText().isEmpty()) {
                mostrarAlerta("Campos obrigatórios em falta!");
            } else {
                bancoUsuarios.put(cpf, new Usuario(txtNome.getText(), txtEmail.getText(), cpf, new String(txtSenha.getPassword())));
                mostrarAlerta("Conta criada com sucesso! Podes fazer login.");
                cardLayout.show(container, "LOGIN");
            }
        });

        JButton btnVoltar = criarBotao(p, "VOLTAR", COLOR_CARD);
        btnVoltar.addActionListener(e -> cardLayout.show(container, "LOGIN"));

        return p;
    }

    // --- DASHBOARD DO UTILIZADOR ---
    private void irParaDashboard() {
        JPanel p = criarPainelBase();
        adicionarLabel(p, "Olá, " + usuarioLogado.getNome().split(" ")[0], 22, COLOR_TEXT, true);
        p.add(Box.createVerticalStrut(10));
        
        JLabel saldo = new JLabel(usuarioLogado.getSaldoCoins() + " ZeloCoins");
        saldo.setFont(new Font("SansSerif", Font.BOLD, 32));
        saldo.setForeground(COLOR_ACCENT);
        saldo.setAlignmentX(0.5f);
        p.add(saldo);
        p.add(Box.createVerticalStrut(50));

        JButton btnReportar = criarBotao(p, "🚨 REPORTAR PROBLEMA", COLOR_DANGER);
        btnReportar.addActionListener(e -> cardLayout.show(container, "REPORTE"));

        JButton btnSair = criarBotao(p, "SAIR", COLOR_CARD);
        btnSair.addActionListener(e -> cardLayout.show(container, "LOGIN"));

        container.add(p, "DASHBOARD");
        container.add(criarTelaReporte(), "REPORTE");
        cardLayout.show(container, "DASHBOARD");
    }

    // --- TELA DE REPORTE ---
    private JPanel criarTelaReporte() {
        JPanel p = criarPainelBase();
        adicionarLabel(p, "O que aconteceu?", 22, COLOR_TEXT, true);
        p.add(Box.createVerticalStrut(20));

        p.add(new JLabel("Categoria do Incidente:") {{ setForeground(Color.GRAY); setAlignmentX(0.5f); }});
        String[] categorias = {"Elétrico", "Hidráulico", "Mobiliário", "Estrutural", "Ar Condicionado"};
        JComboBox<String> cbCat = new JComboBox<>(categorias);
        cbCat.setMaximumSize(new Dimension(350, 40));
        cbCat.setBackground(COLOR_CARD);
        cbCat.setForeground(Color.WHITE);
        p.add(cbCat);
        p.add(Box.createVerticalStrut(20));

        p.add(new JLabel("Descrição do problema:") {{ setForeground(Color.GRAY); setAlignmentX(0.5f); }});
        JTextArea txtDesc = new JTextArea(5, 20);
        txtDesc.setBackground(COLOR_CARD);
        txtDesc.setForeground(Color.WHITE);
        txtDesc.setLineWrap(true);
        txtDesc.setCaretColor(Color.WHITE);
        JScrollPane scroll = new JScrollPane(txtDesc);
        scroll.setMaximumSize(new Dimension(350, 150));
        p.add(scroll);
        p.add(Box.createVerticalStrut(30));

        JButton btnEnviar = criarBotao(p, "ENVIAR (+50 COINS)", COLOR_ACCENT);
        btnEnviar.addActionListener(e -> {
            if (txtDesc.getText().length() < 10) {
                mostrarAlerta("A descrição é demasiado curta.");
            } else {
                listaReportes.add(new Reporte(cbCat.getSelectedItem().toString(), txtDesc.getText(), usuarioLogado.getCpf()));
                usuarioLogado.adicionarCoins(50);
                mostrarAlerta("Reporte enviado! Ganhaste 50 ZeloCoins.");
                irParaDashboard();
            }
        });

        JButton btnCancela = criarBotao(p, "CANCELAR", COLOR_CARD);
        btnCancela.addActionListener(e -> cardLayout.show(container, "DASHBOARD"));

        return p;
    }

    // --- HELPERS DE INTERFACE ---

    private JPanel criarPainelBase() {
        JPanel p = new JPanel();
        p.setBackground(COLOR_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        return p;
    }

    private void adicionarLabel(JPanel p, String text, int size, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color);
        l.setAlignmentX(0.5f);
        p.add(l);
    }

    private JTextField criarCampoInput(JPanel p, String hint) {
        p.add(new JLabel(hint) {{ setForeground(Color.GRAY); setAlignmentX(0.5f); }});
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(350, 40));
        f.setBackground(COLOR_CARD);
        f.setForeground(COLOR_TEXT);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        p.add(f);
        p.add(Box.createVerticalStrut(15));
        return f;
    }

    private JPasswordField criarCampoSenha(JPanel p, String hint) {
        p.add(new JLabel(hint) {{ setForeground(Color.GRAY); setAlignmentX(0.5f); }});
        JPasswordField f = new JPasswordField();
        f.setMaximumSize(new Dimension(350, 40));
        f.setBackground(COLOR_CARD);
        f.setForeground(COLOR_TEXT);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        p.add(f);
        p.add(Box.createVerticalStrut(15));
        return f;
    }

    private JButton criarBotao(JPanel p, String text, Color color) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(350, 50));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(0.5f);
        p.add(b);
        p.add(Box.createVerticalStrut(10));
        return b;
    }

    private void mostrarAlerta(String m) {
        JOptionPane.showMessageDialog(this, m, "SRMI ZeloTech", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Teste: CPF 123 | Senha 123
        bancoUsuarios.put("123", new Usuario("Vithor Castro", "vithor@ufrpe.br", "123", "123"));
        SwingUtilities.invokeLater(() -> new SRMI_Sistema().setVisible(true));
    }
}