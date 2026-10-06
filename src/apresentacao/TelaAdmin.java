//ESSE CODIGO É SOMENTE UM EXEMPLO
package apresentacao;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaAdmin extends JFrame {

    public TelaAdmin() {
        // Configurações básicas da janela
        setTitle("Sistema de Eleições - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Permite posicionar os itens manualmente (para protótipo é mais fácil)

        // Criando um texto (Label)
        JLabel labelUsuario = new JLabel("Usuário:");
        labelUsuario.setBounds(50, 50, 100, 30);
        add(labelUsuario);

        // Criando um campo de texto
        JTextField campoUsuario = new JTextField();
        campoUsuario.setBounds(150, 50, 150, 30);
        add(campoUsuario);

        // Criando um botão
        JButton botaoEntrar = new JButton("Entrar");
        botaoEntrar.setBounds(150, 150, 100, 30);
        add(botaoEntrar);

        // Ação do botão para testar a navegação (Navegação básica exigida na Etapa 1)
        botaoEntrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Botão clicado! Aqui abriria a próxima tela.");
                // Para abrir outra tela, você instanciaria a nova tela aqui, ex:
                // TelaPrincipal tela = new TelaPrincipal();
                // tela.setVisible(true);
                // dispose(); // Fecha a tela de login
            }
        });
    }

    public static void main(String[] args) {
        // Executa a tela
        TelaAdmin tela = new TelaAdmin();
        tela.setVisible(true);
    }
}
