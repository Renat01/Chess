import java.util.ArrayList;
import java.util.HashMap;

public class Position {
   //creating a board to store the position
   Piece[][] board = new Piece[8][8];

   int fenmovecounter, fiftymoverule;

   boolean[] castlingrights = new boolean[6];

   boolean whitetomove;

   String fen;

   HashMap<Integer,Character> mapY = new HashMap<Integer,Character>();
   HashMap<Integer,Character> mapX = new HashMap<Integer,Character>();

   //previous board for en passant
   Piece[][] oldboard = new Piece[8][8];

   public Position(Piece[][] newboard, int newfenmovecounter, int newfiftymoverule, boolean[] newcastlingrights, boolean newwhitetomove, Piece[][] newoldboard){
       copyboard(board, newboard);
       if(newoldboard!=null)copyboard(oldboard, newoldboard);
       this.fenmovecounter=newfenmovecounter;
       this.fiftymoverule=newfiftymoverule;
       for(int i = 0; i<6; i++)this.castlingrights[i]=newcastlingrights[i];
       this.whitetomove=newwhitetomove;

       positionmaps();
       makefen();
   }

   private void copyboard(Piece[][] empty, Piece[][] copy){
       for (int i = 0; i < 8; i++) {
           for (int j = 0; j < 8; j++) {
               if (copy[i][j].ispawn) {
                   Pawn pawn = (Pawn) copy[i][j];
                   empty[i][j] = new Pawn(pawn.white,i,j,pawn.movecounter,pawn.missedleft,pawn.missedright,pawn.enPassantleft,pawn.enPassantright);
               } else if(copy[i][j].isBishop){
                   Bishop bishop = (Bishop) copy[i][j];
                   empty[i][j] = new Bishop(bishop.white,i,j);
               } else if(copy[i][j].isRook){
                   Rook rook = (Rook) copy[i][j];
                   empty[i][j] = new Rook(rook.white,i,j);
               } else if(copy[i][j].isKnight){
                   Knight knight = (Knight) copy[i][j];
                   empty[i][j] = new Knight(knight.white,i,j);
               }else if(copy[i][j].isQueen){
                   Queen queen = (Queen) copy[i][j];
                   empty[i][j] = new Queen(queen.white,i,j);
               }else if(copy[i][j].isKing){
                   King king = (King) copy[i][j];
                   empty[i][j] = new King(king.white,i,j);
               } else if (copy[i][j].isempty) {
                   empty[i][j] = new Piece(0,copy[i][j].white);
               }
           }
       }
   }

   public void positionmaps(){
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
}

   private String passantstring(Piece[][] board, Piece[][] oldboard){
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
            if(board[i][j].ispawn){
                Pawn access = (Pawn) board[i][j];
                ArrayList<int[]> passants = access.enPassant(board,oldboard);
                if(passants!=null){
                    for (int k = 0; k < passants.size(); k++) {
                        if(passants.get(k)!=null){
                            sb.append(mapY.get(passants.get(k)[1]));
                            sb.append(mapX.get(passants.get(k)[0]));
                        }
                    }
                }
            }
        }
    }
    return sb.isEmpty() ? "-" : sb.toString();
}

   private void makefen(){
       fen = new FenString().createFEN(this.board, this.whitetomove, this.castlingrights, this.fiftymoverule, this.fenmovecounter, oldboard==null ? "-" : passantstring(board, oldboard));
   }

   //Method for checking if the position is the same as the compared position
   public boolean same(Position compare){
       String[] fen1 = fen.split(" ");
       String[] fen2 = compare.fen.split(" ");
       for(int i=0;i<4;i++)if(!fen1[i].equals(fen2[i]))return false;
       return true;
   }
}
