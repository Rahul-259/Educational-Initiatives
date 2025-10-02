// Main.java - Entry point
import java.util.*;

public class Main {
    public static void main(String[] args) {
        OfficeManager officeManager = OfficeManager.getInstance();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Smart Office Facility Management ===\n");
        
        while (true) {
            System.out.println("\n1. Book Room");
            System.out.println("2. Cancel Booking");
            System.out.println("3. View Room Status");
            System.out.println("4. Add Room");
            System.out.println("5. Execute Device Commands");
            System.out.println("6. Set Room Configuration");
            System.out.println("7. View All Bookings");
            System.out.println("8. Exit");
            System.out.print("Choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            try {
                switch (choice) {
                    case 1:
                        bookRoom(scanner, officeManager);
                        break;
                    case 2:
                        cancelBooking(scanner, officeManager);
                        break;
                    case 3:
                        viewRoomStatus(scanner, officeManager);
                        break;
                    case 4:
                        addRoom(scanner, officeManager);
                        break;
                    case 5:
                        executeCommands(scanner, officeManager);
                        break;
                    case 6:
                        setConfiguration(scanner, officeManager);
                        break;
                    case 7:
                        officeManager.viewAllBookings();
                        break;
                    case 8:
                        System.out.println("Exiting...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
    private static void bookRoom(Scanner scanner, OfficeManager manager) {
        System.out.print("Room name: ");
        String room = scanner.nextLine();
        System.out.print("Start time (HH:MM): ");
        String start = scanner.nextLine();
        System.out.print("Duration (minutes): ");
        int duration = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Attendees: ");
        int attendees = scanner.nextInt();
        scanner.nextLine();
        
        manager.bookRoom(room, start, duration, attendees);
    }
    
    private static void cancelBooking(Scanner scanner, OfficeManager manager) {
        System.out.print("Room name: ");
        String room = scanner.nextLine();
        manager.cancelBooking(room);
    }
    
    private static void viewRoomStatus(Scanner scanner, OfficeManager manager) {
        System.out.print("Room name: ");
        String room = scanner.nextLine();
        manager.viewRoomStatus(room);
    }
    
    private static void addRoom(Scanner scanner, OfficeManager manager) {
        System.out.print("Room name: ");
        String name = scanner.nextLine();
        System.out.print("Room type (Conference/Meeting): ");
        String type = scanner.nextLine();
        System.out.print("Capacity: ");
        int capacity = scanner.nextInt();
        scanner.nextLine();
        
        manager.addRoom(name, type, capacity);
    }
    
    private static void executeCommands(Scanner scanner, OfficeManager manager) {
        System.out.print("Room name: ");
        String room = scanner.nextLine();
        System.out.print("Commands (L=Lights, A=AC, P=Projector): ");
        String commands = scanner.nextLine().toUpperCase();
        
        manager.executeRoomCommands(room, commands);
    }
    
    private static void setConfiguration(Scanner scanner, OfficeManager manager) {
        System.out.print("Room name: ");
        String room = scanner.nextLine();
        System.out.print("Configuration (ON/OFF): ");
        String config = scanner.nextLine();
        
        manager.setRoomConfiguration(room, config);
    }
}

// Room.java - Room classes with Factory Pattern
abstract class Room {
    protected String name;
    protected int capacity;
    protected boolean occupied;
    protected List<Observer> observers;
    protected List<Device> devices;
    protected Booking currentBooking;
    
    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.occupied = false;
        this.observers = new ArrayList<>();
        this.devices = new ArrayList<>();
        
        // Add default observers
        observers.add(new BookingObserver());
        observers.add(new MaintenanceObserver());
        
        // Add default devices
        devices.add(new Lights());
        devices.add(new AirConditioner());
    }
    
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(name + ": " + message);
        }
    }
    
    public boolean book(String startTime, int duration, int attendees) {
        if (occupied) {
            notifyObservers("Booking failed - Room already occupied");
            return false;
        }
        
        if (attendees > capacity) {
            notifyObservers("Booking failed - Exceeds capacity (" + capacity + ")");
            return false;
        }
        
        currentBooking = new Booking(startTime, duration, attendees);
        occupied = true;
        notifyObservers("Room booked from " + startTime + " for " + duration + " minutes");
        return true;
    }
    
    public void cancelBooking() {
        if (!occupied) {
            notifyObservers("No booking to cancel");
            return;
        }
        
        occupied = false;
        currentBooking = null;
        notifyObservers("Booking cancelled");
    }
    
    public void executeCommand(Command command) {
        command.execute();
    }
    
    public abstract String getRoomType();
    
    public String getName() { return name; }
    public boolean isOccupied() { return occupied; }
    public Booking getCurrentBooking() { return currentBooking; }
    public List<Device> getDevices() { return devices; }
}

class ConferenceRoom extends Room {
    public ConferenceRoom(String name, int capacity) {
        super(name, capacity);
        devices.add(new Projector()); // Conference rooms have projectors
    }
    
    @Override
    public String getRoomType() {
        return "Conference Room";
    }
}

class MeetingRoom extends Room {
    public MeetingRoom(String name, int capacity) {
        super(name, capacity);
        devices.add(new Whiteboard());
    }
    
    @Override
    public String getRoomType() {
        return "Meeting Room";
    }
}

// RoomFactory.java - Factory Pattern
class RoomFactory {
    private static final Map<String, RoomCreator> creators = new HashMap<>();
    
    static {
        creators.put("CONFERENCE", (name, capacity) -> new ConferenceRoom(name, capacity));
        creators.put("MEETING", (name, capacity) -> new MeetingRoom(name, capacity));
    }
    
    public static Room createRoom(String type, String name, int capacity) {
        RoomCreator creator = creators.get(type.toUpperCase());
        if (creator == null) {
            throw new IllegalArgumentException("Invalid room type: " + type);
        }
        return creator.create(name, capacity);
    }
    
    @FunctionalInterface
    interface RoomCreator {
        Room create(String name, int capacity);
    }
}

// Booking.java - Booking model
class Booking {
    private String startTime;
    private int duration;
    private int attendees;
    private String status;
    
    public Booking(String startTime, int duration, int attendees) {
        this.startTime = startTime;
        this.duration = duration;
        this.attendees = attendees;
        this.status = "ACTIVE";
    }
    
    public String getStartTime() { return startTime; }
    public int getDuration() { return duration; }
    public int getAttendees() { return attendees; }
    public String getStatus() { return status; }
    
    public void cancel() { this.status = "CANCELLED"; }
    
    @Override
    public String toString() {
        return String.format("Start: %s, Duration: %d min, Attendees: %d, Status: %s",
            startTime, duration, attendees, status);
    }
}

// Device.java - Device classes for Command Pattern
abstract class Device {
    protected String name;
    protected boolean on;
    
    public Device(String name) {
        this.name = name;
        this.on = false;
    }
    
    public void turnOn() {
        on = true;
        System.out.println(name + " turned ON");
    }
    
    public void turnOff() {
        on = false;
        System.out.println(name + " turned OFF");
    }
    
    public String getName() { return name; }
    public boolean isOn() { return on; }
}

class Lights extends Device {
    public Lights() { super("Lights"); }
}

class AirConditioner extends Device {
    public AirConditioner() { super("Air Conditioner"); }
}

class Projector extends Device {
    public Projector() { super("Projector"); }
}

class Whiteboard extends Device {
    public Whiteboard() { super("Whiteboard"); }
}

// Command.java - Command Pattern
interface Command {
    void execute();
}

class LightsOnCommand implements Command {
    private Device device;
    
    public LightsOnCommand(Device device) { this.device = device; }
    
    @Override
    public void execute() {
        device.turnOn();
    }
}

class ACOnCommand implements Command {
    private Device device;
    
    public ACOnCommand(Device device) { this.device = device; }
    
    @Override
    public void execute() {
        device.turnOn();
    }
}

class ProjectorOnCommand implements Command {
    private Device device;
    
    public ProjectorOnCommand(Device device) { this.device = device; }
    
    @Override
    public void execute() {
        device.turnOn();
    }
}

// Observer.java - Observer Pattern
interface Observer {
    void update(String message);
}

class BookingObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("[BOOKING] " + message);
    }
}

class MaintenanceObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("[MAINTENANCE] " + message);
    }
}

// OfficeManager.java - Singleton Pattern
class OfficeManager {
    private static OfficeManager instance;
    private Map<String, Room> rooms;
    
    private OfficeManager() {
        rooms = new HashMap<>();
        // Initialize with default rooms
        rooms.put("CR1", RoomFactory.createRoom("Conference", "CR1", 10));
        rooms.put("MR1", RoomFactory.createRoom("Meeting", "MR1", 5));
    }
    
    public static synchronized OfficeManager getInstance() {
        if (instance == null) {
            instance = new OfficeManager();
        }
        return instance;
    }
    
    public void addRoom(String name, String type, int capacity) {
        rooms.put(name, RoomFactory.createRoom(type, name, capacity));
        System.out.println("Room added: " + name);
    }
    
    public void bookRoom(String roomName, String startTime, int duration, int attendees) {
        Room room = rooms.get(roomName);
        if (room == null) {
            System.out.println("Room not found: " + roomName);
            return;
        }
        
        boolean success = room.book(startTime, duration, attendees);
        if (success) {
            System.out.println("Booking successful!");
        }
    }
    
    public void cancelBooking(String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) {
            System.out.println("Room not found: " + roomName);
            return;
        }
        room.cancelBooking();
    }
    
    public void viewRoomStatus(String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) {
            System.out.println("Room not found: " + roomName);
            return;
        }
        
        System.out.println("\n=== Room Status ===");
        System.out.println("Name: " + room.getName());
        System.out.println("Type: " + room.getRoomType());
        System.out.println("Occupied: " + (room.isOccupied() ? "Yes" : "No"));
        
        if (room.getCurrentBooking() != null) {
            System.out.println("Current Booking: " + room.getCurrentBooking());
        }
        
        System.out.println("Devices:");
        for (Device device : room.getDevices()) {
            System.out.println("  - " + device.getName() + " (" + (device.isOn() ? "ON" : "OFF") + ")");
        }
    }
    
    public void executeRoomCommands(String roomName, String commands) {
        Room room = rooms.get(roomName);
        if (room == null) {
            System.out.println("Room not found: " + roomName);
            return;
        }
        
        Map<Character, CommandCreator> commandMap = new HashMap<>();
        
        for (Device device : room.getDevices()) {
            if (device instanceof Lights) {
                commandMap.put('L', () -> new LightsOnCommand(device));
            } else if (device instanceof AirConditioner) {
                commandMap.put('A', () -> new ACOnCommand(device));
            } else if (device instanceof Projector) {
                commandMap.put('P', () -> new ProjectorOnCommand(device));
            }
        }
        
        for (char c : commands.toCharArray()) {
            CommandCreator creator = commandMap.get(c);
            if (creator != null) {
                Command cmd = creator.create();
                room.executeCommand(cmd);
            } else {
                System.out.println("Unknown command: " + c);
            }
        }
    }
    
    public void setRoomConfiguration(String roomName, String config) {
        Room room = rooms.get(roomName);
        if (room == null) {
            System.out.println("Room not found: " + roomName);
            return;
        }
        
        boolean turnOn = config.equalsIgnoreCase("ON");
        
        for (Device device : room.getDevices()) {
            if (turnOn) {
                device.turnOn();
            } else {
                device.turnOff();
            }
        }
        
        System.out.println("All devices in " + roomName + " turned " + config);
    }
    
    public void viewAllBookings() {
        System.out.println("\n=== All Room Bookings ===");
        rooms.values().forEach(room -> {
            System.out.println("\n" + room.getName() + " (" + room.getRoomType() + "):");
            if (room.isOccupied() && room.getCurrentBooking() != null) {
                System.out.println("  " + room.getCurrentBooking());
            } else {
                System.out.println("  No active booking");
            }
        });
    }
    
    @FunctionalInterface
    interface CommandCreator {
        Command create();
    }
}