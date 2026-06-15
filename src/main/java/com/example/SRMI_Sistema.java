package com.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

/**
 * PROJETO SRMI - ZeloTech (UFRPE)
 * Versão: 17.0 - Integração Assíncrona Total com Cloud Firestore & Fotos em Base64
 */

@SuppressWarnings("unused")
// --- COMPONENTES VISUAIS CUSTOMIZADOS ---

class RoundedPanel extends JPanel {
    private static final long serialVersionUID = 1L;
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
    private static final long serialVersionUID = 1L;
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
    private static final long serialVersionUID = 1L;

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
    private static final long serialVersionUID = 1L;

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
    public void setSaldoCoins(int q) { this.saldoCoins = q; }
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
    private String categoria, descricao, autorCpf, status, localizacao;
    private List<String> fotos; 
    
    public Reporte(String cat, String desc, String cpf, String localizacao) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.categoria = cat; 
        this.descricao = desc;
        this.autorCpf = cpf; 
        this.status = "Pendente";
        this.localizacao = localizacao;
        this.fotos = new ArrayList<>();
    }
    
    public Reporte(String id, String cat, String desc, String cpf, String status, String localizacao, List<String> fotos) {
        this.id = id;
        this.categoria = cat;
        this.descricao = desc;
        this.autorCpf = cpf;
        this.status = status;
        this.localizacao = localizacao;
        this.fotos = fotos != null ? fotos : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public String getAutorCpf() { return autorCpf; }
    public String getStatus() { return status; }
    public String getLocalizacao() { return localizacao; }
    public List<String> getFotos() { return fotos; }
    
    public void setStatus(String s) { this.status = s; }
    public void adicionarFoto(String base64Foto) { this.fotos.add(base64Foto); }
    public void aprovar() { this.status = "Aprovado"; }
    public void recusar() { this.status = "Recusado"; }
}

// --- CLASSE PRINCIPAL ---

public class SRMI_Sistema extends JFrame {
    private static final long serialVersionUID = 1L;

    private CardLayout cardLayout = new CardLayout();
    private JPanel container = new JPanel(cardLayout);
    private Usuario usuarioLogado = null;

    private final Color COLOR_BG = new Color(15, 23, 42);      
    private final Color COLOR_CARD = new Color(30, 41, 59);    
    private final Color COLOR_ACCENT = new Color(16, 185, 129); 
    private final Color COLOR_TEXT = new Color(248, 250, 252);
    private final Color COLOR_DANGER = new Color(239, 68, 68);
    private final Color COLOR_SIDEBAR = new Color(9, 15, 29);
    private final String ADMIN_KEY = "UFRPE2026";

    public SRMI_Sistema() {
        setTitle("SRMI - Zeladoria Inteligente UFRPE");
        setSize(850, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);

        container.add(criarTelaLogin(), "LOGIN");
        container.add(criarTelaCadastro(), "CADASTRO");
        
        add(container);
        cardLayout.show(container, "LOGIN");
    }

    private void navegarPara(String tela) {
        cardLayout.show(container, tela);
        container.revalidate();
        container.repaint();
    }

    private void alert(String t, String m, boolean err) {
        new CustomAlert(this, t, m, err ? COLOR_DANGER : COLOR_ACCENT).setVisible(true);
    }

    private static String getStringOrDefault(DocumentSnapshot document, String field) {
        return Objects.requireNonNullElse(document.getString(field), "");
    }

    private static String safeString(String value) {
        return Objects.requireNonNullElse(value, "");
    }

    private static List<String> safeStringList(List<String> value) {
        return value == null ? List.of() : new ArrayList<>(value);
    }

    private static List<String> getStringListOrEmpty(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        if (value instanceof List<?> listValue) {
            List<String> result = new ArrayList<>();
            for (Object item : listValue) {
                if (item instanceof String stringItem) {
                    result.add(stringItem);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private JPanel criarPainelBase() {
        JPanel p = new JPanel(); p.setBackground(COLOR_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(40, 50, 40, 50));
        return p;
    }

    private void adicionarLabelCentral(JPanel p, String t, int s, Color c, boolean bold) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, s));
        l.setForeground(c); l.setAlignmentX(0.5f); p.add(l);
    }

    boolean emailValido(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@ufrpe\\.br$");
    }

    JScrollPane criarScrollPane(JPanel painel) {
        JScrollPane scroll = new JScrollPane(painel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_BG);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // --- SINCRONIZADORES DO FIREBASE (THREADS DE SEGUNDO PLANO) ---

    private void atualizarSaldoFirebase(String cpf, int novoSaldo, Runnable aoConcluir) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Firestore db = FirestoreClient.getFirestore();
                String usuarioCpf = Objects.requireNonNull(cpf, "cpf do usuário");
                db.collection("usuarios").document(usuarioCpf).update("saldoCoins", novoSaldo).get();
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    if (aoConcluir != null) aoConcluir.run();
                } catch (Exception ex) {
                    alert("Erro de Rede", "Não foi possível atualizar as moedas na nuvem.", true);
                }
            }
        }.execute();
    }

    private void atualizarStatusChamadoFirebase(Reporte r, String novoStatus, int recompensaCoins, Runnable aoConcluir) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Firestore db = FirestoreClient.getFirestore();
                String chamadoId = Objects.requireNonNull(r.getId(), "id do chamado");
                String autorCpf = Objects.requireNonNull(r.getAutorCpf(), "cpf do autor");

                db.collection("chamados").document(chamadoId).update("status", novoStatus).get();

                if (recompensaCoins > 0) {
                    DocumentSnapshot userDoc = db.collection("usuarios").document(autorCpf).get().get();
                    if (userDoc.exists()) {
                        Long saldoAtual = userDoc.getLong("saldoCoins");
                        long saldo = saldoAtual != null ? saldoAtual : 0L;
                        db.collection("usuarios").document(autorCpf).update("saldoCoins", saldo + recompensaCoins).get();
                    }
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    if (aoConcluir != null) aoConcluir.run();
                } catch (Exception ex) {
                    alert("Erro de Sincronização", "Não foi possível atualizar o chamado na nuvem.", true);
                }
            }
        }.execute();
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

            if (login.isEmpty() || senha.isEmpty()) {
                alert("Campos Vazios", "Insera suas credenciais para continuar.", true);
                return;
            }

            btnLog.setEnabled(false);
            btnLog.setText("AUTENTICANDO...");

            new SwingWorker<Usuario, Void>() {
                @Override
                protected Usuario doInBackground() throws Exception {
                    Firestore db = FirestoreClient.getFirestore();
                    DocumentSnapshot doc = db.collection("usuarios").document(login).get().get();
                    
                    if (!doc.exists()) {
                        QuerySnapshot query = db.collection("usuarios").whereEqualTo("email", login).get().get();
                        if (!query.isEmpty()) {
                            doc = query.getDocuments().get(0);
                        }
                    }

                    if (doc.exists() && senha.equals(doc.getString("senha"))) {
                        String tipo = doc.getString("tipo");
                        String nome = doc.getString("nome");
                        String email = doc.getString("email");
                        String cpf = doc.getId();
                        
                        if ("Admin".equals(tipo)) {
                            return new UsuarioAdmin(nome, email, cpf, senha);
                        } else {
                            UsuarioSimples uSimples = new UsuarioSimples(nome, email, cpf, senha);
                            Long saldoCoins = doc.getLong("saldoCoins");
                            long coins = saldoCoins != null ? saldoCoins : 0L;
                            uSimples.setSaldoCoins((int) coins);
                            return uSimples;
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    btnLog.setEnabled(true);
                    btnLog.setText("ACESSAR SISTEMA");
                    try {
                        Usuario u = get();
                        if (u != null) {
                            usuarioLogado = u;
                            fLog.setText("");
                            fSen.setText("");
                            if (u instanceof UsuarioSimples) irParaDashboardAluno();
                            else irParaDashboardAdmin();
                        } else {
                            alert("Acesso Negado", "Usuário não cadastrado ou senha inválida.", true);
                        }
                    } catch (Exception ex) {
                        alert("Erro de Rede", "Erro ao conectar com o banco de dados.", true);
                    }
                }
            }.execute();
        });
        p.add(btnLog); p.add(Box.createVerticalStrut(10));

        RoundedButton btnIrCad = new RoundedButton("CRIAR NOVA CONTA", COLOR_CARD, 20);
        btnIrCad.setMaximumSize(new Dimension(400, 55));
        btnIrCad.setAlignmentX(0.5f);
        btnIrCad.addActionListener(e -> navegarPara("CADASTRO"));
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
            if (!emailValido(email)) {
                alert("E-mail inválido", "Informe um e-mail institucional válido no formato usuario@ufrpe.br.", true);
                return;
            }

            String tipoPerfil = (cb.getSelectedIndex() == 1) ? "Admin" : "Simples";

            if ("Admin".equals(tipoPerfil)) {
                String code = JOptionPane.showInputDialog(this, "Insira o Código de Acesso Técnico:");
                if (code == null) return;
                if (!ADMIN_KEY.equals(code)) {
                    alert("Acesso Negado", "Código técnico inválido.", true);
                    return;
                }
            }

            btn.setEnabled(false);
            btn.setText("SALVANDO REGISTRO...");

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    Firestore db = FirestoreClient.getFirestore();
                    DocumentSnapshot doc = db.collection("usuarios").document(cpf).get().get();
                    if (doc.exists()) {
                        return false; 
                    }

                    Map<String, Object> dadosUser = new HashMap<>();
                    dadosUser.put("nome", nome);
                    dadosUser.put("email", email);
                    dadosUser.put("senha", senha);
                    dadosUser.put("tipo", tipoPerfil);
                    if ("Simples".equals(tipoPerfil)) {
                        dadosUser.put("saldoCoins", 0);
                    }

                    db.collection("usuarios").document(cpf).set(dadosUser).get();
                    return true;
                }

                @Override
                protected void done() {
                    btn.setEnabled(true);
                    btn.setText("FINALIZAR CADASTRO");
                    try {
                        if (get()) {
                            alert("Sucesso", "Conta criada com sucesso!", false);
                            fNome.setText(""); fCpf.setText(""); fEmail.setText(""); fSenha.setText("");
                            navegarPara("LOGIN");
                        } else {
                            alert("Erro", "Este CPF já possui uma conta ativa.", true);
                        }
                    } catch (Exception ex) {
                        alert("Erro de Rede", "Não foi possível salvar os dados no Firebase.", true);
                    }
                }
            }.execute();
        });
        p.add(btn);
        
        RoundedButton btnVoltar = new RoundedButton("VOLTAR AO LOGIN", COLOR_CARD, 20);
        btnVoltar.addActionListener(e -> navegarPara("LOGIN"));
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

        new SwingWorker<List<Reporte>, Void>() {
            @Override
            protected List<Reporte> doInBackground() throws Exception {
                List<Reporte> userReports = new ArrayList<>();
                Firestore db = FirestoreClient.getFirestore();
                QuerySnapshot query = db.collection("chamados")
                                        .whereEqualTo("autorCpf", aluno.getCpf())
                                        .get().get();
                for (QueryDocumentSnapshot doc : query) {
                    List<String> fotosList = getStringListOrEmpty(doc, "fotos");
                    userReports.add(new Reporte(
                        doc.getId(),
                        getStringOrDefault(doc, "categoria"),
                        getStringOrDefault(doc, "descricao"),
                        getStringOrDefault(doc, "autorCpf"),
                        getStringOrDefault(doc, "status"),
                        getStringOrDefault(doc, "localizacao"),
                        fotosList
                    ));
                }
                return userReports;
            }

            @Override
            protected void done() {
                try {
                    List<Reporte> resultados = get();
                    for(Reporte r : resultados) {
                        JLabel item = new JLabel("#" + r.getId() + " " + r.getCategoria() + " -> " + r.getStatus());
                        item.setForeground(r.getStatus().equals("Aprovado") ? COLOR_ACCENT : 
                                         r.getStatus().equals("Recusado") ? COLOR_DANGER : Color.LIGHT_GRAY); 
                        item.setFont(new Font("SansSerif", Font.PLAIN, 13));
                        item.setAlignmentX(0.5f);
                        hist.add(item);
                        hist.add(Box.createVerticalStrut(5));
                    }
                    hist.revalidate();
                    hist.repaint();
                } catch (Exception ex) {
                    JLabel lblErr = new JLabel("Falha ao recuperar histórico.");
                    lblErr.setForeground(COLOR_DANGER);
                    lblErr.setAlignmentX(0.5f);
                    hist.add(lblErr);
                }
            }
        }.execute();

        JScrollPane scrollHistorico = criarScrollPane(hist);
        scrollHistorico.setPreferredSize(new Dimension(0, 180));
        p.add(scrollHistorico);

        p.add(Box.createVerticalGlue());
        RoundedButton sair = new RoundedButton("SAIR DO SISTEMA", COLOR_CARD, 20);
        sair.addActionListener(e -> { usuarioLogado = null; navegarPara("LOGIN"); });
        p.add(sair);

        container.add(p, "DASH_ALUNO");
        navegarPara("DASH_ALUNO");
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
        navegarPara("LOJA");
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
                btnTrocar.setEnabled(false);
                atualizarSaldoFirebase(aluno.getCpf(), aluno.getSaldoCoins(), () -> {
                    alert("Troca Realizada", "Você adquiriu: " + nome, false);
                    irParaLoja();
                });
            } else {
                alert("Saldo Insuficiente", "Você precisa de mais moedas.", true);
            }
        });
        item.add(btnTrocar, BorderLayout.EAST);
        painel.add(item);
    }

    // --- NOVO REPORTE ESTRUTURAL ---

    private void irParaReporte() {
        JPanel p = new JPanel();
        p.setBackground(COLOR_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        List<String> fotosBase64 = new ArrayList<>(); 

        adicionarLabelCentral(p, "Denúncia Estrutural", 24, COLOR_TEXT, true);
        adicionarLabelCentral(p, "Zeladoria de Infraestrutura UFRPE", 13, Color.GRAY, false);
        p.add(Box.createVerticalStrut(30));
        
        JLabel lblCat = new JLabel("Categoria do Problema:");
        lblCat.setForeground(COLOR_TEXT); lblCat.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblCat.setAlignmentX(0.5f); p.add(lblCat); p.add(Box.createVerticalStrut(5));
        
        String[] cats = {"⚠️ Estrutural (Rachaduras/Infiltrações)", "🔌 Falha Elétrica", "🚰 Vazamento Hidráulico", "🪑 Danos em Mobiliário"};
        JComboBox<String> cat = new JComboBox<>(cats);
        cat.setMaximumSize(new Dimension(450, 40));
        cat.setBackground(COLOR_CARD); cat.setForeground(Color.WHITE);
        cat.setAlignmentX(0.5f); p.add(cat); p.add(Box.createVerticalStrut(15));
        
        JLabel lblLoc = new JLabel("Onde fica o problem? (Prédio/Sala):");
        lblLoc.setForeground(COLOR_TEXT); lblLoc.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblLoc.setAlignmentX(0.5f); p.add(lblLoc); p.add(Box.createVerticalStrut(5));
        
        CustomField txtLocal = new CustomField();
        txtLocal.setMaximumSize(new Dimension(450, 40));
        txtLocal.setAlignmentX(0.5f); p.add(txtLocal); p.add(Box.createVerticalStrut(15));

        JLabel lblDesc = new JLabel("Descrição Detalhada:");
        lblDesc.setForeground(COLOR_TEXT); lblDesc.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblDesc.setAlignmentX(0.5f); p.add(lblDesc); p.add(Box.createVerticalStrut(5));
        
        JTextArea desc = new JTextArea(5, 20);
        desc.setBackground(COLOR_CARD); desc.setForeground(Color.WHITE);
        desc.setCaretColor(Color.WHITE); desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollDesc = new JScrollPane(desc);
        scrollDesc.setBorder(null);
        scrollDesc.setMaximumSize(new Dimension(450, 100));
        scrollDesc.setAlignmentX(0.5f); p.add(scrollDesc); p.add(Box.createVerticalStrut(15));
        
        RoundedPanel uploadBox = new RoundedPanel(15, COLOR_CARD);
        uploadBox.setLayout(new BoxLayout(uploadBox, BoxLayout.Y_AXIS));
        uploadBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        uploadBox.setMaximumSize(new Dimension(450, 80));
        uploadBox.setAlignmentX(0.5f);
        
        JLabel lblContadorFotos = new JLabel("Nenhuma foto anexada.");
        lblContadorFotos.setForeground(Color.LIGHT_GRAY);
        lblContadorFotos.setAlignmentX(0.5f);
        
        RoundedButton btnAnexar = new RoundedButton("📸 ANEXAR FOTO", new Color(59, 130, 246), 10);
        btnAnexar.setAlignmentX(0.5f);
        btnAnexar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Selecione uma imagem");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Imagens suportadas (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                btnAnexar.setEnabled(false);
                btnAnexar.setText("PROCESSANDO IMAGEM...");
                
                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        return Base64.getEncoder().encodeToString(bytes);
                    }
                    @Override
                    protected void done() {
                        try {
                            String base64Str = get();
                            fotosBase64.add(base64Str);
                            lblContadorFotos.setText(fotosBase64.size() + " foto(s) anexada(s) com sucesso.");
                        } catch (Exception ex) {
                            alert("Falha na Leitura", "Não foi possível processar este arquivo de imagem.", true);
                        } finally {
                            btnAnexar.setEnabled(true);
                            btnAnexar.setText("📸 ANEXAR FOTO");
                        }
                    }
                }.execute();
            }
        });
        
        uploadBox.add(btnAnexar);
        uploadBox.add(Box.createVerticalStrut(5));
        uploadBox.add(lblContadorFotos);
        
        p.add(uploadBox); p.add(Box.createVerticalStrut(30));
        
        RoundedButton env = new RoundedButton("ENVIAR PARA ANÁLISE", COLOR_ACCENT, 20);
        env.setMaximumSize(new Dimension(450, 50));
        env.setAlignmentX(0.5f);
        env.addActionListener(e -> {
            if (desc.getText().trim().length() < 10) {
                alert("Descrição Curta", "Por favor, detalhe melhor o problema.", true);
                return;
            }
            String loc = txtLocal.getText().trim().isEmpty() ? "Geral/Não Informado" : txtLocal.getText().trim();
            Reporte novoReporte = new Reporte(cat.getSelectedItem().toString(), desc.getText(), usuarioLogado.getCpf(), loc);
            for (String b64 : fotosBase64) novoReporte.adicionarFoto(b64);
            
            env.setEnabled(false);
            env.setText("ENVIANDO CHAMADO...");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Firestore db = FirestoreClient.getFirestore();
                    Map<String, Object> dados = new HashMap<>();
                    String categoria = safeString(novoReporte.getCategoria());
                    String descricao = safeString(novoReporte.getDescricao());
                    String localizacao = safeString(novoReporte.getLocalizacao());
                    String status = safeString(novoReporte.getStatus());
                    String autorCpf = safeString(novoReporte.getAutorCpf());
                    List<String> fotos = safeStringList(novoReporte.getFotos());

                    dados.put("categoria", categoria);
                    dados.put("descricao", descricao);
                    dados.put("localizacao", localizacao);
                    dados.put("status", status);
                    dados.put("autorCpf", autorCpf);
                    dados.put("fotos", fotos);

                    db.collection("chamados").document(novoReporte.getId()).set(dados).get();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        alert("Sucesso", "Reporte cadastrado com sucesso!", false);
                        irParaDashboardAluno();
                    } catch (Exception ex) {
                        env.setEnabled(true);
                        env.setText("ENVIAR PARA ANÁLISE");
                        alert("Erro de Rede", "Falha ao enviar para nuvem: " + ex.getMessage(), true);
                    }
                }
            }.execute();
        });
        p.add(env); p.add(Box.createVerticalStrut(10));
        
        RoundedButton can = new RoundedButton("CANCELAR", COLOR_CARD, 20);
        can.setMaximumSize(new Dimension(450, 50));
        can.setAlignmentX(0.5f);
        can.addActionListener(e -> irParaDashboardAluno());
        p.add(can);
        
        container.add(p, "REPORTE"); 
        navegarPara("REPORTE");
    }

    // --- FUNÇÃO AUXILIAR PARA GERAR A BARRA LATERAL ADMIN ---
    private JPanel criarSidebarAdmin(String abaAtiva) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(200, 800));
        sidebar.setBorder(new EmptyBorder(30, 15, 30, 15));

        JLabel logo = new JLabel("SRMI Painel");
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setForeground(COLOR_ACCENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(50));

        RoundedButton btnChamados = new RoundedButton("📥 Chamados UFRPE", COLOR_SIDEBAR, 10);
        btnChamados.setMaximumSize(new Dimension(180, 40));
        btnChamados.setAlignmentX(Component.CENTER_ALIGNMENT);
        if(abaAtiva.equals("CHAMADOS")) btnChamados.setForeground(COLOR_ACCENT);
        btnChamados.addActionListener(e -> irParaDashboardAdmin());
        sidebar.add(btnChamados); sidebar.add(Box.createVerticalStrut(12));

        RoundedButton btnHistorico = new RoundedButton("🗄️ Histórico", COLOR_SIDEBAR, 10);
        btnHistorico.setMaximumSize(new Dimension(180, 40));
        btnHistorico.setAlignmentX(Component.CENTER_ALIGNMENT);
        if(abaAtiva.equals("HISTORICO")) btnHistorico.setForeground(COLOR_ACCENT);
        btnHistorico.addActionListener(e -> irParaHistoricoAdmin());
        sidebar.add(btnHistorico); sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(Box.createVerticalGlue());
        
        RoundedButton logout = new RoundedButton("🚪 DESLOGAR", COLOR_DANGER, 12);
        logout.setMaximumSize(new Dimension(160, 40));
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.addActionListener(e -> { usuarioLogado = null; navegarPara("LOGIN"); });
        sidebar.add(logout);

        return sidebar;
    }

    // --- DASHBOARD ADMIN COMPLETO ---

    private void irParaDashboardAdmin() {
        JPanel mainLayout = new JPanel(new BorderLayout());
        mainLayout.setBackground(COLOR_BG);
        mainLayout.add(criarSidebarAdmin("CHAMADOS"), BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_BG);
        content.setBorder(new EmptyBorder(30, 25, 30, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Auditoria de Infraestrutura");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(COLOR_TEXT);
        JLabel sub = new JLabel("Fila técnica de monitoramento de riscos");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(Color.GRAY);
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        content.add(header);
        content.add(Box.createVerticalStrut(25));

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(COLOR_BG);

        new SwingWorker<List<Reporte>, Void>() {
            private long pendentes = 0, resolvidos = 0;
            @Override
            protected List<Reporte> doInBackground() throws Exception {
                List<Reporte> lista = new ArrayList<>();
                Firestore db = FirestoreClient.getFirestore();
                QuerySnapshot snapshot = db.collection("chamados").get().get();
                for (QueryDocumentSnapshot doc : snapshot) {
                    String st = getStringOrDefault(doc, "status");
                    if ("Pendente".equals(st)) pendentes++;
                    else resolvidos++;

                    List<String> f = getStringListOrEmpty(doc, "fotos");
                    lista.add(new Reporte(
                        doc.getId(), getStringOrDefault(doc, "categoria"), getStringOrDefault(doc, "descricao"),
                        getStringOrDefault(doc, "autorCpf"), st, getStringOrDefault(doc, "localizacao"), f
                    ));
                }
                return lista;
            }

            @Override
            protected void done() {
                try {
                    List<Reporte> todosReportes = get();
                    
                    JPanel kpiGrid = new JPanel(new GridLayout(1, 2, 20, 0));
                    kpiGrid.setOpaque(false); kpiGrid.setMaximumSize(new Dimension(550, 75));

                    RoundedPanel cardPnd = new RoundedPanel(15, COLOR_CARD);
                    cardPnd.setLayout(new BorderLayout()); cardPnd.setBorder(new EmptyBorder(10,15,10,15));
                    cardPnd.add(new JLabel("Aguardando Análise") {{ setForeground(Color.LIGHT_GRAY); }}, BorderLayout.NORTH);
                    cardPnd.add(new JLabel(String.valueOf(pendentes)) {{ setFont(new Font("SansSerif", Font.BOLD, 24)); setForeground(COLOR_DANGER); }}, BorderLayout.SOUTH);

                    RoundedPanel cardApr = new RoundedPanel(15, COLOR_CARD);
                    cardApr.setLayout(new BorderLayout()); cardApr.setBorder(new EmptyBorder(10,15,10,15));
                    cardApr.add(new JLabel("Casos Resolvidos") {{ setForeground(Color.LIGHT_GRAY); }}, BorderLayout.NORTH);
                    cardApr.add(new JLabel(String.valueOf(resolvidos)) {{ setFont(new Font("SansSerif", Font.BOLD, 24)); setForeground(COLOR_ACCENT); }}, BorderLayout.SOUTH);

                    kpiGrid.add(cardPnd); kpiGrid.add(cardApr);
                    content.add(kpiGrid, 1); 

                    boolean temChamado = false;
                    for (Reporte r : todosReportes) {
                        if (r.getStatus().equals("Pendente")) {
                            temChamado = true;
                            RoundedPanel item = new RoundedPanel(15, COLOR_CARD);
                            item.setLayout(new BorderLayout(15, 0));
                            item.setMaximumSize(new Dimension(550, 90));
                            item.setBorder(new EmptyBorder(12, 15, 12, 15));

                            String dadosHtml = "<html><b style='font-size:13px; color:#10b981;'>" + r.getCategoria() + "</b>" +
                                               "<br><span style='color:#A0AEC0; font-size:11px;'>📍 Local: " + r.getLocalizacao() + "</span>" +
                                               "<br><span style='color:#E2E8F0; font-size:11px;'>📸 Fotos anexadas: " + r.getFotos().size() + "</span></html>";
                            
                            JLabel lblDados = new JLabel(dadosHtml);
                            item.add(lblDados, BorderLayout.CENTER);

                            JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));
                            acoes.setOpaque(false);
                            
                            RoundedButton btnVerMais = new RoundedButton("VER MAIS", new Color(59, 130, 246), 10);
                            btnVerMais.addActionListener(e -> abrirDetalhesReporte(r, () -> irParaDashboardAdmin()));
                            
                            acoes.add(btnVerMais);
                            item.add(acoes, BorderLayout.EAST);

                            listContainer.add(item);
                            listContainer.add(Box.createVerticalStrut(12));
                        }
                    }

                    if (!temChamado) {
                        JLabel lblVazio = new JLabel("✨ Tudo limpo! Nenhum problema estrutural pendente.");
                        lblVazio.setForeground(COLOR_ACCENT);
                        lblVazio.setFont(new Font("SansSerif", Font.ITALIC, 13));
                        listContainer.add(lblVazio);
                    }
                    listContainer.revalidate();
                    listContainer.repaint();

                } catch (Exception ex) {
                    listContainer.add(new JLabel("Erro ao carregar chamados da nuvem."));
                }
            }
        }.execute();

        content.add(Box.createVerticalStrut(30));
        JLabel lblLista = new JLabel("Solicitações na Fila Técnica:");
        lblLista.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblLista.setForeground(Color.LIGHT_GRAY);
        content.add(lblLista);
        content.add(Box.createVerticalStrut(10));

        JScrollPane scroll = criarScrollPane(listContainer);
        scroll.setAlignmentX(0.0f);
        content.add(scroll);

        mainLayout.add(content, BorderLayout.CENTER);
        container.add(mainLayout, "DASH_ADMIN"); 
        navegarPara("DASH_ADMIN");
    }

    // --- HISTÓRICO ADMIN ---

    private void irParaHistoricoAdmin() {
        JPanel mainLayout = new JPanel(new BorderLayout());
        mainLayout.setBackground(COLOR_BG);
        mainLayout.add(criarSidebarAdmin("HISTORICO"), BorderLayout.WEST);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(COLOR_BG);
        content.setBorder(new EmptyBorder(30, 25, 30, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Histórico de Chamados");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(COLOR_TEXT);
        header.add(title, BorderLayout.NORTH);

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(COLOR_BG);

        new SwingWorker<List<Reporte>, Void>() {
            @Override
            protected List<Reporte> doInBackground() throws Exception {
                List<Reporte> lista = new ArrayList<>();
                Firestore db = FirestoreClient.getFirestore();
                QuerySnapshot sn = db.collection("chamados").get().get();
                for (QueryDocumentSnapshot doc : sn) {
                    String status = getStringOrDefault(doc, "status");
                    if (!"Pendente".equals(status)) {
                        lista.add(new Reporte(
                            doc.getId(), getStringOrDefault(doc, "categoria"), getStringOrDefault(doc, "descricao"),
                            getStringOrDefault(doc, "autorCpf"), status, getStringOrDefault(doc, "localizacao"),
                            getStringListOrEmpty(doc, "fotos")
                        ));
                    }
                }
                return lista;
            }

            @Override
            protected void done() {
                try {
                    List<Reporte> historico = get();
                    for (Reporte r : historico) {
                        RoundedPanel item = new RoundedPanel(15, COLOR_CARD);
                        item.setLayout(new BorderLayout(15, 0));
                        item.setMaximumSize(new Dimension(550, 80));
                        item.setBorder(new EmptyBorder(12, 15, 12, 15));

                        String color = r.getStatus().equals("Aprovado") ? "#10b981" : "#ef4444";
                        String dadosHtml = "<html><b style='font-size:13px; color:" + color + ";'>[" + r.getStatus().toUpperCase() + "] " + r.getCategoria() + "</b>" +
                                           "<br><span style='color:#A0AEC0; font-size:11px;'>📍 Local: " + r.getLocalizacao() + "</span></html>";

                        JLabel lblDados = new JLabel(dadosHtml);
                        item.add(lblDados, BorderLayout.CENTER);

                        RoundedButton btnVerMais = new RoundedButton("VER", new Color(59, 130, 246), 10);
                        btnVerMais.addActionListener(e -> abrirDetalhesReporte(r, () -> irParaHistoricoAdmin()));
                        item.add(btnVerMais, BorderLayout.EAST);

                        listContainer.add(item);
                        listContainer.add(Box.createVerticalStrut(12));
                    }
                    listContainer.revalidate();
                    listContainer.repaint();
                } catch(Exception ex) {
                    listContainer.add(new JLabel("Erro ao resgatar histórico do banco."));
                }
            }
        }.execute();

        JScrollPane scroll = criarScrollPane(listContainer);
        scroll.setAlignmentX(0.0f);
        scroll.setPreferredSize(new Dimension(680, 420));

        JPanel painelCentral = new JPanel();
        painelCentral.setLayout(new BoxLayout(painelCentral, BoxLayout.Y_AXIS));
        painelCentral.setOpaque(false);
        painelCentral.setPreferredSize(new Dimension(700, 520));
        painelCentral.add(header);
        painelCentral.add(Box.createVerticalStrut(18));
        painelCentral.add(scroll);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH; gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(painelCentral, gbc);

        mainLayout.add(content, BorderLayout.CENTER);
        container.add(mainLayout, "HISTORICO_ADMIN");
        navegarPara("HISTORICO_ADMIN");
    }

    // --- RECONSTRUTOR DE FOTOS BASE64 PARA PREVIEW SWING ---

    private void atualizarPreviewFoto(JLabel lblFoto, List<String> fotos) {
        if (fotos == null || fotos.isEmpty()) {
            lblFoto.setIcon(null);
            lblFoto.setText("Nenhuma evidência fotográfica enviada.");
            return;
        }

        String payloadFoto = fotos.get(0); 
        
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                byte[] imageBytes;
                if (payloadFoto.startsWith("/") || payloadFoto.contains(":\\") || payloadFoto.contains(":/")) {
                    File arquivo = new File(payloadFoto);
                    if (!arquivo.exists()) return null;
                    imageBytes = Files.readAllBytes(arquivo.toPath());
                } else {
                    imageBytes = Base64.getDecoder().decode(payloadFoto);
                }
                
                ImageIcon iconeOriginal = new ImageIcon(imageBytes);
                Image imagem = iconeOriginal.getImage().getScaledInstance(420, 220, Image.SCALE_SMOOTH);
                return new ImageIcon(imagem);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon finalIcon = get();
                    if (finalIcon != null) {
                        lblFoto.setIcon(finalIcon);
                        lblFoto.setText("");
                    } else {
                        lblFoto.setIcon(null);
                        lblFoto.setText("Arquivo local de imagem indisponível.");
                    }
                } catch (Exception ex) {
                    lblFoto.setIcon(null);
                    lblFoto.setText("Erro ao renderizar dados Base64.");
                }
            }
        }.execute();
    }

    private void abrirDetalhesReporte(Reporte r, Runnable aoAtualizar) {
        JDialog modal = new JDialog(this, "Detalhes do Chamado", true);
        modal.setSize(540, 700);
        modal.setLocationRelativeTo(this);

        JPanel painelConteudo = new JPanel();
        painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));
        painelConteudo.setBackground(COLOR_BG);
        painelConteudo.setBorder(new EmptyBorder(20, 20, 20, 20));
        painelConteudo.setPreferredSize(new Dimension(520, 750));

        JLabel title = new JLabel("Chamado #" + r.getId());
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(COLOR_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelConteudo.add(title);
        painelConteudo.add(Box.createVerticalStrut(20));

        String info = "<html><b style='color:#10b981;'>Categoria:</b> " + r.getCategoria() +
                      "<br><br><b style='color:#10b981;'>Localização:</b> " + r.getLocalizacao() +
                      "<br><br><b style='color:#10b981;'>Descrição:</b><br>" + r.getDescricao() +
                      "<br><br><b style='color:#10b981;'>Autor (CPF):</b> " + r.getAutorCpf() +
                      "<br><br><b style='color:#10b981;'>Status:</b> " + r.getStatus() + "</html>";
        
        JLabel lblInfo = new JLabel(info);
        lblInfo.setForeground(COLOR_TEXT);
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelConteudo.add(lblInfo);
        painelConteudo.add(Box.createVerticalStrut(20));

        JLabel lblFotos = new JLabel("Foto principal do reporte:");
        lblFotos.setForeground(Color.LIGHT_GRAY);
        lblFotos.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelConteudo.add(lblFotos);
        painelConteudo.add(Box.createVerticalStrut(8));

        JLabel lblFotoPreview = new JLabel("Carregando foto remota...", SwingConstants.CENTER);
        lblFotoPreview.setPreferredSize(new Dimension(420, 220));
        lblFotoPreview.setMinimumSize(new Dimension(420, 220));
        lblFotoPreview.setMaximumSize(new Dimension(420, 220));
        lblFotoPreview.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        lblFotoPreview.setOpaque(true);
        lblFotoPreview.setBackground(new Color(15, 23, 42));
        lblFotoPreview.setForeground(Color.LIGHT_GRAY);
        lblFotoPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelConteudo.add(lblFotoPreview);
        painelConteudo.add(Box.createVerticalStrut(20));

        atualizarPreviewFoto(lblFotoPreview, r.getFotos());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        acoes.setOpaque(false);

        if (r.getStatus().equals("Pendente")) {
            RoundedButton btnAprovar = new RoundedButton("DEFERIR", COLOR_ACCENT, 10);
            btnAprovar.setPreferredSize(new Dimension(120, 40));
            btnAprovar.addActionListener(e -> {
                btnAprovar.setEnabled(false);
                atualizarStatusChamadoFirebase(r, "Aprovado", 50, () -> {
                    alert("Sucesso", "Chamado deferido e +50 ZeloCoins concedidas ao autor.", false);
                    modal.dispose();
                    aoAtualizar.run();
                });
            });

            RoundedButton btnRecusar = new RoundedButton("RECUSAR", COLOR_DANGER, 10);
            btnRecusar.setPreferredSize(new Dimension(120, 40));
            btnRecusar.addActionListener(e -> {
                btnRecusar.setEnabled(false);
                atualizarStatusChamadoFirebase(r, "Recusado", 0, () -> {
                    alert("Aviso", "Chamado arquivado como recusado.", true);
                    modal.dispose();
                    aoAtualizar.run();
                });
            });

            acoes.add(btnAprovar); acoes.add(btnRecusar);
        } else {
            RoundedButton btnFechar = new RoundedButton("FECHAR VIEW", COLOR_CARD, 10);
            btnFechar.setPreferredSize(new Dimension(150, 40));
            btnFechar.addActionListener(e -> modal.dispose());
            acoes.add(btnFechar);
        }

        painelConteudo.add(acoes);

        JScrollPane scrollJanela = new JScrollPane(painelConteudo);
        scrollJanela.setBorder(null);
        scrollJanela.getViewport().setBackground(COLOR_BG);
        scrollJanela.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        modal.setContentPane(scrollJanela);
        modal.pack();
        modal.setLocationRelativeTo(this);
        modal.setVisible(true);
    }

    // --- HELPERS E MAIN ---

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
        // Inicializa o SDK administrativo do Firebase (credenciais via arquivo JSON interno)
        if (!FirebaseInitializer.inicializar()) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível inicializar o Firebase. Verifique o arquivo firebase-config.json e as credenciais.",
                    "Erro Firebase",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new SRMI_Sistema().setVisible(true));
    }
}