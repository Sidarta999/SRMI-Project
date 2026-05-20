package com.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PROJETO SRMI - ZeloTech (UFRPE)
 * Versão: 14.0 - Sprint 3 Ready (Lógica Avançada de Negócio)
 * Funcionalidades: Design Figma, Cadastro Real, Auditoria, Loja de Trocas e IDs Únicos.
 * Integrantes: Vithor Castro e Sidarta Britto
 */

// --- COMPONENTES VISUAIS CUSTOMIZADOS ---

class RoundedPanel extends JPanel {
    private int radius;
    private Color backgroundColor;

    public RoundedPanel(int radius, Color bgColor) {
        this.radius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
    }
}

class RoundedButton extends JButton {
    private Color baseColor;
    private int radius;

    public RoundedButton(String text, Color bgColor, int radius) {
        super(text);
        this.baseColor = bgColor;
        this.radius = radius;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isPressed()) g2.setColor(baseColor.darker());
        else if (getModel().isRollover()) g2.setColor(baseColor.brighter());
        else g2.setColor(baseColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        super.paintComponent(g2);
        g2.dispose();
    }
}

class CustomField extends JTextField {
    public CustomField() {
        setOpaque(false);
        setBackground(new Color(30, 41, 59)); 
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setFont(new Font("SansSerif", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        super.paintComponent(g2);
        g2.dispose();
    }
}

// --- MODAL DE AVISO PERSONALIZADO ---

class CustomAlert extends JDialog {
    public CustomAlert(JFrame parent, String title, String message, Color titleColor) {
        super(parent, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        RoundedPanel content = new RoundedPanel(30, new Color(15, 23, 42));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(titleColor);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMsg = new JLabel("<html><center>" + message + "</center></html>");
        lblMsg.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblMsg.setForeground(new Color(248, 250, 252));
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedButton btnOk = new RoundedButton("ENTENDIDO", new Color(30, 41, 59), 15);
        btnOk.setMaximumSize(new Dimension(150, 45));
        btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOk.addActionListener(e -> dispose());

        content.add(lblTitle); content.add(Box.createVerticalStrut(20));
        content.add(lblMsg); content.add(Box.createVerticalStrut(30));
        content.add(btnOk);
        add(content); pack(); setLocationRelativeTo(parent);
    }
}

// --- MODELO DE DADOS ---

abstract class Usuario {
    protected String nome, email, cpf, senha;
    public Usuario(String n, String e, String c, String s) { nome=n; email=e; cpf=c; senha=s; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}

class UsuarioSimples extends Usuario {
    private int saldoCoins = 0;
    public UsuarioSimples(String n, String e, String c, String s) { super(n, e, c, s); }
    public int getSaldoCoins() { return saldoCoins; }
    public void adicionarCoins(int q) { this.saldoCoins += q; }
    public boolean debitarCoins(int valor) {
        if (this.saldoCoins >= valor) {
            this.saldoCoins -= valor;
            return true;
        }
        return false;
    }
}

class UsuarioAdmin extends Usuario {
    public UsuarioAdmin(String n, String e, String c, String s) { super(n, e, c, s); }
}

class Reporte {
    private String id;
    private String categoria, descricao, autorCpf, status;
    
    public Reporte(String cat, String desc, String cpf) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.categoria = cat; this.descricao = desc;
        this.autorCpf = cpf; this.status = "Pendente";
    }
    public String getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public String getAutorCpf() { return autorCpf; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
}

// --- CLASSE PRINCIPAL ---

public class SRMI_Sistema extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel container = new JPanel(cardLayout);
    
    private static Map<String, Usuario> bancoUsuarios = new HashMap<>();
    private static List<Reporte> listaReportes = new ArrayList<>();
    private Usuario usuarioLogado = null;

    private final Color COLOR_BG = new Color(15, 23, 42);      
    private final Color COLOR_CARD = new Color(30, 41, 59);    
    private final Color COLOR_ACCENT = new Color(16, 185, 129); 
    private final Color COLOR_TEXT = new Color(248, 250, 252);
    private final Color COLOR_DANGER = new Color(239, 68, 68);
    private final String ADMIN_KEY = "UFRPE2026";

    public SRMI_Sistema() {
        setTitle("SRMI - Zeladoria Inteligente UFRPE");
        setSize(480, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);

        container.add(criarTelaLogin(), "LOGIN");
        container.add(criarTelaCadastro(), "CADASTRO");
        
        add(container);
        cardLayout.show(container, "LOGIN");
    }

    private void alert(String t, String m, boolean err) {
        new CustomAlert(this, t, m, err ? COLOR_DANGER : COLOR_ACCENT).setVisible(true);
    }

    private JPanel criarPainelBase() {
        JPanel p = new JPanel(); p.setBackground(COLOR_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(40, 30, 40, 30));
        return p;
    }

    private void adicionarLabelCentral(JPanel p, String t, int s, Color c, boolean bold) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, s));
        l.setForeground(c); l.setAlignmentX(0.5f); p.add(l);
    }

    // --- TELA DE LOGIN ---

    private JPanel criarTelaLogin() {
        JPanel p = criarPainelBase();
        adicionarLabelCentral(p, "SRMI", 64, COLOR_ACCENT, true);
        adicionarLabelCentral(p, "Zeladoria Inteligente UFRPE", 14, Color.GRAY, false);
        p.add(Box.createVerticalStrut(50));

        CustomField fLog = (CustomField) adicionarCampoInput(p, "CPF ou E-mail Institucional", false);
        JPasswordField fSen = (JPasswordField) adicionarCampoInput(p, "Senha de Acesso", true);

        RoundedButton btnLog = new RoundedButton("ACESSAR SISTEMA", COLOR_ACCENT, 20);
        btnLog.setMaximumSize(new Dimension(400, 55));
        btnLog.setAlignmentX(0.5f);
        btnLog.addActionListener(e -> {
            String login = fLog.getText().trim();
            String senha = new String(fSen.getPassword()).trim();
            
            Usuario u = bancoUsuarios.get(login);
            if (u == null) {
                for (Usuario user : bancoUsuarios.values()) {
                    if (user.getEmail().equalsIgnoreCase(login)) { u = user; break; }
                }
            }
            if (u != null && u.getSenha().equals(senha)) {
                usuarioLogado = u;
                if (u instanceof UsuarioSimples) irParaDashboardAluno();
                else irParaDashboardAdmin();
            } else {
                alert("Acesso Negado", "Usuário não encontrado ou senha incorreta.", true);
            }
        });
        p.add(btnLog); p.add(Box.createVerticalStrut(10));

        RoundedButton btnIrCad = new RoundedButton("CRIAR NOVA CONTA", COLOR_CARD, 20);
        btnIrCad.setMaximumSize(new Dimension(400, 55));
        btnIrCad.setAlignmentX(0.5f);
        btnIrCad.addActionListener(e -> cardLayout.show(container, "CADASTRO"));
        p.add(btnIrCad);
        return p;
    }

    // --- TELA DE CADASTRO ---

    private JPanel criarTelaCadastro() {
        JPanel p = criarPainelBase();
        adicionarLabelCentral(p, "Novo Registro", 28, COLOR_TEXT, true);
        p.add(Box.createVerticalStrut(30));

        JTextField fNome = (JTextField) adicionarCampoInput(p, "Nome Completo", false);
        JTextField fCpf = (JTextField) adicionarCampoInput(p, "CPF (Apenas números)", false);
        JTextField fEmail = (JTextField) adicionarCampoInput(p, "E-mail Institucional (@ufrpe.br)", false);
        JPasswordField fSenha = (JPasswordField) adicionarCampoInput(p, "Defina sua Senha", true);

        adicionarLabelCentral(p, "Tipo de Perfil", 12, Color.GRAY, true);
        String[] roles = {"Estudante (Aluno)", "Técnico (Administrador)"};
        JComboBox<String> cb = new JComboBox<>(roles);
        cb.setMaximumSize(new Dimension(400, 45));
        cb.setBackground(COLOR_CARD);
        cb.setForeground(Color.WHITE);
        p.add(cb); p.add(Box.createVerticalStrut(25));

        RoundedButton btn = new RoundedButton("FINALIZAR CADASTRO", COLOR_ACCENT, 20);
        btn.setMaximumSize(new Dimension(400, 55));
        btn.setAlignmentX(0.5f);
        btn.addActionListener(e -> {
            String nome = fNome.getText().trim();
            String cpf = fCpf.getText().trim();
            String email = fEmail.getText().trim();
            String senha = new String(fSenha.getPassword()).trim();

            if (nome.isEmpty() || cpf.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                alert("Erro de Registro", "Todos os campos são obrigatórios.", true);
                return;
            }
            if (bancoUsuarios.containsKey(cpf)) {
                alert("Erro", "Este CPF já possui uma conta ativa.", true);
                return;
            }
            
            if (cb.getSelectedIndex() == 1) {
                String code = JOptionPane.showInputDialog(this, "Insira o Código de Acesso Técnico:");
                if (code == null) return;
                if (!ADMIN_KEY.equals(code)) {
                    alert("Acesso Negado", "Código técnico inválido.", true);
                    return;
                }
                bancoUsuarios.put(cpf, new UsuarioAdmin(nome, email, cpf, senha));
            } else {
                bancoUsuarios.put(cpf, new UsuarioSimples(nome, email, cpf, senha));
            }
            
            alert("Sucesso", "Conta criada com sucesso! Você já pode logar.", false);
            cardLayout.show(container, "LOGIN");
        });
        p.add(btn);
        
        RoundedButton btnVoltar = new RoundedButton("VOLTAR AO LOGIN", COLOR_CARD, 20);
        btnVoltar.addActionListener(e -> cardLayout.show(container, "LOGIN"));
        p.add(Box.createVerticalStrut(10)); p.add(btnVoltar);
        return p;
    }

    // --- DASHBOARD ALUNO ---

    private void irParaDashboardAluno() {
        UsuarioSimples aluno = (UsuarioSimples) usuarioLogado;
        JPanel p = criarPainelBase();
        adicionarLabelCentral(p, "Olá, " + aluno.getNome().split(" ")[0], 26, COLOR_TEXT, true);
        p.add(Box.createVerticalStrut(20));

        RoundedPanel card = new RoundedPanel(25, COLOR_CARD);
        card.setLayout(new GridBagLayout());
        card.setMaximumSize(new Dimension(400, 100));
        JLabel coins = new JLabel(aluno.getSaldoCoins() + " ZeloCoins");
        coins.setFont(new Font("SansSerif", Font.BOLD, 32));
        coins.setForeground(COLOR_ACCENT);
        card.add(coins);
        p.add(card); p.add(Box.createVerticalStrut(30));

        RoundedButton btnLoja = new RoundedButton("TROCAR ZELOCOINS", COLOR_ACCENT, 20);
        btnLoja.setMaximumSize(new Dimension(400, 60));
        btnLoja.addActionListener(e -> irParaLoja());
        p.add(btnLoja); p.add(Box.createVerticalStrut(15));

        RoundedButton btnReport = new RoundedButton("REPORTAR PROBLEMA", COLOR_DANGER, 20);
        btnReport.setMaximumSize(new Dimension(400, 60));
        btnReport.addActionListener(e -> irParaReporte());
        p.add(btnReport);

        p.add(Box.createVerticalStrut(20));
        adicionarLabelCentral(p, "Meu histórico de reportes:", 12, Color.GRAY, false);
        
        JPanel hist = new JPanel(); hist.setLayout(new BoxLayout(hist, BoxLayout.Y_AXIS)); hist.setBackground(COLOR_BG);
        for(Reporte r : listaReportes) {
            if(r.getAutorCpf().equals(aluno.getCpf())) {
                JLabel item = new JLabel("#" + r.getId() + " " + r.getCategoria() + " -> " + r.getStatus());
                item.setForeground(r.getStatus().equals("Aprovado") ? COLOR_ACCENT : Color.LIGHT_GRAY); 
                item.setAlignmentX(0.5f);
                hist.add(item);
            }
        }
        p.add(new JScrollPane(hist) {{ setBorder(null); getViewport().setBackground(COLOR_BG); }});

        p.add(Box.createVerticalGlue());
        RoundedButton sair = new RoundedButton("SAIR DO SISTEMA", COLOR_CARD, 20);
        sair.addActionListener(e -> { usuarioLogado = null; cardLayout.show(container, "LOGIN"); });
        p.add(sair);

        container.add(p, "DASH_ALUNO");
        cardLayout.show(container, "DASH_ALUNO");
    }

    // --- LOJA DE RECOMPENSAS ---

    private void irParaLoja() {
        JPanel p = criarPainelBase();
        adicionarLabelCentral(p, "Loja de Recompensas", 24, COLOR_ACCENT, true);
        adicionarLabelCentral(p, "Seu Saldo: " + ((UsuarioSimples)usuarioLogado).getSaldoCoins() + " ZC", 14, Color.GRAY, false);
        p.add(Box.createVerticalStrut(30));

        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 15));
        grid.setBackground(COLOR_BG);

        adicionarItemLoja(grid, "Almoço Extra", 300);
        adicionarItemLoja(grid, "Janta Extra", 200);
        adicionarItemLoja(grid, "Proteína Dobrada", 150);
        adicionarItemLoja(grid, "Sobremesa Dobrada", 100);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null); scroll.getViewport().setBackground(COLOR_BG);
        p.add(scroll); p.add(Box.createVerticalStrut(20));

        RoundedButton btnVoltar = new RoundedButton("VOLTAR AO MENU", COLOR_CARD, 20);
        btnVoltar.addActionListener(e -> irParaDashboardAluno());
        p.add(btnVoltar);

        container.add(p, "LOJA");
        cardLayout.show(container, "LOJA");
    }

    private void adicionarItemLoja(JPanel painel, String nome, int preco) {
        RoundedPanel item = new RoundedPanel(15, COLOR_CARD);
        item.setLayout(new BorderLayout(20, 0));
        item.setBorder(new EmptyBorder(15, 20, 15, 20));
        item.setMaximumSize(new Dimension(400, 75));

        JLabel info = new JLabel("<html><b>" + nome + "</b><br><font color='#10b981'>" + preco + " ZeloCoins</font></html>");
        info.setForeground(COLOR_TEXT);
        item.add(info, BorderLayout.WEST);

        RoundedButton btnTrocar = new RoundedButton("TROCAR", COLOR_ACCENT, 10);
        btnTrocar.addActionListener(e -> {
            UsuarioSimples aluno = (UsuarioSimples) usuarioLogado;
            if (aluno.debitarCoins(preco)) {
                alert("Troca Realizada", "Você adquiriu: " + nome + ".\nSaldo atualizado: " + aluno.getSaldoCoins(), false);
                irParaLoja();
            } else {
                alert("Saldo Insuficiente", "Você precisa de mais " + (preco - aluno.getSaldoCoins()) + " ZeloCoins.", true);
            }
        });
        item.add(btnTrocar, BorderLayout.EAST);
        painel.add(item);
    }

    // --- REPORTE E ADMIN ---

    private void irParaReporte() {
        JPanel p = criarPainelBase();
        adicionarLabelCentral(p, "Novo Reporte", 22, COLOR_TEXT, true);
        p.add(Box.createVerticalStrut(20));
        String[] cats = {"Elétrico", "Hidráulico", "Mobiliário", "Estrutural", "Ar Condicionado"};
        JComboBox<String> cat = new JComboBox<>(cats);
        cat.setMaximumSize(new Dimension(400, 45));
        p.add(cat); p.add(Box.createVerticalStrut(20));
        JTextArea desc = new JTextArea(6, 20);
        desc.setBackground(COLOR_CARD); desc.setForeground(Color.WHITE);
        desc.setCaretColor(Color.WHITE); desc.setLineWrap(true);
        p.add(new JScrollPane(desc)); p.add(Box.createVerticalStrut(20));
        RoundedButton env = new RoundedButton("ENVIAR PARA ANÁLISE", COLOR_ACCENT, 20);
        env.addActionListener(e -> {
            if (desc.getText().trim().length() < 10) {
                alert("Descrição Curta", "Por favor, detalhe melhor o problema (mín. 10 caracteres).", true);
                return;
            }
            listaReportes.add(new Reporte(cat.getSelectedItem().toString(), desc.getText(), usuarioLogado.getCpf()));
            alert("Sucesso", "Reporte enviado! Aguarde a validação técnica para receber as moedas.", false);
            irParaDashboardAluno();
        });
        p.add(env);
        RoundedButton can = new RoundedButton("CANCELAR", COLOR_CARD, 20);
        can.addActionListener(e -> irParaDashboardAluno());
        p.add(Box.createVerticalStrut(10)); p.add(can);
        container.add(p, "REPORTE"); cardLayout.show(container, "REPORTE");
    }

    private void irParaDashboardAdmin() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(COLOR_BG);
        p.setBorder(new EmptyBorder(40, 20, 40, 20));
        adicionarLabelCentral(p, "Auditoria Técnica - UFRPE", 22, COLOR_ACCENT, true);
        JPanel list = new JPanel(); list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS)); list.setBackground(COLOR_BG);
        for (Reporte r : listaReportes) {
            if (r.getStatus().equals("Pendente")) {
                RoundedPanel item = new RoundedPanel(18, COLOR_CARD);
                item.setLayout(new BorderLayout(15, 0));
                item.setMaximumSize(new Dimension(500, 90));
                item.setBorder(new EmptyBorder(15, 15, 15, 15));
                item.add(new JLabel("<html><b>#" + r.getId() + " - " + r.getCategoria() + "</b><br><small>Autor: " + r.getAutorCpf() + "</small></html>") {{ setForeground(COLOR_TEXT); }}, BorderLayout.CENTER);
                JPanel acoes = new JPanel(new FlowLayout()); acoes.setOpaque(false);
                RoundedButton ok = new RoundedButton("OK", COLOR_ACCENT, 12);
                ok.addActionListener(e -> {
                    r.setStatus("Aprovado");
                    Usuario u = bancoUsuarios.get(r.getAutorCpf());
                    if (u instanceof UsuarioSimples) ((UsuarioSimples) u).adicionarCoins(50);
                    alert("Validado", "Reporte aprovado e saldo creditado.", false);
                    irParaDashboardAdmin();
                });
                RoundedButton del = new RoundedButton("X", COLOR_DANGER, 12);
                del.addActionListener(e -> { r.setStatus("Recusado"); irParaDashboardAdmin(); });
                acoes.add(ok); acoes.add(del);
                item.add(acoes, BorderLayout.EAST);
                list.add(item); list.add(Box.createVerticalStrut(10));
            }
        }
        p.add(new JScrollPane(list) {{ setBorder(null); getViewport().setBackground(COLOR_BG); }}, BorderLayout.CENTER);
        p.add(new RoundedButton("SAIR", COLOR_CARD, 20) {{ addActionListener(e -> { usuarioLogado = null; cardLayout.show(container, "LOGIN"); }); }}, BorderLayout.SOUTH);
        container.add(p, "DASH_ADMIN"); cardLayout.show(container, "DASH_ADMIN");
    }

    // --- HELPERS ---

    private JComponent adicionarCampoInput(JPanel p, String label, boolean isSenha) {
        JLabel l = new JLabel(label); l.setForeground(Color.GRAY); l.setAlignmentX(0.5f);
        p.add(l);
        JComponent f;
        if (isSenha) {
            f = new JPasswordField() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(30, 41, 59)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    super.paintComponent(g); g2.dispose();
                }
            };
            f.setOpaque(false); f.setForeground(Color.WHITE); f.setBorder(new EmptyBorder(10, 15, 10, 15));
        } else {
            f = new CustomField();
        }
        f.setMaximumSize(new Dimension(400, 45));
        p.add(f); p.add(Box.createVerticalStrut(15));
        return f;
    }

    public static void main(String[] args) {
        bancoUsuarios.put("123", new UsuarioSimples("Vithor Castro", "v@ufrpe.br", "123", "123"));
        bancoUsuarios.put("999", new UsuarioAdmin("Admin UFRPE", "admin@ufrpe.br", "999", "999"));
        SwingUtilities.invokeLater(() -> new SRMI_Sistema().setVisible(true));
    }
}