import java.io.*;
import javax.swing.*;

public class Stockfish {

     private String bestmove;
     private double cp;
     private String path = "chess\\utilities\\stockfish-windows-x86-64-avx2.exe";
     private ConfigReader config = new ConfigReader("chess\\utilities\\config.properties");
     private int depth = Integer.parseInt(config.getProperty("Depth"));

    private void FetchEvaluation(String fen, JLabel evaluationlabel, JLabel bestmovelabel){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(path);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            writer.write("uci\n");
            writer.flush();

            String line;
            while((line = reader.readLine())!=null){
                if(line.equals("uciok"))break;
            }

            writer.write("position fen " + fen + "\n");
            writer.flush();
            
            writer.write("go depth " + depth + "\n");
            writer.flush();
            String[] responsearr;
            while((line = reader.readLine())!=null){
                if(line.startsWith("info depth " + depth)){
                    responsearr = line.split(" ");
                    if(responsearr[8].equals("cp")){
                        cp = Double.parseDouble(responsearr[9])/100;
                        String[] temp = fen.split(" ");
                        if(temp[1].equals("b"))cp*=-1;
                        if(evaluationlabel!=null)evaluationlabel.setText("Evaluation: "+cp);
                    } else if(responsearr[8].equals("mate")){
                        if(evaluationlabel!=null)evaluationlabel.setText("mate in " + responsearr[9]);
                    }
                } else if(line.startsWith("bestmove")){
                    responsearr = line.split(" ");
                    bestmove = responsearr[1];
                    if(bestmovelabel!=null)bestmovelabel.setText("Best Move: "+bestmove);
                    break;
                }   
            }

            reader.close();
            writer.close();
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double SetEvaluation(String fen, JLabel evaluationlabel, JLabel bestmovelabel){
        Thread fetchThread = new Thread(() -> FetchEvaluation(fen,evaluationlabel,bestmovelabel));
        fetchThread.start();
        return cp;
    }

    private String Bot(String fen,String lvlvalue,boolean limitstrength,String elo,String botdepth, String movetime){
        String move=null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(path);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            writer.write("uci\n");
            writer.flush();

            String line;
            while((line = reader.readLine())!=null){
                if(line.equals("uciok"))break;
            }

            if(limitstrength){

                writer.write("setoption name Skill Level value " + lvlvalue + "\n");
                writer.flush();

                writer.write("setoption name UCI_LimitStrength value true\n");
                writer.flush();

                writer.write("setoption name UCI_Elo value " + elo + "\n");
                writer.flush();
            }

            writer.write("position fen " + fen + "\n");
            writer.flush();
            
            writer.write("go depth " + botdepth + " movetime " + movetime +"\n");
            writer.flush();
            String[] responsearr;
            while((line = reader.readLine())!=null){
                if(line.startsWith("bestmove")){
                    responsearr = line.split(" ");
                    move = responsearr[1];
                    break;
                }   
            }

            reader.close();
            writer.close();
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return move;
    }

    public String getBotMove(String fen, String difficulty){
        switch (difficulty) {
            case "Beginner":
                return Bot(fen,"1",true,"600","2","500");
            case "Casual":
                return Bot(fen,"4",true,"1000","7","750");
            case "Intermediate":
                return Bot(fen,"8",true,"1600","12","1000");
            case "Advanced":
                return Bot(fen,"15",true,"2100","17","1250");
            case "Impossible":
                return Bot(fen,"",false,"","30","1500");
        }
        return null;
    }

}