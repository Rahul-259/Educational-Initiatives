// Main.java - Entry point
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Rover rover = new Rover(0, 0, Direction.NORTH);
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Mars Rover Simulation ===");
        System.out.println("Commands: M (Move), L (Turn Left), R (Turn Right)");
        System.out.println("Type commands as a string (e.g., MMLMRM) or 'exit' to quit\n");
        
        while (true) {
            System.out.println("\nCurrent Position: " + rover.getPosition());
            System.out.print("Enter commands: ");
            String input = scanner.nextLine().toUpperCase();
            
            if (input.equals("EXIT")) {
                System.out.println("Exiting simulation...");
                break;
            }
            
            rover.executeCommands(input);
        }
        
        scanner.close();
    }
}

// Position.java - Value Object
class Position {
    private int x;
    private int y;
    private Direction direction;
    
    public Position(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getDirection() { return direction; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDirection(Direction direction) { this.direction = direction; }
    
    @Override
    public String toString() {
        return String.format("(%d, %d) facing %s", x, y, direction);
    }
}

// Direction.java - Enum
enum Direction {
    NORTH("N"), EAST("E"), SOUTH("S"), WEST("W");
    
    private String code;
    
    Direction(String code) {
        this.code = code;
    }
    
    public Direction turnLeft() {
        Map<Direction, Direction> leftTurns = Map.of(
            NORTH, WEST,
            WEST, SOUTH,
            SOUTH, EAST,
            EAST, NORTH
        );
        return leftTurns.get(this);
    }
    
    public Direction turnRight() {
        Map<Direction, Direction> rightTurns = Map.of(
            NORTH, EAST,
            EAST, SOUTH,
            SOUTH, WEST,
            WEST, NORTH
        );
        return rightTurns.get(this);
    }
    
    @Override
    public String toString() {
        return code;
    }
}

// Command.java - Command Pattern (no if-else)
interface Command {
    void execute(Rover rover);
}

class MoveCommand implements Command {
    @Override
    public void execute(Rover rover) {
        rover.move();
    }
}

class TurnLeftCommand implements Command {
    @Override
    public void execute(Rover rover) {
        rover.turnLeft();
    }
}

class TurnRightCommand implements Command {
    @Override
    public void execute(Rover rover) {
        rover.turnRight();
    }
}

// CommandFactory.java - Factory Pattern (no if-else using Map)
class CommandFactory {
    private static final Map<Character, Command> commandMap = new HashMap<>();
    
    static {
        commandMap.put('M', new MoveCommand());
        commandMap.put('L', new TurnLeftCommand());
        commandMap.put('R', new TurnRightCommand());
    }
    
    public static Command createCommand(char commandChar) {
        Command command = commandMap.get(commandChar);
        
        return Optional.ofNullable(command)
            .orElseThrow(() -> new IllegalArgumentException("Invalid command: " + commandChar));
    }
}

// Grid.java - Composite Pattern for obstacles
interface GridElement {
    boolean isObstacle(int x, int y);
}

class EmptyGrid implements GridElement {
    @Override
    public boolean isObstacle(int x, int y) {
        return false;
    }
}

class ObstacleGrid implements GridElement {
    private List<Obstacle> obstacles;
    
    public ObstacleGrid() {
        this.obstacles = new ArrayList<>();
        // Add some sample obstacles
        obstacles.add(new Obstacle(2, 2));
        obstacles.add(new Obstacle(3, 5));
    }
    
    @Override
    public boolean isObstacle(int x, int y) {
        return obstacles.stream().anyMatch(obs -> obs.isAt(x, y));
    }
    
    public void addObstacle(int x, int y) {
        obstacles.add(new Obstacle(x, y));
    }
}

class Obstacle {
    private int x;
    private int y;
    
    public Obstacle(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public boolean isAt(int x, int y) {
        return this.x == x && this.y == y;
    }
}

// Rover.java - Main Rover class
class Rover {
    private Position position;
    private GridElement grid;
    private List<Observer> observers;
    
    // Movement strategy map (no if-else)
    private Map<Direction, MoveStrategy> moveStrategies;
    
    public Rover(int x, int y, Direction direction) {
        this.position = new Position(x, y, direction);
        this.grid = new ObstacleGrid();
        this.observers = new ArrayList<>();
        
        // Initialize observers
        observers.add(new StatusObserver());
        
        // Initialize movement strategies
        initializeMoveStrategies();
    }
    
    private void initializeMoveStrategies() {
        moveStrategies = new HashMap<>();
        moveStrategies.put(Direction.NORTH, (pos) -> pos.setY(pos.getY() + 1));
        moveStrategies.put(Direction.SOUTH, (pos) -> pos.setY(pos.getY() - 1));
        moveStrategies.put(Direction.EAST, (pos) -> pos.setX(pos.getX() + 1));
        moveStrategies.put(Direction.WEST, (pos) -> pos.setX(pos.getX() - 1));
    }
    
    public void executeCommands(String commands) {
        for (char c : commands.toCharArray()) {
            try {
                Command command = CommandFactory.createCommand(c);
                command.execute(this);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    public void move() {
        int currentX = position.getX();
        int currentY = position.getY();

        // Use strategy pattern instead of if-else
        MoveStrategy strategy = moveStrategies.get(position.getDirection());
        Position tempPos = new Position(currentX, currentY, position.getDirection());
        strategy.move(tempPos);

        final int targetX = tempPos.getX();
        final int targetY = tempPos.getY();

        // Check for obstacles
        boolean hasObstacle = grid.isObstacle(targetX, targetY);

        String message = hasObstacle 
            ? "Obstacle detected at (" + targetX + ", " + targetY + "). Cannot move."
            : "Moved to (" + targetX + ", " + targetY + ")";

        notifyObservers(message);

        // Update position only if no obstacle
        if (!hasObstacle) {
            position.setX(targetX);
            position.setY(targetY);
        }
    }
    
    public void turnLeft() {
        position.setDirection(position.getDirection().turnLeft());
        notifyObservers("Turned left. Now facing " + position.getDirection());
    }
    
    public void turnRight() {
        position.setDirection(position.getDirection().turnRight());
        notifyObservers("Turned right. Now facing " + position.getDirection());
    }
    
    private void notifyObservers(String message) {
        observers.forEach(observer -> observer.update(message));
    }
    
    public Position getPosition() {
        return position;
    }
}

// MoveStrategy.java - Strategy Pattern (no if-else)
interface MoveStrategy {
    void move(Position position);
}

// Observer.java - Observer Pattern
interface Observer {
    void update(String message);
}

class StatusObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("[ROVER] " + message);
    }
}