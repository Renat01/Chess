import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class HomeScreen extends JFrame implements ActionListener{

    JPanel BottomPanel = new JPanel();

    public HomeScreen(){
        super("Chess.java");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.decode("#a1c47e"));
        setLayout(new FlowLayout());
        setResizable(false);

        Layout();
        
        addbuttons();
    }

    private void Layout(){
        JLabel TopPanel = new JLabel("Chess.java");
        TopPanel.setPreferredSize(new Dimension(800,150));
        TopPanel.setFont(new Font("Consolas", Font.BOLD, 32));
        TopPanel.setHorizontalAlignment(SwingConstants.CENTER);
        add(TopPanel);
        BottomPanel.setPreferredSize(new Dimension(800,450));
        BottomPanel.setBackground(Color.decode("#a1c47e"));
        add(BottomPanel);
    }

    private void addbuttons(){
        String[] array = new String[]{"Settings","Self Analysis" ,"Puzzles" , "Play Bots", "Play over the Board"};
        for (int i = 0; i < array.length; i++) {
            JButton b = new JButton(array[i]);
            b.setPreferredSize(new Dimension(400, 50));
            b.setFocusable(false);
            b.setBackground(Color.LIGHT_GRAY);
            b.addActionListener(this);
            b.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
            BottomPanel.add(b,SwingConstants.CENTER);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton sourcebutton = (JButton) e.getSource();
        String buttontext = sourcebutton.getText();
        dispose();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                switch (buttontext) {
                    case "Play over the Board":
                        new Game().setVisible(true);
                        break;
                    case "Settings": 
                        new Setting().setVisible(true);
                        break;
                    case"Self Analysis":
                        try {
                            new SelfAnalysis().setVisible(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case"Puzzles":
                        new Puzzle().setVisible(true);
                        break;
                    case"Play Bots": new BotMenu().setVisible(true);
                        break;
                    default: JOptionPane.showMessageDialog(null, "Work in Progress", "Not done yet...", JOptionPane.INFORMATION_MESSAGE);
                        break;
                }
            }
            
        });
    }
}
