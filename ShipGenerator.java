import java.util.*;

public class ShipGenerator {
    private int[][] ship; //represents our ship (2-Dimension matrix)
    private int rows, cols; //rows and column numbers of our ship
    private Random random = new Random(); //random number generator

    //ShipGenerator constructor takes in the dimensions of the ship when first creating the ship
    public ShipGenerator(int rows, int cols, int k){
        this.rows = rows; //retrieve # rows for our ship
        this.cols = cols; //retrieve # rows for our ship
        this.ship = new int[rows][cols]; //initialize the ship with our passed in dimensions
        generateShip(); //call generateShip to generate our pathways
        loosenShipDeadEnds((findDeadEnds().size() / 2) - 1); //losen about ((1/2) - 1) deadends from our ship to allow cycles/loops and more leeway to be created
        initializeBotAndLeakPosition(k); //initialize the random positions for the bot and leak (makes sure they do not overlap)
    }

    public int[][] getShip() { //getter method to return generated ship
        return ship;
    }

    public int getRows() {  //getter method to return number of rows in ship
        return rows;
    }
    
    public int getCols() {  //getter method to return number of cols in ship
        return cols;
    }

    private void initializeBotAndLeakPosition(int k) {
        int[] initialBotPosition;
        int[] initialLeakPosition;

        do {
            //set our initial bot and leak positions as random coordinates
            initialBotPosition = randomOpenBotPosition(); 
            initialLeakPosition =  randomOpenLeakPosition(initialBotPosition[0], initialBotPosition[1], k); //generates and retrieves a random leak position outisde of the initial detection square of the bot
            
        } while (Arrays.equals(initialBotPosition, initialLeakPosition));  //this while loop makes sure that the two positions are generated repeatedly until they do not overlap
     
    }

    public int[] getKeyPosition(int[][] ship, int value) { //this method returns the position in the ship associated with that 'key' value
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (ship[row][col] == value) {
                    return new int[]{row, col};
                }
            }
        }
        return null; //null is only returned if there is no bot or leak (or if value is not 2,3)
    }

    private void initializeShip(){//initializes the entire ship to start (all ship cells are blocked to start, thus they are set to equal 1)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                ship[i][j] = 1;
            }
        }
    }

    private void generateShip() {
        //initialize all cells as blocked 
        initializeShip();

        int initialRow = random.nextInt(rows); //generate a random row coordinate
        int initialCol = random.nextInt(cols); //generate a random cols coordinate
        ship[initialRow][initialCol] = 0; //open this random position (this is our bot's initial position)

        // Create a list to store cells within our ship that have exactly one neighbor
        List<int[]> cellsWithOneOpenNeighbor = new ArrayList<>();
        cellsWithOneOpenNeighbor.add(new int[]{initialRow - 1, initialCol});
        cellsWithOneOpenNeighbor.add(new int[]{initialRow + 1, initialCol});
        cellsWithOneOpenNeighbor.add(new int[]{initialRow, initialCol - 1});
        cellsWithOneOpenNeighbor.add(new int[]{initialRow, initialCol + 1});


        //this loop will run until there are no more cells in the ship with exactly one open neighbor
        while (!cellsWithOneOpenNeighbor.isEmpty()) {
            //choose a random cell from the 'cellsWithOneOpenNeighbor' list
            int randIndex = random.nextInt(cellsWithOneOpenNeighbor.size());

            //set a currentCell coordinate equal to the random cell coordinate picked from the list
            int[] currentCell = cellsWithOneOpenNeighbor.remove(randIndex); 

            //obtain the current row coordinate and current column coordinate seperately
            int row = currentCell[0];
            int col = currentCell[1];

            //check if the cell is a coordinate within the ship to avoid Array Out of Bounds error
            if (!inShip(row, col)) {
                continue; //ignore any coordinates outside the ship and continue
            }


            // Count the number of open (set to '0') neighbors that the currentCell has 
            int openNeighbors = 0;
            if (inShip(row - 1, col) && ship[row - 1][col] == 0) openNeighbors++;
            if (inShip(row + 1, col) && ship[row + 1][col] == 0) openNeighbors++;
            if (inShip(row, col - 1) && ship[row][col - 1] == 0) openNeighbors++;
            if (inShip(row, col + 1) && ship[row][col + 1] == 0) openNeighbors++;

            if (openNeighbors == 1) { //if the number of open neighbors a cell has is 1, we can open this cell in our ship
                ship[row][col] = 0;

            //add neighbors of the current cell to the 'cellsWithOneOpenNeighbor' list and repeat process
                cellsWithOneOpenNeighbor.add(new int[]{row - 1, col});
                cellsWithOneOpenNeighbor.add(new int[]{row + 1, col});
                cellsWithOneOpenNeighbor.add(new int[]{row, col - 1});
                cellsWithOneOpenNeighbor.add(new int[]{row, col + 1});
            }
        }
    }

    public boolean inShip(int row, int col) { //checks whether the passed in row and column coordinate are within the ship bounds
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public void printship() { //prints out the generated ship
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(ship[i][j] + "  ");
            }
            System.out.println();
        }
    }
    
    private int[] randomOpenBotPosition() { //generates a random open cell coordinate within the ship 
        int row, col;
        do {
            row = random.nextInt(rows); //get random row coordinate
            col = random.nextInt(cols); //get random col coordinate
        } while (ship[row][col] != 0); //do while here ensures that new coordinates are generated until they are open cells

        ship[row][col] = 2; //set this randomly generated open position to 2, to represent its our initial bot position
        return new int[]{row, col};
    }

    private int[] randomOpenLeakPosition(int botRow, int botCol, int k) { //generates a random open leak cell that is outside the detection square of the initial bot position
        int row, col;
        do {
            row = random.nextInt(rows); //get random row coordinate
            col = random.nextInt(cols); //get random col coordinate
        } while (withinDetectionSquare(row, col, botRow, botCol, k) || ship[row][col] != 0); //do while ensures that new coordinates are generated until they are an open cell outside of the detection square
    
        ship[row][col] = 3;  // set this randomly generated open position to 3, to represent it's our leak position
        return new int[]{row, col};
    }

    private boolean withinDetectionSquare(int row, int col, int botRow, int botCol, int k) { //checks wether or not a (row,col) coordinate is in the 'detection square' of position (botRow, botCol)
        
        //store the row and column boundaries of the square in 4 seperate x/y values
        int y1Square = Math.max(0, botRow - k);
        int y2Square = Math.min(rows - 1, botRow + k);
        int x1Square = Math.max(0, botCol - k);
        int x2Square = Math.min(cols - 1, botCol + k);
    
        return row >= y1Square && row <= y2Square && col >= x1Square && col <= x2Square; //returns true if position is within the detection square and false otherwise
    }

    private boolean isShipCellInterior(int row, int col) { //this method checks whether the passed in coordinate is within the interior the ship (not an edge coordinate)
        return inShip(row - 1, col) && inShip(row + 1, col) && inShip(row, col - 1) && inShip(row, col + 1);
    }

    private boolean hasExactlyOneOpenNeighbor(int row, int col) { //checks if a cell has EXACTLY 1 neighbor
        int openNeighborCount = 0;
        if (inShip(row - 1, col) && ship[row - 1][col] == 0) openNeighborCount++;
        if (inShip(row + 1, col) && ship[row + 1][col] == 0) openNeighborCount++;
        if (inShip(row, col - 1) && ship[row][col - 1] == 0) openNeighborCount++;
        if (inShip(row, col + 1) && ship[row][col + 1] == 0) openNeighborCount++;
        return openNeighborCount == 1;
    }

    private boolean hasExactlyThreeBlockedNeighbors(int row, int col) { //checks if a cell has EXACTLY 3 blocked neighbors
        int blockedNeighborCount = 0;
        if (inShip(row - 1, col) && ship[row - 1][col] == 1) blockedNeighborCount++;
        if (inShip(row + 1, col) && ship[row + 1][col] == 1) blockedNeighborCount++;
        if (inShip(row, col - 1) && ship[row][col - 1] == 1) blockedNeighborCount++;
        if (inShip(row, col + 1) && ship[row][col + 1] == 1) blockedNeighborCount++;
        return blockedNeighborCount == 3;
    }

    private List<int[]> findDeadEnds() { //this method returns a list of dead-end positions within our generated ship
        List<int[]> deadEndPositions = new ArrayList<>();
    
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (ship[i][j] == 0 && isShipCellInterior(i, j) && hasExactlyThreeBlockedNeighbors(i, j) && hasExactlyOneOpenNeighbor(i, j)) {
                    // Check if the current cell meets a 'dead-end' cell criteria, which is listed below
                    // - It must be an open cell (ship[i][j] == 0).
                    // - It must be an interior cell (not an edge cell) with 4 neighbors that are not out of bounds
                    // - 3 of the 4 neighbors must be blocked 
                    // - 1 of the 4 neighbors must be open
                    deadEndPositions.add(new int[]{i, j}); //if a position meets this criteria, we add it to the deadEndPositions list
                }
            }
        }
    
        return deadEndPositions; //return the deadEndsPosition list
    }

    private void loosenShipDeadEnds(int deadEndNum) { //deadEndNum is the number of deadends we pass in to be modified (it is right now set to 1/2 - 5 dead ends which is just below a half)
        List<int[]> deadEndPositions = findDeadEnds(); //retrieves all the deadend positions in the ship
    
        int numModified = 0;
        while (numModified < deadEndNum && !deadEndPositions.isEmpty()) { //continues while the 'deadEndPositions' list is not empty AND until we have modified 'deadEndNum' dead ends
            int randIndex = random.nextInt(deadEndPositions.size()); //generate a random number in the 'deadEndPositions' list
            int[] position = deadEndPositions.get(randIndex);  // Randomly select a specific dead-end position from the list
    
            // Check if the selected dead-end cell has exactly three blocked neighbors
            if (hasExactlyThreeBlockedNeighbors(position[0], position[1])) {
                // Initialize a list to store the blocked neighbors
                List<int[]> blockedNeighbors = new ArrayList<>();
    
                // Check each of the four possible neighbors of the selected dead-end cell
                if (inShip(position[0] - 1, position[1]) && ship[position[0] - 1][position[1]] == 1) {
                    blockedNeighbors.add(new int[]{position[0] - 1, position[1]});
                }
                if (inShip(position[0] + 1, position[1]) && ship[position[0] + 1][position[1]] == 1) {
                    blockedNeighbors.add(new int[]{position[0] + 1, position[1]});
                }
                if (inShip(position[0], position[1] - 1) && ship[position[0]][position[1] - 1] == 1) {
                    blockedNeighbors.add(new int[]{position[0], position[1] - 1});
                }
                if (inShip(position[0], position[1] + 1) && ship[position[0]][position[1] + 1] == 1) {
                    blockedNeighbors.add(new int[]{position[0], position[1] + 1});
                }
    
                // If there are blocked neighbors, randomly choose one to open
                if (!blockedNeighbors.isEmpty()) {
                    int randNeighborIndex = random.nextInt(blockedNeighbors.size());
                    int[] neighbor = blockedNeighbors.get(randNeighborIndex);
                    ship[neighbor[0]][neighbor[1]] = 0; // Open the selected blocked neighbor
                    numModified++; // Increment the count of modified dead-end cells
    
                    // Print the chosen dead-end cell position and the modified neighbor (FOR DEBUGGING)
                    // System.out.println("Chosen Dead-End Cell: (" + position[0] + ", " + position[1] + ")");
                    // System.out.println("Modified Neighbor: (" + neighbor[0] + ", " + neighbor[1] + ")");
                }
            }
            // Remove the current processed dead-end position from the list and continue next iteration
            deadEndPositions.remove(randIndex);
        } 
    }
    
    public static void main(String[] args) {
        //generates a 50x50 ship, with completed pathways and dead-end loosening already done, and then prints the ship
        //makes sure the initial leak spot is outside the detection square of size (2k+1)^2 of the initial bot position, in this case k is set to 3, so our initial leak position would be outside the 7x7 square from our initial bot position
        new ShipGenerator(50, 50, 3).printship(); 
    }
}