import java.awt.*;
import java.awt.image.*;
import java.util.*;

/*
Initial setup to get working:
  Change ScreenWidth, ScreenHeight
  Make sure TOT_MINES is correct

Hopefully it works then
*/

public class MSolver{

  static BufferedImage screenShotImage(){
    try {
      Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
      ScreenWidth = captureSize.width;
      ScreenHeight = captureSize.height;
      BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
      return bufferedImage;
    }
    catch(Exception e) { e.printStackTrace(); }
    return null;
  }

  static int ScreenWidth = 1600;
  static int ScreenHeight = 900;


  static boolean isDark(int rgb){
    int red = (rgb >> 16) & 0xFF;
    int green = (rgb >> 8) & 0xFF;
    int blue = rgb & 0xFF;
    return red + green + blue < 120;
  }

  static int colorDifference(int r1, int g1, int b1, int r2, int g2, int b2){
    return Math.abs(r1 - r2) + Math.abs(b1 - b2) + Math.abs(g1 - g2);
  }


  static int BoardWidth = 0;
  static int BoardHeight = 1;
  static double BoardPix = 0;
  static int BoardTopW = 0;
  static int BoardTopH = 0;



  // Take a screenshot and try to figure out the board dimensions and stuff like that
  static void calibrate(){

    // Display this message
    System.out.println("Calibrating Screen...");

    BufferedImage bi = screenShotImage();
    bi.createGraphics();
    Graphics2D g = (Graphics2D)bi.getGraphics();


    int hh = 0; // boardheight of previous column
    int firh = 0; // position of first found
    int firw = 0;
    int lash = 0; // position of last found
    int lasw = 0;
    int tot = 0; // total number of crosses found


    for(int w = 0; w<ScreenWidth; w++){

      for(int h=0; h < ScreenHeight; h++){
        int rgb = bi.getRGB(w,h);

        if(isDark(rgb)){

          if(w<10 || h<10 || w>ScreenWidth-10 || h> ScreenHeight-10)
            continue;

          // look for the cross shape to indicate position on board
          // we consider it a cross if:
          //   - the square is dark
          //   - four selected pixels to the N,S,E,W are dark
          //   - four selected pixels to the NE, SE, NW, SW are not dark
          if(isDark(bi.getRGB(w+7,h)))
          if(isDark(bi.getRGB(w-7,h)))
          if(isDark(bi.getRGB(w,h+7)))
          if(isDark(bi.getRGB(w,h-7)))
          if(isDark(bi.getRGB(w+3,h)))
          if(isDark(bi.getRGB(w-3,h)))
          if(isDark(bi.getRGB(w,h+3)))
          if(isDark(bi.getRGB(w,h-3)))
          if(!isDark(bi.getRGB(w-7,h-7)))
          if(!isDark(bi.getRGB(w+7,h-7)))
          if(!isDark(bi.getRGB(w-7,h+7)))
          if(!isDark(bi.getRGB(w+7,h+7)))
          if(!isDark(bi.getRGB(w-3,h-3)))
          if(!isDark(bi.getRGB(w+3,h-3)))
          if(!isDark(bi.getRGB(w-3,h+3)))
          if(!isDark(bi.getRGB(w+3,h+3))){
            
            g.setColor(Color.YELLOW); // for _calibrate.png
            g.fillRect(w-3,h-3,7,7);
            tot++;
            BoardHeight++;

            // Find the position of the first cross
            if(firh == 0){
              firh = h;
              firw = w;
            }

            // Note position of the last cross
            lash = h;
            lasw = w;
          }
        }


      }

      if(BoardHeight > 1){
        hh = BoardHeight;
        BoardHeight = 1;
      }
    }

    // Determine boardwidth from total and boardheight
    BoardHeight = hh;
    if(tot % (BoardHeight-1) == 0)
      BoardWidth = tot / (BoardHeight-1) + 1;
    else BoardWidth = 0;

    // Determine BoardPix by taking an average
    BoardPix = 0.5*((double)(lasw - firw) / (double)(BoardWidth-2))
             + 0.5*((double)(lash - firh) / (double)(BoardHeight-2));
    
    // Determine first cell position (where to click)
    int halfsiz = (int)BoardPix / 2;
    BoardTopW = firw - halfsiz + 3;
    BoardTopH = firh - halfsiz + 3;


    System.out.printf("BoardWidth=%d, BoardHeight=%d, BoardPix=%f\n", BoardWidth, BoardHeight, BoardPix);
    System.out.printf("BoardTopW=%d, BoardTopH=%d\n",BoardTopW, BoardTopH);


  }


  static int mouseLocX = ScreenWidth / 2;
  static int mouseLocY = ScreenHeight / 2;
  static void moveMouse(int mouseX, int mouseY) throws Throwable{

    int distance = Math.max(Math.abs(mouseX-mouseLocX), Math.abs(mouseY-mouseLocY));
    int DELAY = distance/4;
    int numSteps = DELAY / 5;

    double stepx = (double)(mouseX - mouseLocX) / (double)numSteps;
    double stepy = (double)(mouseY - mouseLocY) / (double)numSteps;

    for(int i=0; i<numSteps; i++){
      robot.mouseMove(mouseLocX + (int)(i*stepx), mouseLocY + (int)(i*stepy));
      Thread.sleep(5);
    }
    robot.mouseMove(mouseX, mouseY);
    mouseLocX = mouseX;
    mouseLocY = mouseY;
  }


  // Click on a given square, given i and j
  static void clickOn(int i, int j) throws Throwable{
    int mouseX = BoardTopW + (int)(j*BoardPix);
    int mouseY = BoardTopH + (int)(i*BoardPix);
    moveMouse(mouseX, mouseY);

    robot.mousePress(16);
    Thread.sleep(5);
    robot.mouseRelease(16);
    Thread.sleep(10);
  }


  // Manually flag some mine
  static void flagOn(int i, int j) throws Throwable{
    int mouseX = BoardTopW + (int)(j*BoardPix);
    int mouseY = BoardTopH + (int)(i*BoardPix);
    moveMouse(mouseX, mouseY);

    robot.mousePress(4);
    Thread.sleep(5);
    robot.mouseRelease(4);
    Thread.sleep(10);
  }


  // Click on it with both mouse buttons in order to "chord"
  static void chordOn(int i, int j) throws Throwable{
    int mouseX = BoardTopW + (int)(j*BoardPix);
    int mouseY = BoardTopH + (int)(i*BoardPix);
    moveMouse(mouseX, mouseY);

    robot.mousePress(4);
    robot.mousePress(16);
    Thread.sleep(5);
    robot.mouseRelease(4);
    robot.mouseRelease(16);
    Thread.sleep(10);
  }


  // Special method to try to separate 3 from 7
  // which conveniently are the same color
  static int detect_3_7(int[] areapix){

    // Assume it's length 225 and dimensions 15x15.
    // Classify each pixel as red or not.
    // Since we don't have to deal with 5, we can take a greater liberty
    // in deciding on red pixels.

    boolean redx[][] = new boolean[15][15];
    for(int k=0; k<225; k++){
      int i = k % 15;
      int j = k / 15;
      int rgb = areapix[k];
      int red = (rgb >> 16) & 0xFF;
      int green = (rgb >> 8) & 0xFF;
      int blue = rgb & 0xFF;

      if(colorDifference(red, green, blue, 170, 0, 0) < 100)
        redx[i][j] = true;
    }

    /*
    for(int i=0; i<15; i++){
      for(int j=0; j<15; j++){
        if(redx[i][j]) System.out.print("x");
        else System.out.print(" ");
      }
      System.out.println();
    }
    System.out.println();
    */


    // Look for this pattern in the 3 but not 7:
    // x x
    // x .
    /*
    for(int i=0; i<14; i++){
      for(int j=0; j<14; j++){
        if(redx[i][j] && redx[i+1][j] 
             && redx[i][j+1] && !redx[i+1][j+1])
          return 3;
      }
    }
    */

    // . . .
    //   x
    for(int i=0; i<13; i++){
      for(int j=0; j<13; j++){
        if(!redx[i][j] && !redx[i][j+1] && !redx[i][j+2] && redx[i+1][j+1])
          return 3;
      }
    }

    return 7;

  }


  // Try to read what number's in this position
  static int detect(BufferedImage bi, int i, int j){
    int mouseX = BoardTopW + (int)(j*BoardPix);
    int mouseY = BoardTopH + (int)(i*BoardPix);

    // Don't take one pixel, take a 15x15 area of pixels
    int areapix[] = new int[225];
    int count = 0;
    for(int ii = mouseX-7; ii <= mouseX+7; ii++)
      for(int jj = mouseY-7; jj <= mouseY+7; jj++){
        areapix[count] = bi.getRGB(ii,jj);
        count++;
      }


    boolean hasColorOfOneSquare = false;
    boolean hasColorOfBlank = false;
    boolean isRelativelyHomogenous = true;

    for(int rgb : areapix){
      int red = (rgb >> 16) & 0xFF;
      int green = (rgb >> 8) & 0xFF;
      int blue = rgb & 0xFF;

      // Detect death
      if(colorDifference(red, green, blue, 110,110,110) < 20)
        return -10;

      // Detect flagging of any sort
      if(colorDifference(red,green,blue,255,0,0) < 30)
        return -3;

      if(colorDifference(red, green, blue, 65,79,188) < 10){
        hasColorOfOneSquare = true;
      }
      if(blue > red && blue > green &&
          colorDifference(red, green, blue, 220,220,255) < 200){
        hasColorOfBlank = true;
      }
      if(colorDifference(red, green, blue, 167,3,5) < 20)
        return detect_3_7(areapix);
      if(colorDifference(red, green, blue, 29,103,4) < 20) return 2;
      if(colorDifference(red, green, blue, 0,0,138) < 20) return 4;
      if(colorDifference(red, green, blue, 124,1,3) < 20) return 5;
      if(colorDifference(red, green, blue, 7,122,131) < 20) return 6;
    }

    // Determine how 'same' the area is.
    // This is to separate the empty areas which are relatively the same from
    // the unexplored areas which have a gradient of some sort.
    {
      int rgb00 = areapix[0];
      int red00 = (rgb00 >> 16) & 0xFF;
      int green00 = (rgb00 >> 8) & 0xFF;
      int blue00 = rgb00 & 0xFF;
      for(int rgb : areapix){
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        if(colorDifference(red, green, blue, red00, green00, blue00) > 60){
          isRelativelyHomogenous = false;
          break;
        }
      }
    }



    if(hasColorOfOneSquare && hasColorOfBlank)
      return 1;

    if(hasColorOfBlank && isRelativelyHomogenous)
      return 0;

    return -1;
  }


  static Scanner scanner = new Scanner(System.in);
  static Robot robot;
  static Random rand = new Random();


  // Internal representation of the board state as displayed on the screen.
  // 1-8 means that the square there is that number
  // 0 means that it's actually empty
  // -1 means it's not opened yet
  // -2 means it's outside the boundries of the board
  // -3 means a mine
  // -10 means that something went wrong and we should exit the program
  static int[][] onScreen = null;

  // List of squares in which we know there are mines
  static boolean[][] flags = null;

  static int numMines = 0;
  static int TOT_MINES = 99;


  // Remove the need for edge detection every fricking time
  static int onScreen(int i, int j){
    if(i < 0 || j < 0 || i > BoardHeight-1 || j > BoardWidth-1)
      return -10;
    return onScreen[i][j];
  }


  // take a screenshot and update the onScreen array
  static int updateOnScreen(){
    BufferedImage bi = screenShotImage();

    int numMines_t = 0;
    for(int i = 0; i < BoardHeight; i++){
      for(int j=0; j < BoardWidth; j++){

        int d = detect(bi,i,j);
        if(d == -10) return d; // death
        onScreen[i][j] = d;

        // Special case for flags
        if(d == -3 || flags[i][j]){
          onScreen[i][j] = -1;
          flags[i][j] = true;
        }
        if(d == -1){
          flags[i][j] = false;
        }
        

        // Update mines count
        if(flags[i][j]){
          numMines_t++;
        }

      }
    }

    //if(numMines_t < numMines - 2) exit();
    numMines = numMines_t;
    return 0;
  }


  // tries to detect problems with screenshotting
  static boolean checkConsistency(){
    for(int i=0; i<BoardHeight; i++){
      for(int j=0; j<BoardWidth; j++){
        
        int freeSquares = countFreeSquaresAround(onScreen, i, j);
        int numFlags = countFlagsAround(flags, i, j);

        if(onScreen(i,j) == 0 && freeSquares > 0){
          return false;
        }
        if((onScreen(i,j) - numFlags) > 0 && freeSquares == 0){
          return false;
        }

      }
    }

    return true;
  }


  // Handle clicking the first square
  static void firstSquare() throws Throwable{

    // Check that it's indeed the first square
    robot.mouseMove(0,0);
    Thread.sleep(20);
    updateOnScreen();
    robot.mouseMove(mouseLocX,mouseLocY);
    boolean isUntouched = true;
    for(int i=0; i<BoardHeight; i++){
      for(int j=0; j<BoardWidth; j++){
        if(onScreen(i,j) != -1)
          isUntouched = false;
      }
    }
    if(!isUntouched){
      return;
    }

    // Click the middle
    clickOn(BoardHeight/2-1, BoardWidth/2-1);
    clickOn(BoardHeight/2-1, BoardWidth/2-1);
    Thread.sleep(200);

  }


  // How many flags exist around this square?
  static int countFlagsAround(boolean[][] array, int i, int j){
    int mines = 0;

    // See if we're on the edge of the board
    boolean oU = false, oD = false, oL = false, oR = false;
    if(i == 0) oU = true;
    if(j == 0) oL = true;
    if(i == BoardHeight-1) oD = true;
    if(j == BoardWidth-1) oR = true;

    if(!oU && array[i-1][j]) mines++;
    if(!oL && array[i][j-1]) mines++;
    if(!oD && array[i+1][j]) mines++;
    if(!oR && array[i][j+1]) mines++;
    if(!oU && !oL && array[i-1][j-1]) mines++;
    if(!oU && !oR && array[i-1][j+1]) mines++;
    if(!oD && !oL && array[i+1][j-1]) mines++;
    if(!oD && !oR && array[i+1][j+1]) mines++;

    return mines;
  }

  // How many unopened squares around this square?
  static int countFreeSquaresAround(int[][] board, int i, int j){
    int freeSquares = 0;

    if(onScreen(i-1,j)==-1) freeSquares++;
    if(onScreen(i+1,j)==-1) freeSquares++;
    if(onScreen(i,j-1)==-1) freeSquares++;
    if(onScreen(i,j+1)==-1) freeSquares++;
    if(onScreen(i-1,j-1)==-1) freeSquares++;
    if(onScreen(i-1,j+1)==-1) freeSquares++;
    if(onScreen(i+1,j-1)==-1) freeSquares++;
    if(onScreen(i+1,j+1)==-1) freeSquares++;

    return freeSquares;
  }

  // A boundry square is an unopened square with opened squares near it.
  static boolean isBoundry(int[][] board, int i, int j){
    if(board[i][j] != -1) return false;

    boolean oU = false, oD = false, oL = false, oR = false;
    if(i == 0) oU = true;
    if(j == 0) oL = true;
    if(i == BoardHeight-1) oD = true;
    if(j == BoardWidth-1) oR = true;
    boolean isBoundry = false;

    if(!oU && board[i-1][j]>=0) isBoundry = true;
    if(!oL && board[i][j-1]>=0) isBoundry = true;
    if(!oD && board[i+1][j]>=0) isBoundry = true;
    if(!oR && board[i][j+1]>=0) isBoundry = true;
    if(!oU && !oL && board[i-1][j-1]>=0) isBoundry = true;
    if(!oU && !oR && board[i-1][j+1]>=0) isBoundry = true;
    if(!oD && !oL && board[i+1][j-1]>=0) isBoundry = true;
    if(!oD && !oR && board[i+1][j+1]>=0) isBoundry = true;

    return isBoundry;
  }


  // Attempt to deduce squares that we know have mines
  // More specifically if number of squares around it = its number
  static void attemptFlagMine() throws Throwable{

    for(int i=0; i<BoardHeight; i++){
      for(int j=0; j<BoardWidth; j++){
        
        if(onScreen(i,j) >= 1){
          int curNum = onScreen(i,j);

          // Flag necessary squares
          if(curNum == countFreeSquaresAround(onScreen,i,j)){
            for(int ii=0; ii<BoardHeight; ii++){
              for(int jj=0; jj<BoardWidth; jj++){
                if(Math.abs(ii-i)<=1 && Math.abs(jj-j)<=1){
                  if(onScreen(ii,jj) == -1 && !flags[ii][jj]){
                    flags[ii][jj] = true;
                    flagOn(ii,jj);
                  }
                }
              }
            }
          }


        }
      }
    }

  }


  // Attempt to deduce a spot that should be free and click it
  // More specifically:
  // Find a square where the number of flags around it is the same as it
  // Then click every empty square around it
  static void attemptMove() throws Throwable{

    boolean success = false;
    for(int i=0; i<BoardHeight; i++){
      for(int j=0; j<BoardWidth; j++){
        
        if(onScreen(i,j) >= 1){

          // Count how many mines around it
          int curNum = onScreen[i][j];
          int mines = countFlagsAround(flags,i,j);
          int freeSquares = countFreeSquaresAround(onScreen,i,j);


          // Click on the deduced non-mine squares
          if(curNum == mines && freeSquares > mines){
            success = true;

            // Use the chord or the classical algorithm
            if(freeSquares - mines > 1){
              chordOn(i,j);
              onScreen[i][j] = 0; // hack to make it not overclick a square
              continue;
            }

            // Old algorithm: don't chord
            for(int ii=0; ii<BoardHeight; ii++){
              for(int jj=0; jj<BoardWidth; jj++){
                if(Math.abs(ii-i)<=1 && Math.abs(jj-j)<=1){
                  if(onScreen(ii,jj) == -1 && !flags[ii][jj]){
                    clickOn(ii,jj);
                    onScreen[ii][jj] = 0;
                  }
                }
              }
            }

          }
        }
      }
    }

    if(success) return;

    // Bring in the big guns
    tankSolver();
  }


  // Exactly what it says on the tin
  static void guessRandomly() throws Throwable{
    System.out.println("Attempting to guess randomly");
    while(true){
      int k = rand.nextInt(BoardHeight*BoardWidth);
      int i = k / BoardWidth;
      int j = k % BoardWidth;

      if(onScreen(i,j) == -1 && !flags[i][j]){
        clickOn(i,j);
        return;
      }
    }
  }

  
  // TANK solver: slow and heavyweight backtrack solver designed to
  // solve any conceivable position! (in development)
  static void tankSolver() throws Throwable{

    // Be extra sure it's consistent
    Thread.sleep(100);
    robot.mouseMove(0,0);
    Thread.sleep(20);
    updateOnScreen();
    robot.mouseMove(mouseLocX,mouseLocY);
    //dumpPosition();
    if(!checkConsistency()) return;

    // Timing
    long tankTime = System.currentTimeMillis();

    ArrayList<Pair> borderTiles = new ArrayList<Pair>();
    ArrayList<Pair> allEmptyTiles = new ArrayList<Pair>();

    // Endgame case: if there are few enough tiles, don't bother with border tiles.
    borderOptimization = false;
    for(int i=0; i<BoardHeight; i++)
      for(int j=0; j<BoardWidth; j++)
        if(onScreen(i,j) == -1 && !flags[i][j])
          allEmptyTiles.add(new Pair(i,j));

    // Determine all border tiles
    for(int i=0; i<BoardHeight; i++)
      for(int j=0; j<BoardWidth; j++)
        if(isBoundry(onScreen,i,j) && !flags[i][j])
          borderTiles.add(new Pair(i,j));

    // Count how many squares outside the knowable range
    int numOutSquares = allEmptyTiles.size() - borderTiles.size();
    if(numOutSquares > BF_LIMIT){
      borderOptimization = true;
    }
    else borderTiles = allEmptyTiles;


    // Something went wrong
    if(borderTiles.size() == 0)
      return;


    // Run the segregation routine before recursing one by one
    // Don't bother if it's endgame as doing so might make it miss some cases
    ArrayList<ArrayList<Pair>> segregated;
    if(!borderOptimization){
      segregated = new ArrayList<ArrayList<Pair>>();
      segregated.add(borderTiles);
    }
    else segregated = tankSegregate(borderTiles);

    int totalMultCases = 1;
    boolean success = false;
    double prob_best = 0; // Store information about the best probability
    int prob_besttile = -1;
    int prob_best_s = -1;
    for(int s = 0; s < segregated.size(); s++){

      // Copy everything into temporary constructs
      tank_solutions = new ArrayList<boolean[]>();
      tank_board = onScreen.clone();
      knownMine = flags.clone();

      knownEmpty = new boolean[BoardHeight][BoardWidth];
      for(int i=0; i<BoardHeight; i++)
        for(int j=0; j<BoardWidth; j++)
          if(tank_board[i][j] >= 0)
            knownEmpty[i][j] = true;
          else knownEmpty[i][j] = false;


      // Compute solutions -- here's the time consuming step
      tankRecurse(segregated.get(s),0);

      // Something screwed up
      if(tank_solutions.size() == 0) return;


      // Check for solved squares
      for(int i=0; i<segregated.get(s).size(); i++){
        boolean allMine = true;
        boolean allEmpty = true;
        for(boolean[] sln : tank_solutions){
          if(!sln[i]) allMine = false;
          if(sln[i]) allEmpty = false;
        }

        
        Pair<Integer,Integer> q = segregated.get(s).get(i);
        int qi = q.getFirst();
        int qj = q.getSecond();

        // Muahaha
        if(allMine){
          flags[qi][qj] = true;
          flagOn(qi,qj);
        }
        if(allEmpty){
          success = true;
          clickOn(qi,qj);
        }
      }

      totalMultCases *= tank_solutions.size();

      
      // Calculate probabilities, in case we need it
      if(success) continue;
      int maxEmpty = -10000;
      int iEmpty = -1;
      for(int i=0; i<segregated.get(s).size(); i++){
        int nEmpty = 0;
        for(boolean[] sln : tank_solutions){
          if(!sln[i]) nEmpty++;
        }
        if(nEmpty > maxEmpty){
          maxEmpty = nEmpty;
          iEmpty = i;
        }
      }
      double probability = (double)maxEmpty / (double)tank_solutions.size();

      if(probability > prob_best){
        prob_best = probability;
        prob_besttile = iEmpty;
        prob_best_s = s;
      }

    }

    // But wait! If there's any hope, bruteforce harder (by a factor of 32x)!
    if(BF_LIMIT == 8 && numOutSquares > 8 && numOutSquares <= 13){
      System.out.println("Extending bruteforce horizon...");
      BF_LIMIT = 13;
      tankSolver();
      BF_LIMIT = 8;
      return;
    }

    tankTime = System.currentTimeMillis() - tankTime;
    if(success){
      System.out.printf(
          "TANK Solver successfully invoked at step %d (%dms, %d cases)%s\n",
          numMines, tankTime, totalMultCases, (borderOptimization?"":"*"));
      return;
    }


    // Take the guess, since we can't deduce anything useful
    System.out.printf(
        "TANK Solver guessing with probability %1.2f at step %d (%dms, %d cases)%s\n",
        prob_best, numMines, tankTime, totalMultCases,
        (borderOptimization?"":"*"));
    Pair<Integer,Integer> q = segregated.get(prob_best_s).get(prob_besttile);
    int qi = q.getFirst();
    int qj = q.getSecond();
    clickOn(qi,qj);

  }


  // Segregation routine: if two regions are independant then consider
  // them as separate regions
  static ArrayList<ArrayList<Pair>>
            tankSegregate(ArrayList<Pair> borderTiles){

    ArrayList<ArrayList<Pair>> allRegions = new ArrayList<ArrayList<Pair>>();
    ArrayList<Pair> covered = new ArrayList<Pair>();

    while(true){

      LinkedList<Pair> queue = new LinkedList<Pair>();
      ArrayList<Pair> finishedRegion = new ArrayList<Pair>();

      // Find a suitable starting point
      for(Pair firstT : borderTiles){
        if(!covered.contains(firstT)){
          queue.add(firstT);
          break;
        }
      }

      if(queue.isEmpty())
        break;

      while(!queue.isEmpty()){

        Pair<Integer,Integer> curTile = queue.poll();
        int ci = curTile.getFirst();
        int cj = curTile.getSecond();

        finishedRegion.add(curTile);
        covered.add(curTile);

        // Find all connecting tiles
        for(Pair<Integer,Integer> tile : borderTiles){
          int ti = tile.getFirst();
          int tj = tile.getSecond();

          boolean isConnected = false;

          if(finishedRegion.contains(tile))
            continue;
          
          if(Math.abs(ci-ti)>2 || Math.abs(cj-tj) > 2)
            isConnected = false;

          else{
            // Perform a search on all the tiles
            tilesearch:
            for(int i=0; i<BoardHeight; i++){
              for(int j=0; j<BoardWidth; j++){
                if(onScreen(i,j) > 0){
                  if(Math.abs(ci-i) <= 1 && Math.abs(cj-j) <= 1 &&
                      Math.abs(ti-i) <= 1 && Math.abs(tj-j) <= 1){
                    isConnected = true;
                    break tilesearch;
                  }
                }
              }
            }
          }
          
          if(!isConnected) continue;

          if(!queue.contains(tile))
            queue.add(tile);

        }
      }

      allRegions.add(finishedRegion);

    }

    return allRegions;

  }



  static int[][] tank_board = null;
  static boolean[][] knownMine = null;
  static boolean[][] knownEmpty = null;
  static ArrayList<boolean[]> tank_solutions;
  
  // Should be true -- if false, we're bruteforcing the endgame
  static boolean borderOptimization;
  static int BF_LIMIT = 8;

  // Recurse from depth k (0 is root)
  // Assumes the tank variables are already set; puts solutions in
  // the static arraylist.
  static void tankRecurse(ArrayList<Pair> borderTiles, int k){

    // Return if at this point, it's already inconsistent
    int flagCount = 0;
    for(int i=0; i<BoardHeight; i++)
      for(int j=0; j<BoardWidth; j++){

        // Count flags for endgame cases
        if(knownMine[i][j])
          flagCount++;

        int num = tank_board[i][j];
        if(num < 0) continue;

        // Total bordering squares
        int surround = 0;
        if((i==0&&j==0) || (i==BoardHeight-1 && j==BoardWidth-1))
          surround = 3;
        else if(i==0 || j==0 || i==BoardHeight-1 || j==BoardWidth-1)
          surround = 5;
        else surround = 8;

        int numFlags = countFlagsAround(knownMine, i,j);
        int numFree = countFlagsAround(knownEmpty, i,j);
        
        // Scenario 1: too many mines
        if(numFlags > num) return;

        // Scenario 2: too many empty
        if(surround - numFree < num) return;
      }

    // We have too many flags
    if(flagCount > TOT_MINES)
      return;


    // Solution found!
    if(k == borderTiles.size()){

      // We don't have the exact mine count, so no
      if(!borderOptimization && flagCount < TOT_MINES)
        return;

      boolean[] solution = new boolean[borderTiles.size()];
      for(int i=0; i<borderTiles.size(); i++){
        Pair<Integer,Integer> s = borderTiles.get(i);
        int si = s.getFirst();
        int sj = s.getSecond();
        solution[i] = knownMine[si][sj];
      }
      tank_solutions.add(solution);
      return;
    }

    Pair<Integer,Integer> q = borderTiles.get(k);
    int qi = q.getFirst();
    int qj = q.getSecond();

    // Recurse two positions: mine and no mine
    knownMine[qi][qj] = true;
    tankRecurse(borderTiles, k+1);
    knownMine[qi][qj] = false;

    knownEmpty[qi][qj] = true;
    tankRecurse(borderTiles, k+1);
    knownEmpty[qi][qj] = false;

  }


  /*
    todo -
      read / write calibration settings
      
    Minor defects / bugs:
      1) Calibration routine kind of sucks, can't calibrate at
           small resolutions and can't calibrate non-empty board
      2) Death / win detection kind of sucks, can't distinguish
           win from loss and sometimes fails to detect either
      3) Clicking order is highly non-human
      4) Endgame solver is inefficient: we could make it kick in earlier if
           it was more efficient

    Known but non-fixable defects:
      1) Cannot automatically detect number of mines
  */

  public static void main(String[] args) throws Throwable {
    Thread.sleep(2000);
    robot = new Robot();

    // Keep these as these are the most common settings
    BoardWidth = 30;
    BoardHeight = 16;
    /*
    BoardPix = 35.267857;
    BoardTopW = 175;
    BoardTopH = 120;
    */
    BoardPix = 42.035714;
    BoardTopW = 193;
    BoardTopH = 134;

    // Determine board height and width and position
    calibrate();
    if(BoardWidth < 9 || BoardHeight < 9 || BoardWidth > 30 || BoardWidth > 30){
      System.out.println("Calibration Failed.");
      return;
    }


    // Initialize internal constructs
    onScreen = new int[BoardHeight][BoardWidth];
    flags = new boolean[BoardHeight][BoardWidth];
    for(int i=0; i<BoardHeight; i++) for(int j=0; j<BoardWidth; j++) flags[i][j]=false;

    // Debugging: is it reading correctly?
    /*
    updateOnScreen();
    dumpPosition();
    tankSolver();
    exit();
    */
  

    firstSquare();
    for(int c=0; c<1000000; c++){
      int status = updateOnScreen();
      if(!checkConsistency()){
        robot.mouseMove(0,0);
        status = updateOnScreen();
        robot.mouseMove(mouseLocX,mouseLocY);
        if(status == -10) exit();
        continue;
      }
      // Exit on death
      if(status == -10) exit();
      attemptFlagMine();
      attemptMove();
    }


  }

  static void exit(){
    // For any reason, we want to exit
    //System.out.println("Steps: " + numMines);
    System.exit(0);
  }


  // Debugging: for whatever reason, dump the board
  static void dumpPosition(){
    for(int i = 0; i < BoardHeight; i++){
      for(int j=0; j < BoardWidth; j++){

        int d = onScreen(i,j);
        if(flags[i][j])
          System.out.print(".");
        else if(d >= 1)
          System.out.print(d);
        else if(d == 0)
          System.out.print(" ");
        else System.out.print("#");

      }
      System.out.println();
    }
    System.out.println();
  }
}


// Copied from http://stackoverflow.com/questions/156275/
class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
        super();
        this.first = first;
        this.second = second;
    }

    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
        if (other instanceof Pair) {
                Pair otherPair = (Pair) other;
                return 
                ((  this.first == otherPair.first ||
                        ( this.first != null && otherPair.first != null &&
                          this.first.equals(otherPair.first))) &&
                 (      this.second == otherPair.second ||
                        ( this.second != null && otherPair.second != null &&
                          this.second.equals(otherPair.second))) );
        }

        return false;
    }

    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B second) {
        this.second = second;
    }
}

