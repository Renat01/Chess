import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
//Game class, here the game is initialized and the main game loop is run. As well as further rules are implemented
public class Puzzle extends JFrame implements ActionListener{
    JPanel gamepanel = new JPanel();
    Piece[][] board = new Piece[8][8];
    JButton[][] boardbuttons = new JButton[8][8];
    ImageIcon blackpawn = new ImageIcon("chess\\img\\black-pawn.png");
    ImageIcon whitepawn = new ImageIcon("chess\\img\\white-pawn.png");
    ImageIcon blackbishop = new ImageIcon("chess\\img\\black-bishop.png");
    ImageIcon whitebishop = new ImageIcon("chess\\img\\white-bishop.png");
    ImageIcon blackrook = new ImageIcon("chess\\img\\black-rook.png");
    ImageIcon whiterook = new ImageIcon("chess\\img\\white-rook.png");
    ImageIcon blackknight = new ImageIcon("chess\\img\\black-knight.png");
    ImageIcon whiteknight = new ImageIcon("chess\\img\\white-knight.png");
    ImageIcon blackqueen = new ImageIcon("chess\\img\\black-queen.png");
    ImageIcon whitequeen = new ImageIcon("chess\\img\\white-queen.png");
    ImageIcon blackking = new ImageIcon("chess\\img\\black-king.png");
    ImageIcon whiteking = new ImageIcon("chess\\img\\white-king.png");
    ImageIcon righticon = new ImageIcon("chess\\img\\right.png");
    ImageIcon wrongicon = new ImageIcon("chess\\img\\wrong.png");
    ArrayList<JButton> possiblemoves = new ArrayList<JButton>();
    boolean isSelected = false;
    Piece selected;
    Border blackborder = BorderFactory.createLineBorder(Color.BLACK , 3,true);
    boolean whitetomove=true;
    boolean whitekingMoved = false;
    boolean blackkingMoved = false;
    boolean whiterook1 = false;
    boolean whiterook2 = false;
    boolean blackrook1 = false;
    boolean blackrook2 = false;
    boolean[] castlingrights = new boolean[6];
    JLayeredPane layeredPane = new JLayeredPane();
    int movetracker = 0; 
    int fiftymoverule = 0;
    int fenmovecounter = 1;
    ArrayList<Position> savedpositions = new ArrayList<>();
    HashMap<Integer,Character> mapY = new HashMap<>();
    HashMap<Integer,Character> mapX = new HashMap<>();
    HashMap<Character,Integer> reversemapY = new HashMap<>();
    HashMap<Character,Integer> reversemapX = new HashMap<>();
    JPanel buttonspanel = new JPanel();
    JPanel sidepanel = new JPanel();
    DefaultTableModel tablemodel = new DefaultTableModel(0,2){
        // Make all cells non-editable
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Disable editing
        }
    };
    JTable movestable = new JTable(tablemodel);
    int currentdot=0;
    Piece[][] var = null;
    Stack<Position> redo = new Stack<>();
    Stack<Position> undo = new Stack<>();
    ConfigReader config = new ConfigReader("chess\\utilities\\config.properties");
    Color squareCol;
    boolean sounds = config.getProperty("Sound").equals("true") ? true : false;
    JLabel tomovelabel,movestatus,ID,Elo,themelabel;
    JButton nextpuzzle;
    boolean playercolor;
    int puzzlemovecounter;
    String[] puzzlemoves;
    //Constructor to create a new Game object and make the frame 
    Puzzle(){
        //Setting up the frame with the title and size and making it unresizable
        super("Puzzles");
        squareCol = getColorFromString(config.getProperty("Darksq"));
        setSize(920,815);
        setLayout(new FlowLayout());
        getContentPane().setBackground(Color.lightGray);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        new HomeScreen().setVisible(true);
                    }
                    
                });
            }
        });  
        //Initializing the players, board and sidepanel
        TopBar();
        addboard();
        addsidepanel();
        add(gamepanel);
        //Initializing the starting position of the pieces
        ArrayList<JButton> start = new ArrayList<JButton>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(i==7 || i==6)start.add(boardbuttons[i][j]);
            }
        }
        //Enabling the buttons and playing the start sound
        enablebuttons(start);
        if(sounds)play("chess\\soundfx\\game-start.wav");
        //Initializing the player and adding the pieces, as well as drawing and resigning options
        BottomBar();
        addpieces();
        positionmaps();

        nextpuzzle.doClick();
    }

    public Color getColorFromString(String str){
        switch (str) {
            case "Gray":
                return Color.gray;
            case "Blue":
                return Color.decode("#3B99D9");
            case "Brown":
                return Color.decode("#D18B46");
            case "Green":
                return Color.decode("#769656");        
            default:
                return Color.gray;
        }
    }
    //Method to play a sound file
    public void play(String filePath){
        try {
            File file = new File(filePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.close();  
            }
        });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Method to create the player bar
    private JPanel Playerbar(){
        //Creating the panel for the players
        JPanel panel = new JPanel();
        //Making the front end of the panel
        panel.setLayout(new FlowLayout());
        panel.setPreferredSize(new Dimension(950,50));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK , 3,true));
        panel.setBackground(Color.decode("#deb887"));
        
        return panel;
    }

    private void TopBar(){
        JPanel bar = Playerbar();

        ID = new JLabel("Puzzle ID: 00008");
        ID.setPreferredSize(new Dimension(200,40));
        ID.setFont(new Font("Times New Roman",Font.BOLD,20));

        Elo = new JLabel("Elo: 1489");
        Elo.setPreferredSize(new Dimension(200,40));
        Elo.setFont(new Font("Times New Roman",Font.BOLD,20));

        bar.add(ID);
        bar.add(Elo);

        add(bar);
    }
    
    private void BottomBar(){
        JPanel bar = Playerbar();
        
        themelabel = new JLabel("Themes: ");
        themelabel.setPreferredSize(new Dimension(800,40));
        themelabel.setFont(new Font("Times New Roman",Font.BOLD,20));

        bar.add(themelabel);

        add(bar);
    }
    //Method to add the board to the game
    private void addboard(){
        //Making the board panel, setting the size of the board and adding it to the game panel
        JPanel boardpanel = new JPanel();
        boardpanel.setLayout(new GridLayout(8,8));
        boardpanel.setBounds(0,0,650,650);

        layeredPane.setPreferredSize(new Dimension(655, 650));
        layeredPane.add(boardpanel, JLayeredPane.DEFAULT_LAYER);

        gamepanel.add(layeredPane);
        
        //Making the buttons for the board and adding them to the board panel
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JButton b = new JButton();
                boardpanel.add(b);
                b.setBorder(null);
                boardbuttons[i][j] = b;
                boardbuttons[i][j].setFocusable(false);
                b.addActionListener(this);
                //the buttons are added with the respective colors
                if((i+j)%2==0){ 
                    b.setBackground(Color.white);
                } else {
                    b.setBackground(squareCol);
                }
            }
        }
    }
    //Method for creating the side panel for displaying the moves and buttons for resignation and drawing
    private void addsidepanel(){
        //Setting up the front end for the side panel, moves panel and buttonspanel 
        sidepanel.setPreferredSize(new Dimension(180, 650));
        gamepanel.add(sidepanel);

        movestable.setTableHeader(null);
        movestable.setShowGrid(false);
        movestable.setBackground(Color.decode("#EEEEEE"));
 
        JLabel move = new JLabel("Moves");
        move.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        move.setPreferredSize(new Dimension(160, 30));
        move.setHorizontalAlignment(SwingConstants.CENTER);
        sidepanel.add(move);
        
        //Making the scrollpane for the moves panel in case the text of the moves exceeds the size of the panel
        JScrollPane scroll = new JScrollPane(movestable);
        scroll.setPreferredSize(new Dimension(160, 250));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        sidepanel.add(scroll);
        RedoUndo();
        otherstuffsidepanel();
    }

    private void RedoUndo(){

        JButton redobtn = new JButton("--->");
        redobtn.setPreferredSize(new Dimension(80,50));
        redobtn.setFont(new Font("Arial", Font.BOLD, 15));
        redobtn.setBackground(Color.lightGray);
        redobtn.setFocusable(false);
        redobtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(!redo.isEmpty()){
                    currentdot++;
                    undo.add(redo.pop());

                    if(redo.isEmpty()){
                        ArrayList<JButton> temp = new ArrayList<>();
                        findenabledbuttons(temp);
                        enablebuttons(temp);
                        updatedisplay(board);
                    } else {
                        disableallbuttons();
                        updatedisplay(savedpositions.get(savedpositions.size()-redo.size()-1).board);
                    }
                    updatecurrentdot();
                }

            }
            
        });
        
        JButton undobtn = new JButton("<---");
        undobtn.setPreferredSize(new Dimension(80,50));
        undobtn.setFont(new Font("Arial", Font.BOLD, 15));
        undobtn.setBackground(Color.lightGray);
        undobtn.setFocusable(false);
        undobtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(!undo.isEmpty()){
                    currentdot--;
                    redo.add(undo.pop());
                    disableallbuttons();
                    updatedisplay(redo.peek().board);
                    updatecurrentdot();
                }
                
            }
            
        });

        sidepanel.add(undobtn);
        sidepanel.add(redobtn);
    }

    private void otherstuffsidepanel(){
        tomovelabel = new JLabel("<html><i>White to move</i></html>");
        tomovelabel.setFont(new Font("Comic Sans MS",Font.BOLD,17));
        tomovelabel.setPreferredSize(new Dimension(120,60));
        sidepanel.add(tomovelabel);

        JButton reveal = new JButton("<html><center>Reveal <br>Next Move</center></html>");
        reveal.setPreferredSize(new Dimension(96,50));
        reveal.setFocusable(false);
        reveal.setBackground(Color.lightGray);
        reveal.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                reveal.setText(puzzlemoves[puzzlemovecounter]);
                java.util.Timer timer = new java.util.Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        reveal.setText("<html><center>Reveal <br>Next Move</center></html>");
                    }
                    
                }, 1000);
            }
            
        });
        sidepanel.add(reveal);

        nextpuzzle = new JButton("<html><center>Next <br> Puzzle</center></html>");
        nextpuzzle.setPreferredSize(new Dimension(74,50));
        nextpuzzle.setFocusable(false);
        nextpuzzle.setBackground(Color.lightGray);
        nextpuzzle.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                isSelected=false;
                possiblemoves.clear();
                savedpositions.clear();

                undo.clear();
                redo.clear();
                currentdot=0;
                tablemodel.setRowCount(0);
                movetracker=0;
                puzzlemovecounter=0;

                String[] PuzzleInfo = newPuzzleInfo();
                String id = PuzzleInfo[0];
                String fen = PuzzleInfo[1];
                String moves = PuzzleInfo[2];
                String elo = PuzzleInfo[3];
                String themes = PuzzleInfo[4];

                ID.setText("Puzzle ID: " + id);
                replacefen(fen);
                puzzlemoves=moves.split(" ");
                Elo.setText("Elo: " + elo);
                themelabel.setText("Themes: " + themes);

                updatedisplay(board);
                ArrayList<JButton> enabledbuttons = new ArrayList<JButton>();
                findenabledbuttons(enabledbuttons);
                enablebuttons(enabledbuttons);

                //Initializing the position and adding it to the list of saved positions
                Position p = new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null);
                savedpositions.add(p);

                simulatemove(moves.split(" ")[0]);
            }
            
        });
        sidepanel.add(nextpuzzle);

        JPanel space = new JPanel();
        space.setPreferredSize(new Dimension(180,45));
        sidepanel.add(space);

        movestatus = new JLabel();
        sidepanel.add(movestatus);
    }

    private String[] newPuzzleInfo(){
        String filePath = "chess\\utilities\\puzzles.txt";

        Random random = new Random();
        int targetLine = (random.nextInt(20000))+1;

        String[] puzzleinfo = new String[5];
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int currentLine = 0;
            while ((line = reader.readLine()) != null) {
                currentLine++;
                if (currentLine == targetLine) {
                    String[] temp =line.split(",");
                    puzzleinfo[0] = temp[0];
                    puzzleinfo[1] = temp[1];
                    puzzleinfo[2] = temp[2];
                    puzzleinfo[3] = temp[3];
                    puzzleinfo[4] = temp[7];
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return puzzleinfo;
    }

    private void replacefen(String fen){
        String[] parts = fen.split(" ");

        replaceboard(parts[0]);

        whitetomove = parts[1].equals("w") ? true : false;
        tomovelabel.setText(!whitetomove ? "White to move" : "Black to move");
        if(!whitetomove){
            movetracker++;
            currentdot++;
            tablemodel.addRow(new Object[]{"-" ,""});
        }
        playercolor=!whitetomove;

        resetcastlingrights();
        if(!parts[2].equals("-")){
            char[] chars = parts[2].toCharArray();
            for (char c : chars) {
                switch (c) {
                    case 'Q':
                        whitekingMoved=false;
                        whiterook1=false;
                        break;
                    case 'K': 
                        whitekingMoved=false;
                        whiterook2=false;
                        break;
                    case 'k':
                        blackkingMoved=false;
                        blackrook1=false;
                        break;
                    case 'q':
                        blackkingMoved=false;
                        blackrook2=false;
                        break;
                }
            }
        }
        
        //passant no need for that

        fiftymoverule = Integer.parseInt(parts[4]);

        fenmovecounter = Integer.parseInt(parts[5]);

    }

    private void replaceboard(String fenboard){
        String[] rows = fenboard.split("/");

        for (int row = 0; row < 8; row++) {
            int col = 0;
            for (char c : rows[row].toCharArray()) {
                if (Character.isDigit(c)) {
                    // If it's a digit, fill that many empty squares
                    int emptySquares = c - '0';
                    for (int i = 0; i < emptySquares; i++) {
                        board[row][col] = new Piece(0, true);
                        col++;
                    }
                } else {
                    // Place the piece at the current board position
                    board[row][col] = getpiecefromchar(c,row,col);
                    col++;
                }
                
            }
        }
    }

    private Piece getpiecefromchar(char c, int x, int y){
        boolean isWhite = Character.isUpperCase(c);

        switch (Character.toLowerCase(c)) {
        case 'r': return new Rook(isWhite, x, y);
        case 'n': return new Knight(isWhite, x, y);
        case 'b': return new Bishop(isWhite, x, y);
        case 'q': return new Queen(isWhite, x, y);
        case 'k': return new King(isWhite, x, y);
        case 'p': return new Pawn(isWhite, x, y, 0, false, false, false, false);
        default: return new Piece(0, true); // Default empty square placeholder
    }
    }

    private void resetcastlingrights(){
        whitekingMoved=true;
        whiterook1=true;
        whiterook2=true;
        blackkingMoved=true;
        blackrook1=true;
        blackrook2=true;
    }

    private void simulatemove(String movestr){

        char[] str = movestr.toCharArray();
        boardbuttons[reversemapX.get(str[1])][reversemapY.get(str[0])].doClick();
        if(board[reversemapX.get(str[1])][reversemapY.get(str[0])].isKing){
            if(whitetomove && movestr.equals("e1g1"))boardbuttons[7][7].doClick();
            else if(!whitetomove && movestr.equals("e8g8"))boardbuttons[0][7].doClick();
            else if(whitetomove && movestr.equals("e1c1"))boardbuttons[7][0].doClick();
            else if(!whitetomove && movestr.equals("e8c8"))boardbuttons[0][0].doClick();
            else boardbuttons[reversemapX.get(str[3])][reversemapY.get(str[2])].doClick();
        } else boardbuttons[reversemapX.get(str[3])][reversemapY.get(str[2])].doClick();
        
    }

    private void disableallbuttons(){
        for(JButton[] i : boardbuttons){
            for (JButton b : i) {
                b.setEnabled(false);
            }
        }
    }
    //Method for adding the pieces to the board
    private void addpieces(){
        //Adding the pieces to the board based on rows and columns and the color of the piece on the board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(i==1){
                    board[i][j] = new Pawn(false,i,j, 0, false, false, false ,false);
                } else if(i==6){
                    board[i][j] = new Pawn(true,i,j, 0, false ,false, false ,false);
                } else if(i==0 && (j==2 || j==5)){
                    board[i][j] = new Bishop(false,i,j);
                } else if(i==7 && (j==2 || j==5)){
                    board[i][j] = new Bishop(true,i,j);
                } else if (i==0 && (j==0 || j==7)){
                    board[i][j] = new Rook(false,i,j);
                }else if (i==7 && (j==0 || j==7)){
                    board[i][j] = new Rook(true,i,j);
                } else if(i==0 && (j==1 || j==6)){
                    board[i][j] = new Knight(false,i,j);
                } else if(i==7 && (j==1 || j==6)){ 
                    board[i][j] = new Knight(true,i,j);
                }else if (i==0 && j==3) {
                    board[i][j] = new Queen(false,i,j);
                }else if (i==7 && j==3) {
                    board[i][j] = new Queen(true,i,j);
                } else if(i==7 && j==4){
                    board[i][j] = new King(true,i,j);
                } else if(i==0 && j==4){
                    board[i][j] = new King(false,i,j);
                }
                //otherwise the piece is a empty square
                else{
                    board[i][j] = new Piece(0,false);
                }
            }
        }
        updatedisplay(board);
        findAllPossibleMoves();
    }
    //Method for updating the display of the board for each piece after moving a piece
    private void updatedisplay(Piece[][] board){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boardbuttons[i][j].setIcon(null);
                boardbuttons[i][j].setBorder(null);
                if(board[i][j].ispawn && board[i][j].white){
                    boardbuttons[i][j].setIcon(whitepawn);
                    boardbuttons[i][j].setDisabledIcon(whitepawn);
                } else if(board[i][j].ispawn && !board[i][j].white){
                    boardbuttons[i][j].setIcon(blackpawn);
                    boardbuttons[i][j].setDisabledIcon(blackpawn);
                } else if(board[i][j].isBishop && board[i][j].white){
                    boardbuttons[i][j].setIcon(whitebishop);
                    boardbuttons[i][j].setDisabledIcon(whitebishop);
                } else if(board[i][j].isBishop && !board[i][j].white){
                    boardbuttons[i][j].setIcon(blackbishop);
                    boardbuttons[i][j].setDisabledIcon(blackbishop);
                } else if (board[i][j].isRook && board[i][j].white){
                    boardbuttons[i][j].setIcon(whiterook);
                    boardbuttons[i][j].setDisabledIcon(whiterook);
                }else if (board[i][j].isRook && !board[i][j].white){
                    boardbuttons[i][j].setIcon(blackrook);
                    boardbuttons[i][j].setDisabledIcon(blackrook);
                } else if(board[i][j].isKnight && !board[i][j].white){
                    boardbuttons[i][j].setIcon(blackknight);
                    boardbuttons[i][j].setDisabledIcon(blackknight);
                } else if(board[i][j].isKnight && board[i][j].white){
                    boardbuttons[i][j].setIcon(whiteknight);
                    boardbuttons[i][j].setDisabledIcon(whiteknight);
                } else if (board[i][j].isQueen && board[i][j].white) {
                    boardbuttons[i][j].setIcon(whitequeen);
                    boardbuttons[i][j].setDisabledIcon(whitequeen);
                } else if (board[i][j].isQueen && !board[i][j].white) {
                    boardbuttons[i][j].setIcon(blackqueen);
                    boardbuttons[i][j].setDisabledIcon(blackqueen);
                } else if (board[i][j].isKing && !board[i][j].white) {
                    boardbuttons[i][j].setIcon(blackking);
                    boardbuttons[i][j].setDisabledIcon(blackking);
                } else if (board[i][j].isKing && board[i][j].white) {
                    boardbuttons[i][j].setIcon(whiteking);
                    boardbuttons[i][j].setDisabledIcon(whiteking);
                }
            }
        }
    }
    //Method for getting the position of a button in the board
    private int[] getPosition(JButton b){
        int[] pos = new int[2];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (boardbuttons[i][j] == b) {
                    pos[0] = i;
                    pos[1] = j;
                }
            }
        }
        return pos;
    }
    //Method for getting the position of a piece in the board
    private int[] getPosition(Piece piece){
        int[] pos = new int[2];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == piece) {
                    pos[0] = i;
                    pos[1] = j;
                }
            }
        }
        return pos;
    }
    //Method for making the actions when a button is clicked 
    @Override
    public void actionPerformed(ActionEvent e) {
        JButton sourcebutton = (JButton) e.getSource();
        int[] pos = getPosition(sourcebutton);
        int x = pos[0];
        int y = pos[1];
        Piece sourcepiece = (Piece) board[x][y];
        //Allowing the user to deselect a piece by clicking it again
        if(isSelected && sourcepiece==selected){
            isSelected = false;
            sourcebutton.setBorder(null);
            for (JButton b : possiblemoves) {
                b.setBorder(null);
            }
            possiblemoves.clear();
            ArrayList<JButton> enabledbuttons = new ArrayList<JButton>();
            findenabledbuttons(enabledbuttons);
            enablebuttons(enabledbuttons);
            return;
        }
        //Allows the user to select a piece by clicking it
        if(!isSelected){
            checkMove(sourcepiece, sourcebutton , x, y);
            possiblemoves.add(sourcebutton);
            enablebuttons(possiblemoves);
        }
        //Allows the user to make a move by clicking the preferred button on the board
        else if(isSelected){
            makeMove(sourcebutton,x,y);
        }
    }
    //Method for checking and showing all the legal moves the selected piece can make
    private void checkMove(Piece sourcepiece ,JButton sourceButton, int x, int y){
        if(!sourcepiece.isempty){
            isSelected = true;
            //letting the user know which piece is selected
            sourceButton.setBorder(blackborder);
            //checking if the selected piece is a pawn
            if(sourcepiece.ispawn){
                //relevant to know the past moves of the pawn in order to judge for en Passant
                if (savedpositions.size()>1) {
                    var = savedpositions.get(savedpositions.size()-2).board;
                }
                //Creating a new Pawn object for the selected piece
                Pawn selectedpiece = (Pawn) sourcepiece;
                selected = sourcepiece;
                //checking if the selected piece can move based on the list retrieved from the Pawn class
                if(selectedpiece.checkallmoves(board,var).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board,var)) {
                        //checking if the selected piece can move if the king is in check in order to block the check 
                        if(moveincheck(selectedpiece, pos)){
                            possiblemoves.add(boardbuttons[pos[0]][pos[1]]);
                            boardbuttons[pos[0]][pos[1]].setBorder(blackborder);
                        }
                        //checking if the selected piece can perform en Passant right or left
                        if(selectedpiece.enPassant(board,var)!=null){
                            if(selectedpiece.enPassant(board,var).get(0)!=null && !selectedpiece.missedleft){
                                selectedpiece.enPassantleft = true;
                            }if(selectedpiece.enPassant(board,var).get(1)!=null && !selectedpiece.missedright){
                                selectedpiece.enPassantright = true;
                            }
                        }
                    }
                }
                //Creating a new Bishop object for the selected piece if the selected piece is a Bishop
            } else if(sourcepiece.isBishop){
                Bishop selectedpiece = (Bishop) sourcepiece;
                selected = sourcepiece;
                //checking if the selected piece can move based on the list retrieved from the Bishop class
                if(selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                        if(moveincheck(selectedpiece, pos)){
                            possiblemoves.add(boardbuttons[pos[0]][pos[1]]);
                            boardbuttons[pos[0]][pos[1]].setBorder(blackborder);
                        }
                    }
                }
                //Creating a new Rook object for the selected piece if the selected piece is a Rook
            } else if (sourcepiece.isRook) {
                Rook selectedpiece = (Rook) sourcepiece;
                selected = sourcepiece;
                //checking if the selected piece can move based on the list retrieved from the Rook class
                if(selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                        if(moveincheck(selectedpiece, pos)){
                            possiblemoves.add(boardbuttons[pos[0]][pos[1]]);
                            boardbuttons[pos[0]][pos[1]].setBorder(blackborder);
                        }
                    }
                }
                //Creating a new Knight object for the selected piece if the selected piece is a Knight
            } else if (sourcepiece.isKnight) {
                Knight selectedpiece = (Knight) sourcepiece;
                selected = sourcepiece;
                //checking if the selected piece can move based on the list retrieved from the Knight class
                if (selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                        if(moveincheck(selectedpiece, pos)){
                            possiblemoves.add(boardbuttons[pos[0]][pos[1]]);
                            boardbuttons[pos[0]][pos[1]].setBorder(blackborder);
                        }
                    }
                }
                //Creating a new Queen object for the selected piece if the selected piece is a Queen
            }else if (sourcepiece.isQueen) {
                Queen selectedpiece = (Queen) sourcepiece;
                selected = sourcepiece;
                //Checking if the selected piece can move based on the list retrieved from the Queen class
                if(selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                        if(moveincheck(selectedpiece, pos)){
                            possiblemoves.add(boardbuttons[pos[0]][pos[1]]);
                            boardbuttons[pos[0]][pos[1]].setBorder(blackborder);
                        }
                    }
                }
                //Creating a new King object for the selected piece if the selected piece is a King
            } else if (sourcepiece.isKing) {
                King selectedpiece = (King) sourcepiece;
                selected = sourcepiece;
                //Checking if the selected piece can move based on the list retrieved from the King class
                if (selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                        if(moveincheck(selectedpiece, pos)){
                            possiblemoves.add(boardbuttons[pos[0]][pos[1]]);
                            boardbuttons[pos[0]][pos[1]].setBorder(blackborder);
                        }
                    }
                }
                //Checking if the King can perform long castling for white and black
                if(checkCastleLong()){
                    if(whitetomove){
                        possiblemoves.add(boardbuttons[7][0]);
                        boardbuttons[7][0].setBorder(blackborder);
                    } else if(!whitetomove){
                        possiblemoves.add(boardbuttons[0][0]);
                        boardbuttons[0][0].setBorder(blackborder);
                    }
                }
                //Checking if the King can perform short castling for white and black
                if(checkCastleShort()){
                    if(whitetomove){
                        possiblemoves.add(boardbuttons[7][7]);
                        boardbuttons[7][7].setBorder(blackborder);
                    } else if(!whitetomove){
                        possiblemoves.add(boardbuttons[0][7]);
                        boardbuttons[0][7].setBorder(blackborder);
                    }
                }
                
            }
        }
    }
    //Method for making the move of the selected piece
    private void makeMove(JButton sourcebutton, int x, int y){
        //if the selected button is a highlighted button where the user can move
        if(possiblemoves.contains(sourcebutton)){
            //if the selected piece is a pawn
            if(selected.ispawn){
                //relevant to know the board of the past moves of the pawn in order to judge for en Passant
                if (savedpositions.size()>1) {
                    var = savedpositions.get(savedpositions.size()-2).board;
                }
                //Creating a new Pawn object for the selected piece
                Pawn selectedpiece = (Pawn) selected;
                //checking if there are any moves for the pawn to make
                if(selectedpiece.checkallmoves(board,var).size()>0){
                    //taking each move from the list
                    for (int[] pos : selectedpiece.checkallmoves(board,var)) {
                        if(boardbuttons[pos[0]][pos[1]]==sourcebutton){
                            //if the pawn is at the end of the board the pawn will promote
                            if ((selectedpiece.white && pos[0]==0)||(!selectedpiece.white && pos[0]==7)){
                                promotion(selectedpiece, pos[0], pos[1]);
                                return;
                            }
    
                            if(sounds)play("chess\\soundfx\\move-self.wav");
                            currentdot++;
                            undo.add(new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null));
    
                            //checking if the pawn can perform en Passant right or left
                            if(selectedpiece.enPassant(board,var)!=null && (selectedpiece.enPassantleft || selectedpiece.enPassantright)){
                                //taking the new position where the pawn can perform en Passant and storing it in the newpos array
                                int[] newpos = null;  
                                for (int[] i : selectedpiece.enPassant(board,var)) {
                                    if(i!=null){
                                        newpos = i;
                                    }
                                }
                                //Making the move in the board for white and black by removing the piece taken
                                if(selectedpiece.white && newpos[0]==pos[0] && newpos[1]==pos[1]){
                                    board[pos[0]+1][pos[1]]=new Piece(0,false);
                                } else if(!selectedpiece.white && newpos[0]==pos[0] && newpos[1]==pos[1]){
                                    board[pos[0]-1][pos[1]]=new Piece(0,true);
                                }
                            }
                            //move the pawn and display the move
                            if (board[pos[0]][pos[1]].isempty) {
                                if(!displayMoves(pos[0],pos[1],false,false,-1,""))return;
                            }else{
                                if(!displayMoves(pos[0],pos[1],true,false,-1,""))return; 
                            }
                            //making the move
                            selectedpiece.movepawn(pos[0],pos[1],board);
                            fiftymoverule = -1;
                                
                        }
                    }
                }
                //Creating a new Bishop object for the selected piece if the selected piece is a Bishop
            } else if (selected.isBishop) {
                if(sounds)play("chess\\soundfx\\move-self.wav");
                currentdot++;
                undo.add(new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null));

                Bishop selectedpiece = (Bishop) selected;
                    //checking if the selected piece can move based on the list retrieved from the checkallmoves method of the Bishop class
                    if (selectedpiece.checkallmoves(board).size()>0){
                        //for each of the positions retrieved from the list find where to move and display the move
                        for (int[] pos : selectedpiece.checkallmoves(board)) {
                            if (boardbuttons[pos[0]][pos[1]]==sourcebutton) {
                                //displaying the move
                                if (board[pos[0]][pos[1]].isempty) {
                                    if(!displayMoves(pos[0],pos[1],false,false,-1,""))return;
                                }else{
                                    if(!displayMoves(pos[0],pos[1],true,false,-1,""))return;
                                }
                                //updating the 50 move rule
                                if (!board[pos[0]][pos[1]].isempty && board[pos[0]][pos[1]].white!=selectedpiece.white) {
                                    fiftymoverule = -1;
                                }
                                //making the move
                                selectedpiece.movebishop(pos[0],pos[1],board);
                            }
                        }
                    }
            //Creating a new Rook object for the selected piece if the selected piece is a Rook
            } else if (selected.isRook) {
                if(sounds)play("chess\\soundfx\\move-self.wav");
                currentdot++;
                undo.add(new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null));

                Rook selectedpiece = (Rook) selected;
                if(selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                        if (boardbuttons[pos[0]][pos[1]]==sourcebutton) {
                            int[] rookpos = getPosition(selectedpiece);
                            //depicting which rook is where for castling and it is necessary to know whether the rook has moved or not
                            if(rookpos[0]==7 && rookpos[1]==0){
                                whiterook1 = true;
                                //queen side
                            } else if(rookpos[0]==7 && rookpos[1]==7){
                                whiterook2 = true;
                                //king side
                            } else if(rookpos[0]==0 && rookpos[1]==7){
                                blackrook2 = true;
                                //king side
                            } else if(rookpos[0]==0 && rookpos[1]==0){
                                blackrook1 = true;
                                //queen side
                            }
                            //displaying the move
                            if (board[pos[0]][pos[1]].isempty) {
                                if(!displayMoves(pos[0],pos[1],false,false,-1,""))return;
                            }else{
                                if(!displayMoves(pos[0],pos[1],true,false,-1,""))return;
                            }
                            if (!board[pos[0]][pos[1]].isempty && board[pos[0]][pos[1]].white!=selectedpiece.white) {
                                fiftymoverule = -1;
                            }
                            //making the move
                            selectedpiece.moveRook(pos[0],pos[1],board);
                        }
                    }
                }
                //Creating a new Knight object for the selected piece if the selected piece is a Knight
            }else if (selected.isKnight) {
                if(sounds)play("chess\\soundfx\\move-self.wav");
                currentdot++;
                undo.add(new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null));

                Knight selectedpiece = (Knight) selected;
                //checking if the selected piece can move based on the list retrieved from the checkallmoves method of the Knight class
                if (selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                            if(boardbuttons[pos[0]][pos[1]]==sourcebutton){
                                //displaying the move
                                if (board[pos[0]][pos[1]].isempty) {
                                    if(!displayMoves(pos[0],pos[1],false,false,-1,""))return;
                                }else{
                                    if(!displayMoves(pos[0],pos[1],true,false,-1,""))return;
                                }
                                //updating the 50 move rule
                                if (!board[pos[0]][pos[1]].isempty && board[pos[0]][pos[1]].white!=selectedpiece.white) {
                                    fiftymoverule = -1;
                                }
                                //making the move
                                selectedpiece.moveknight(pos[0],pos[1],board);
                        }
                    }
                }
                //Creating a new Queen object for the selected piece if the selected piece is a Queen
            }else if (selected.isQueen) {
                if(sounds)play("chess\\soundfx\\move-self.wav");
                currentdot++;
                undo.add(new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null));

                Queen selectedpiece = (Queen) selected;
                //checking if the selected piece can move based on the list retrieved from the checkallmoves method of the Queen class
                if(selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                            if (boardbuttons[pos[0]][pos[1]]==sourcebutton) {
                                //displaying the move
                                if (board[pos[0]][pos[1]].isempty) {
                                    if(!displayMoves(pos[0],pos[1],false,false,-1,""))return;
                                }else{
                                    if(!displayMoves(pos[0],pos[1],true,false,-1,""))return;
                                }
                                //updating the 50 move rule
                                if (!board[pos[0]][pos[1]].isempty && board[pos[0]][pos[1]].white!=selectedpiece.white) {
                                    fiftymoverule = -1;
                                }
                                //making the move
                                selectedpiece.moveQueen(pos[0],pos[1],board);
                            }
                        }
                }
                //Creating a new King object for the selected piece if the selected piece is a King
            } else if (selected.isKing) {
                if(sounds)play("chess\\soundfx\\move-self.wav");
                currentdot++;
                undo.add(new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null));
                
                King selectedpiece = (King) selected;
                //checking if the selected piece can move based on the list retrieved from the checkallmoves method of the King class
                if (selectedpiece.checkallmoves(board).size()>0){
                    for (int[] pos : selectedpiece.checkallmoves(board)) {
                        if(boardbuttons[pos[0]][pos[1]]==sourcebutton){
                            //displaying the move
                            if (board[pos[0]][pos[1]].isempty) {
                                if(!displayMoves(pos[0],pos[1],false,false,-1,""))return; 
                            }else{
                                if(!displayMoves(pos[0],pos[1],true,false,-1,""))return;
                            }
                            //updating the 50 move rule
                            if (!board[pos[0]][pos[1]].isempty && board[pos[0]][pos[1]].white!=selectedpiece.white) {
                                fiftymoverule = -1;
                            }
                            //making the move
                            selectedpiece.moveKing(pos[0],pos[1],board);
                            if(selectedpiece.white){
                                whitekingMoved = true;
                            } else {
                                blackkingMoved = true;
                            }
                        }
                    }
                }
                //checking for castling short or long and displaying the move
                if(board[x][y].isRook){
                    if ((x==7 && y==7) || (x==0 && y==7) ){ 
                        //calling the method for castling short
                        if(!displayMoves(-1,-1,false,false,0,""))return;
                        castleshort();
                    } else if((x==0 && y==0) || (x==7 && y==0)){
                        //calling the method for castling long
                        if(!displayMoves(-1,-1,false,false,1,""))return;
                        castlelong();
                    }
                }
            }
            isSelected = false;
            //resetting the en Passant possibilities
            resetpassant();
            //clearing the possible moves
            possiblemoves.clear();
            //updating the display
            updatedisplay(board);
            //switching the turn of the players
            switchturn();

            updatecastlingrights();

            Position p = new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null);
            savedpositions.add(p);

            //if the king is in check setting a red border to let the user know that the king is in check
            if(KingInCheck(board)){
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if(whitetomove && board[i][j].isKing && board[i][j].white){
                            boardbuttons[i][j].setBorder(BorderFactory.createLineBorder(Color.RED , 3,true));
                        } else if(!whitetomove && board[i][j].isKing && !board[i][j].white){
                            boardbuttons[i][j].setBorder(BorderFactory.createLineBorder(Color.RED , 3,true));
                        }
                    }
                }
            }

            //otherwise incrementing the 50 move rule and checking if the game can be called off by the 50 move rule
            fiftymoverule++;
            /*if(fiftymoverule==100){
                endgame(false,true);
            }*/

            ArrayList<JButton> enabledbuttons = new ArrayList<JButton>();
            findenabledbuttons(enabledbuttons);
            enablebuttons(enabledbuttons);
            if(whitetomove!=playercolor){
                if(puzzlemoves.length>puzzlemovecounter)simulatemove(puzzlemoves[puzzlemovecounter]);
                else nextpuzzle.doClick();
            }
        }
    }

    private void updatecastlingrights(){
        castlingrights[0]=whitekingMoved;
        castlingrights[1]=whiterook1;
        castlingrights[2]=whiterook2;
        castlingrights[3]=blackkingMoved;
        castlingrights[4]=blackrook1;
        castlingrights[5]=blackrook2;
    }

    private void positionmaps(){
        //Creating a hashmap for the y axis of the board to name the squares
        mapY.put(0, 'a');
        mapY.put(1, 'b');
        mapY.put(2, 'c');
        mapY.put(3, 'd');
        mapY.put(4, 'e');
        mapY.put(5, 'f');
        mapY.put(6, 'g');
        mapY.put(7, 'h');

        //Creating a hashmap for the x axis of the board to name the squares
        mapX.put(0, '8');
        mapX.put(1, '7');
        mapX.put(2, '6');
        mapX.put(3, '5');
        mapX.put(4, '4');
        mapX.put(5, '3');
        mapX.put(6, '2');
        mapX.put(7, '1');

        for(int entry : mapY.keySet())reversemapY.put(mapY.get(entry), entry);
        for(int entry : mapX.keySet())reversemapX.put(mapX.get(entry), entry);
        
    }
    //Method for displaying the moves
    private boolean displayMoves(int x , int y, boolean capture, boolean promotion, int castle, String promotiontype){
        //Creating a stringbuilder for concatenating the moves and forming a notation
        StringBuilder movestr = new StringBuilder();
        
        //Appending the notation of the piece to the stringbuilder
        if(!selected.isempty && castle!=1 && castle!=0){

            movestr.append(mapY.get(selected.y));
            movestr.append(mapX.get(selected.x));
            //if the move is a capture append x to the stringbuilder and play a sound
            if(capture){
                if(sounds)play("chess\\soundfx\\capture.wav");
                //movestr.append("x");
            }
            movestr.append(mapY.get(y));
            movestr.append(mapX.get(x));
            //create a temporary board
            Piece[][] tempboard = new Piece[8][8];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    tempboard[i][j] = board[i][j];
                }
            }
            if(selected.ispawn){
                Pawn selectedpiece = (Pawn) selected;
                selectedpiece.movepawn(x,y,tempboard);
            } else if(selected.isBishop){
                Bishop selectedpiece = (Bishop) selected;
                selectedpiece.movebishop(x,y,tempboard);
            } else if(selected.isRook){
                Rook selectedpiece = (Rook) selected;
                selectedpiece.moveRook(x,y,tempboard);
            } else if(selected.isKnight){
                Knight selectedpiece = (Knight) selected;
                selectedpiece.moveknight(x,y,tempboard);
            } else if(selected.isQueen){
                Queen selectedpiece = (Queen) selected;
                selectedpiece.moveQueen(x,y,tempboard);
            } else if(selected.isKing){
                King selectedpiece = (King) selected;
                selectedpiece.moveKing(x,y,tempboard);
            }
            //if the user promoted then append = to the stringbuilder and play a sound, as well as append the notation of the promoted piece
            if(promotion){
                if(sounds)play("chess\\soundfx\\promote.wav");
                //movestr.append("=");
                movestr.append(promotiontype);
            }
        }
        //if the user is castling then append O-O or O-O-O to the stringbuilder based on the castling type and play a sound
        if(castle==0){
            if(sounds)play("chess\\soundfx\\castle.wav");
            //movestr.append("O-O"); short castle

            if(whitetomove)movestr.append("e1f1");
            else movestr.append("e8f8");
        } else if(castle==1){
            if(sounds)play("chess\\soundfx\\castle.wav");
            //movestr.append("O-O-O"); long castle

            if(whitetomove)movestr.append("e1c1");
            else movestr.append("e8c8");
        }

        if(!movestr.toString().equals(puzzlemoves[puzzlemovecounter])){
            movestatus.setIcon(wrongicon);
            for (JButton b : possiblemoves) {
                b.setBorder(null);
            }
            isSelected=false;
            currentdot--;
            undo.pop();
            ArrayList<JButton> enabledbuttons = new ArrayList<JButton>();
            findenabledbuttons(enabledbuttons);
            enablebuttons(enabledbuttons);
            return false;
        } else {
            movestatus.setIcon(righticon);
            puzzlemovecounter++;
        }

        if (whitetomove) {
            movetracker++;
            tablemodel.addRow(new Object[]{movestr.toString(),""});
        } else {
            fenmovecounter++;
            tablemodel.setValueAt(movestr.toString(), movetracker-1, 1);
        }
        updatecurrentdot();
        return true;
    }

    private void updatecurrentdot(){
        for (int row = 0; row < tablemodel.getRowCount(); row++) {
            for (int col = 0; col < tablemodel.getColumnCount(); col++) {
                Object cellValue = tablemodel.getValueAt(row, col);
                String cell = (String) cellValue;
                if(cell.length()>0 && cell.charAt(cell.length()-1)=='•')tablemodel.setValueAt(cell.substring(0, cell.length()-1), row, col);
            }
        }
        int currentrow = (currentdot-1)/2;
        int currentcol = (currentdot-1)%2;
        if(currentcol>-1 && currentrow>-1)tablemodel.setValueAt(tablemodel.getValueAt(currentrow, currentcol) + "•", currentrow, currentcol);
    }
    //Method for finding all the possible moves
    private ArrayList<JButton> findAllPossibleMoves(){
        ArrayList<JButton> findAllMoves = new ArrayList<JButton>();
        //finding all the possible moves for each piece
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!board[i][j].isempty && ((whitetomove && board[i][j].white) || (!whitetomove && !board[i][j].white))) {
                    if (board[i][j].ispawn) {
                        //creating a new Pawn object for the selected piece
                        Pawn selectedpiece = (Pawn) board[i][j];
                        if(savedpositions.size()>1){
                            var = savedpositions.get(savedpositions.size()-2).board;
                        }
                        //checking if the selected piece can move based on the list retrieved from the Pawn class and adding it to the ArrayList
                        for (int[] pos : selectedpiece.checkallmoves(board,var)) {
                            if(moveincheck(selectedpiece, pos)){
                                findAllMoves.add(boardbuttons[pos[0]][pos[1]]);
                            }
                        }
                        //creating a new Bishop object for the selected piece if the selected piece is a Bishop
                    }else if(board[i][j].isBishop){
                        Bishop selectedpiece = (Bishop) board[i][j];
                        //checking if the selected piece can move based on the list retrieved from the Bishop class and adding it to the ArrayList
                        for (int[] pos : selectedpiece.checkallmoves(board)) {
                            if(moveincheck(selectedpiece, pos)){
                                findAllMoves.add(boardbuttons[pos[0]][pos[1]]);
                            }
                        }
                        //creating a new Rook object for the selected piece if the selected piece is a Rook
                    } else if(board[i][j].isRook){
                        Rook selectedpiece = (Rook) board[i][j];
                        //checking if the selected piece can move based on the list retrieved from the Rook class and adding it to the ArrayList
                        for (int[] pos : selectedpiece.checkallmoves(board)) {
                            if(moveincheck(selectedpiece, pos)){
                                findAllMoves.add(boardbuttons[pos[0]][pos[1]]);
                            }
                        }
                        //creating a new Knight object for the selected piece if the selected piece is a Knight
                    } else if(board[i][j].isKnight){
                        Knight selectedpiece = (Knight) board[i][j];
                        //checking if the selected piece can move based on the list retrieved from the Knight class and adding it to the ArrayList
                        for (int[] pos : selectedpiece.checkallmoves(board)) {
                            if(moveincheck(selectedpiece, pos)){
                                findAllMoves.add(boardbuttons[pos[0]][pos[1]]);
                            }
                        }
                        //creating a new Queen object for the selected piece if the selected piece is a Queen
                    }else if(board[i][j].isQueen){
                        Queen selectedpiece = (Queen) board[i][j];
                        //checking if the selected piece can move based on the list retrieved from the Queen class and adding it to the ArrayList
                        for (int[] pos : selectedpiece.checkallmoves(board)) {
                            if(moveincheck(selectedpiece, pos)){
                                findAllMoves.add(boardbuttons[pos[0]][pos[1]]);
                            }
                        }
                        //creating a new King object for the selected piece if the selected piece is a King
                    } else if(board[i][j].isKing){
                        King selectedpiece = (King) board[i][j];
                        //checking if the selected piece can move based on the list retrieved from the King class and adding it to the ArrayList
                        for (int[] pos : selectedpiece.checkallmoves(board)) {
                            if(moveincheck(selectedpiece, pos)){
                                findAllMoves.add(boardbuttons[pos[0]][pos[1]]);
                            }
                        }
                    }
                }
            }
        }
        //Returning the ArrayList of possible moves
        return findAllMoves;
    }
    //Method for promoting a pawn
    private void promotion(Piece piece, int x, int y){

        int Side = boardbuttons[0][0].getHeight();

        int PosX = 1 + Side*piece.y;
        int PosY = piece.white ? 1 : 1+Side*3;

        JPanel promotionpanel = new JPanel(new GridLayout(5, 1));
        for(int i = 0; i<4; i++){
            JButton b = new JButton();
            switch(i){
                case 0 : b.setIcon(piece.white ? whitequeen : blackqueen);
                b.setName("q");
                    break;
                case 1 : b.setIcon(piece.white ? whiterook : blackrook);
                b.setName("r");
                    break;
                case 2 : b.setIcon(piece.white ? whitebishop : blackbishop);
                b.setName("b");
                    break;
                case 3 : b.setIcon(piece.white ? whiteknight : blackknight);
                b.setName("n");
                    break;
            }
            b.setFocusable(false);
            b.setBackground(Color.white);
            b.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    currentdot++;
                    undo.add(new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null));
                    if (board[x][y].isempty) {
                        if(displayMoves(x,y,false,true,-1,b.getName()))return;
                    }else{
                        if(displayMoves(x,y,true,true,-1,b.getName()))return;   
                    }

                    board[piece.x][piece.y] = new Piece(0, false);
                    switch(b.getName()){
                        case "q": board[x][y]=new Queen(piece.white, x, y);
                            break;
                        case "r": board[x][y]=new Rook(piece.white, x, y);
                            break;
                        case "b": board[x][y]=new Bishop(piece.white, x, y);
                            break;
                        case "n": board[x][y]=new Knight(piece.white, x, y);
                            break;
                    }

                    isSelected = false;
                    //resetting the en Passant possibilities
                    resetpassant();
                    //clearing the possible moves
                    possiblemoves.clear();
                    //updating the display
                    updatedisplay(board);
                    //switching the turn of the players
                    switchturn();
                    //updating castling rights
                    updatecastlingrights();
                    //adding the current position to the list of saved positions
                    Position p = new Position(board, fenmovecounter, fiftymoverule, castlingrights, whitetomove, savedpositions.size()>0 ? savedpositions.get(savedpositions.size()-1).board : null);
                    savedpositions.add(p);

                    //if the king is in check setting a red border to let the user know that the king is in check
                    if(KingInCheck(board)){
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if(whitetomove && board[i][j].isKing && board[i][j].white){
                                    boardbuttons[i][j].setBorder(BorderFactory.createLineBorder(Color.RED , 3,true));
                                } else if(!whitetomove && board[i][j].isKing && !board[i][j].white){
                                    boardbuttons[i][j].setBorder(BorderFactory.createLineBorder(Color.RED , 3,true));
                                }
                            }
                        }
                    }

                    fiftymoverule++;
                    layeredPane.remove(promotionpanel);
                    ArrayList<JButton> enabledbuttons = new ArrayList<JButton>();
                    findenabledbuttons(enabledbuttons);
                    enablebuttons(enabledbuttons);

                    layeredPane.revalidate();
                    layeredPane.repaint();
                    
                }
                
            });
            promotionpanel.add(b);
        }
        JButton xbutton = new JButton("X");
        xbutton.setFocusable(false);
        xbutton.setFont(new Font("Comic Sans MS", Font.BOLD, 31));
        xbutton.setBackground(Color.white);
        xbutton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                layeredPane.remove(promotionpanel);
                layeredPane.revalidate();
                layeredPane.repaint();
            }
            
        });
        promotionpanel.add(xbutton);

        promotionpanel.setBounds(PosX, PosY, Side, Side*5);
        layeredPane.add(promotionpanel, JLayeredPane.PALETTE_LAYER);
    }
    //Method for making the move of castling short for white and black
    private void castleshort(){
        if(whitetomove){
            board[7][4] = new Piece(0,true);
            board[7][5] = new Rook(true,7,5);
            board[7][6] = new King(true,7,6);
            board[7][7] = new Piece(0,true);
        } else if(!whitetomove){
            board[0][4] = new Piece(0,false);
            board[0][5] = new Rook(false,0,5);
            board[0][6] = new King(false,0,6);
            board[0][7] = new Piece(0,false);
        }
    }
    //Method for making the move of castling long for white and black
    private void castlelong(){
        if(whitetomove){
            board[7][4] = new Piece(0,true);
            board[7][3] = new Rook(true,7,5);
            board[7][2] = new King(true,7,6);
            board[7][0] = new Piece(0,true);
        } else if(!whitetomove){
            board[0][4] = new Piece(0,false);
            board[0][3] = new Rook(false,0,5);
            board[0][2] = new King(false,0,6);
            board[0][0] = new Piece(0,false);
        }
    }
    //Method to check for the opportunity of castling long
    private boolean checkCastleLong(){
        //if the king has not moved and the rook is in the starting position then castling long is possible
        if(whitetomove && !KingInCheck(board) && board[7][0].isRook){
            if (!whitekingMoved && !whiterook1) {
                Piece[][] tempboard = new Piece[8][8];
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        tempboard[i][j] = board[i][j];
                    }
                }
                King whiteking = (King) board[7][4];
                if(!KingInCheck(tempboard) && board[7][3].isempty){
                    whiteking.moveKing(7,3,tempboard);
                    if(!KingInCheck(tempboard) && board[7][2].isempty){
                        whiteking.moveKing(7,2,tempboard);
                        if(!KingInCheck(tempboard) && board[7][1].isempty){
                            return true;
                        }
                    }
                }
            }
        //if the king has not moved and the rook is in the starting position then castling long is possible
        } else if(!whitetomove && !KingInCheck(board) && board[0][0].isRook){
            if(!blackkingMoved && !blackrook1){
                Piece[][] tempboard = new Piece[8][8];
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        tempboard[i][j] = board[i][j];
                    }
                }
                King blackking = (King) board[0][4];
                if(!KingInCheck(tempboard) && tempboard[0][3].isempty){
                    blackking.moveKing(0,3,tempboard);
                    if(!KingInCheck(tempboard) && tempboard[0][2].isempty){
                        blackking.moveKing(0,2,tempboard);
                        if(!KingInCheck(tempboard) && board[0][1].isempty){
                            return true;
                        }
                    }
                }
                
            }
        }
        //otherwise castling is not possible
        return false;
    }
    //Method to check for the opportunity of castling short
    private boolean checkCastleShort(){
    //if the king has not moved and the rook is in the starting position then castling short is possible
    if(whitetomove && !KingInCheck(board)){
        if (!whitekingMoved && !whiterook2 && board[7][7].isRook) {
            Piece[][] tempboard = new Piece[8][8];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    tempboard[i][j] = board[i][j];
                }
            }
            King whiteking = (King) board[7][4];
            if(board[7][5].isempty){
                whiteking.moveKing(7,5,tempboard);
                if(!KingInCheck(tempboard) && board[7][6].isempty){
                    whiteking.moveKing(7,6,tempboard);
                    if(!KingInCheck(tempboard)){
                        return true;
                    }
                }
            }
        }
        //if the king has not moved and the rook is in the starting position then castling short is possible
    } else if(!whitetomove && !KingInCheck(board) && board[0][7].isRook){
        if(!blackkingMoved && !blackrook2){
            Piece[][] tempboard = new Piece[8][8];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    tempboard[i][j] = board[i][j];
                }
            }
            King blackking = (King) board[0][4];
            if(tempboard[0][5].isempty){
                blackking.moveKing(0,5,tempboard);
                if(!KingInCheck(tempboard) && tempboard[0][6].isempty){
                    blackking.moveKing(0,6,tempboard);
                    if(!KingInCheck(tempboard)){
                        return true;
                    }
                }
            }
            
        }
    }
    //otherwise castling is not possible
    return false;
    }
    //Method for switching the turn of the players
    private void switchturn(){
    if(whitetomove){
        whitetomove=false;
    } else {
        whitetomove=true;
    }
    }
    //Method for enabling the buttons from the enabled arraylist
    private void enablebuttons(ArrayList<JButton> enabled){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boardbuttons[i][j].setEnabled(false);
            }
        }
        for (JButton b : enabled) {
            b.setEnabled(true);
        }
    }
    //Method for finding the enabled buttons
    private void findenabledbuttons(ArrayList<JButton> enabled){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(whitetomove && !board[i][j].isempty && board[i][j].white){
                    enabled.add(boardbuttons[i][j]);
                } else if(!whitetomove && !board[i][j].isempty && !board[i][j].white){
                    enabled.add(boardbuttons[i][j]);
                }
            }
        }
    }
    //Method for checking if the king is in check
    private boolean KingInCheck(Piece[][] currentboard){
        int[] kingpos = null;
        if (whitetomove) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    //iterating the board to find the white king and storing its position
                    if(currentboard[i][j].isKing && currentboard[i][j].white){
                        kingpos = new int[]{i,j};
                    }
                }
            }
            //iterating the board to find all the opponents moves and if any of these moves sees the king
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if(!currentboard[i][j].isempty && !currentboard[i][j].white){
                        if(currentboard[i][j].isQueen){
                            Queen selected = (Queen) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if(currentboard[i][j].isBishop){
                            Bishop selected = (Bishop) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if(currentboard[i][j].isKnight){
                            Knight selected = (Knight) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if(currentboard[i][j].isRook){
                            Rook selected = (Rook) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if(currentboard[i][j].ispawn){
                            Pawn selected = (Pawn) currentboard[i][j];
                            if(savedpositions.size()>1){
                            var = savedpositions.get(savedpositions.size()-2).board;
                        }
                            for (int[] pos : selected.checkallmoves(currentboard,var)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if (currentboard[i][j].isKing) {
                            King selected = (King) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            //iterating the board to find the black king and storing its position
        }else if (!whitetomove) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if(currentboard[i][j].isKing && !currentboard[i][j].white){
                        kingpos = new int[]{i,j};
                    }
                }
            }
            //iterating the board to find all the opponents moves and if any of these moves sees the king
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if(!currentboard[i][j].isempty && currentboard[i][j].white){
                        if(currentboard[i][j].isQueen){
                            Queen selected = (Queen) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if(currentboard[i][j].isBishop){
                            Bishop selected = (Bishop) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if(currentboard[i][j].isKnight){
                            Knight selected = (Knight) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if(currentboard[i][j].isRook){
                            Rook selected = (Rook) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if(currentboard[i][j].ispawn){
                            Pawn selected = (Pawn) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard,var)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        } else if (currentboard[i][j].isKing) {
                            King selected = (King) currentboard[i][j];
                            for (int[] pos : selected.checkallmoves(currentboard)) {
                                if(pos[0]==kingpos[0] && pos[1]==kingpos[1]){
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        //otherwise the king is not in check
        return false;
    }
    //Method for setting the en Passant to false in case en passant was possible for a specific piece
    private void resetpassant(){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(board[i][j].ispawn){
                    Pawn access = (Pawn) board[i][j];
                    if(access.enPassantleft){
                        access.enPassantleft = false;
                        access.missedleft = true;
                    }
                    if(access.enPassantright){
                        access.enPassantright = false;
                        access.missedright = true;
                    }
                }
            }
        }
    }
    //Method for checking if the move does not lead to a check and determines if the king is pinned
    private boolean moveincheck(Piece selectedpiece, int[] pos){
            //Creating a temporary board
            Piece[][] tempboard = new Piece[8][8];
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        tempboard[i][j] = board[i][j];
                    }
                }
                //Determining the type of the piece and moving it to the new position
                if(selectedpiece.isQueen){
                    Queen a = (Queen) selectedpiece;
                    a.moveQueen(pos[0],pos[1],tempboard);
                } else if(selectedpiece.isBishop){
                    Bishop a = (Bishop) selectedpiece;
                    a.movebishop(pos[0],pos[1],tempboard);
                } else if(selectedpiece.isKnight){
                    Knight a = (Knight) selectedpiece;
                    a.moveknight(pos[0],pos[1],tempboard);
                } else if(selectedpiece.isRook){
                    Rook a = (Rook) selectedpiece;
                    a.moveRook(pos[0],pos[1],tempboard);
                } else if(selectedpiece.ispawn){
                    Pawn a = (Pawn) selectedpiece;
                    a.movepawn(pos[0],pos[1],tempboard);
                    if(savedpositions.size()>1){
                        var = savedpositions.get(savedpositions.size()-2).board;
                    }
                    if(a.enPassant(tempboard,var)!=null){
                        int[] newpos = null;  
                        if(savedpositions.size()>1){
                            var = savedpositions.get(savedpositions.size()-2).board;
                        }
                        for (int[] i : a.enPassant(board,var)) {
                            if(i!=null){
                                newpos = i;
                            }
                        }
                        if((!a.missedleft && a.enPassantleft) ^ (!a.missedright && a.enPassantright) && a.white && newpos[0]==pos[0] && newpos[1]==pos[1]){
                            tempboard[pos[0]+1][pos[1]]=new Piece(0,false);
                        } else if((!a.missedleft && a.enPassantleft) ^ (!a.missedright && a.enPassantright) && !a.white && newpos[0]==pos[0] && newpos[1]==pos[1]){
                            tempboard[pos[0]-1][pos[1]]=new Piece(0,true);
                        }
                    }
                } else if (selectedpiece.isKing) {
                    King a = (King) selectedpiece;
                    a.moveKing(pos[0],pos[1],tempboard);
                }  
                //if the pieces that moved lead to a check then the move is not allowed
                if(!KingInCheck(tempboard)){
                    return true;
                }
                return false;
        }
}