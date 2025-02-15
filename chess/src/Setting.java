import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Setting extends JFrame{
    ConfigReader config = new ConfigReader("chess\\utilities\\config.properties");
    Setting(){
        super("Settings");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        new HomeScreen().setVisible(true);
                        config.saveProperty();
                    }
                    
                });
            }
        });
        setSize(700,550);  
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new FlowLayout());
        getContentPane().setBackground(Color.decode("#a1c47e"));

        Sounds();

        ColorThemes();

        StockfishDepth();
    }
    
    private void Sounds(){

        JRadioButton sound = new JRadioButton("Sounds",true);
        sound.setPreferredSize(new Dimension(250, 70));
        sound.setFont(new Font("Comic Sans MS",Font.BOLD, 18));
        sound.setFocusable(false);
        sound.setBackground(Color.decode("#a1c47e"));
        sound.setIcon(new ImageIcon(config.getProperty("Sound").equals("true") ? "chess\\img\\sound.png" : "chess\\img\\nosound.png"));
        sound.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(sound.isSelected()){
                    sound.setIcon(new ImageIcon("chess\\img\\nosound.png"));
                    config.setProperty("Sound", "false");
                } else {
                    sound.setIcon(new ImageIcon("chess\\img\\sound.png"));
                    config.setProperty("Sound", "true");
                }
                
            }
            
        });
        add(sound);
    }

    private void ColorThemes(){
        JPanel panel1 = new JPanel();
        panel1.setPreferredSize(new Dimension(550,70));
        panel1.setBackground(Color.decode("#a1c47e"));
        add(panel1);

        JLabel label = new JLabel("Choose theme: ");
        label.setPreferredSize(new Dimension(150,70));
        label.setFont(new Font("Comic Sans MS",Font.BOLD,15));
        panel1.add(label);

        String[] options = new String[]{"Green and White", "Gray and White", "Blue and White", "Brown and White"};
        JComboBox<String> menu = new JComboBox<>(options);
        byte index;
        switch (config.getProperty("Darksq")) {
            case "Blue":
                index = 2;
                break;
            case "Green":
                index = 0;
                break;
            case "Brown":
                index = 3;
                break;
            default: index = 1;
                break;
        }
        menu.setSelectedIndex(index);
        menu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) menu.getSelectedItem();
                String color;
                switch (selected) {
                    case "Green and White":
                        color="Green";
                    break;
                    case "Blue and White":
                        color = "Blue";
                    break;
                    case "Brown and White":
                        color="Brown";
                    break;    
                    default: color="Gray";
                        break;
                }
                config.setProperty("Darksq", color);

            }
            
        });
        panel1.add(menu);
    }

    private void StockfishDepth(){
        JPanel panel1 = new JPanel();
        panel1.setPreferredSize(new Dimension(550,70));
        panel1.setBackground(Color.decode("#a1c47e"));
        add(panel1);

        JLabel enter = new JLabel("Stockfish Depth ");
        enter.setFont(new Font("Comic Sans MS",Font.BOLD,15));
        enter.setPreferredSize(new Dimension(150,70));
        panel1.add(enter);

        JSpinner number = new JSpinner(new SpinnerNumberModel(Integer.parseInt(config.getProperty("Depth")),1,30,1));
        number.setPreferredSize(new Dimension(50,30));
        number.setFont(new Font("Comic Sans MS",Font.BOLD,15));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) number.getEditor();
        editor.getTextField().setEditable(false);
        number.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                config.setProperty("Depth", String.valueOf(number.getValue()));
            }
            
        });
        panel1.add(number);
    }
}
