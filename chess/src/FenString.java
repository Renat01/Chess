public class FenString {

    public String createFEN(Piece[][] board, boolean whitetomove, boolean[] castlingrights, int fiftymoverule, int movecounter, String passant){
        StringBuilder fen = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int emptyCount = 0;
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece.isempty) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(getPieceChar(piece));
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            if (i < 7) fen.append('/');
        }

        fen.append(" ").append(whitetomove ? "w" : "b");

        fen.append(" ");
        if((castlingrights[0] || (!castlingrights[0] && castlingrights[1] && castlingrights[2])) && (castlingrights[3] || (!castlingrights[3] && castlingrights[4] && castlingrights[5])))fen.append("-");
        else{
            if(!castlingrights[2])fen.append("K");
            if(!castlingrights[1])fen.append("Q");
            if(!castlingrights[4])fen.append("k");
            if(!castlingrights[5])fen.append("q");
        }
        
        fen.append(" ").append(passant);
                
        fen.append(" ").append(fiftymoverule);
        
        fen.append(" ").append(movecounter);

        return fen.toString();
    }

    private String getPieceChar(Piece piece){
        String str = "";
        if(piece.isQueen){
            str = "q";
        } else if(piece.isBishop){
            str="b";
        } else if(piece.isKnight){
            str="n";
        } else if(piece.isRook){
            str="r";
        } else if(piece.isKing){
            str = "k";
        } else {
            str = "p";
        }
        return piece.white ? str.toUpperCase() : str;
    }

}