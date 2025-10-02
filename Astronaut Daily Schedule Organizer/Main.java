// Main.java - Entry point
import java.util.*;

public class Main {
    public static void main(String[] args) {
        ScheduleManager manager = ScheduleManager.getInstance();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Astronaut Daily Schedule Organizer ===\n");
        
        while (true) {
            System.out.println("\n1. Add Task");
            System.out.println("2. Remove Task");
            System.out.println("3. View Tasks");
            System.out.println("4. Edit Task");
            System.out.println("5. Mark Task Complete");
            System.out.println("6. View Tasks by Priority");
            System.out.println("7. Exit");
            System.out.print("Choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            try {
                switch (choice) {
                    case 1:
                        addTask(scanner, manager);
                        break;
                    case 2:
                        removeTask(scanner, manager);
                        break;
                    case 3:
                        manager.viewTasks();
                        break;
                    case 4:
                        editTask(scanner, manager);
                        break;
                    case 5:
                        markComplete(scanner, manager);
                        break;
                    case 6:
                        viewByPriority(scanner, manager);
                        break;
                    case 7:
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
    
    private static void addTask(Scanner scanner, ScheduleManager manager) {
        System.out.print("Description: ");
        String desc = scanner.nextLine();
        System.out.print("Start Time (HH:MM): ");
        String start = scanner.nextLine();
        System.out.print("End Time (HH:MM): ");
        String end = scanner.nextLine();
        System.out.print("Priority (High/Medium/Low): ");
        String priority = scanner.nextLine();
        
        Task task = TaskFactory.createTask(desc, start, end, priority);
        manager.addTask(task);
    }
    
    private static void removeTask(Scanner scanner, ScheduleManager manager) {
        System.out.print("Task description to remove: ");
        String desc = scanner.nextLine();
        manager.removeTask(desc);
    }
    
    private static void editTask(Scanner scanner, ScheduleManager manager) {
        System.out.print("Task description to edit: ");
        String desc = scanner.nextLine();
        System.out.print("New Start Time (HH:MM): ");
        String start = scanner.nextLine();
        System.out.print("New End Time (HH:MM): ");
        String end = scanner.nextLine();
        manager.editTask(desc, start, end);
    }
    
    private static void markComplete(Scanner scanner, ScheduleManager manager) {
        System.out.print("Task description to mark complete: ");
        String desc = scanner.nextLine();
        manager.markTaskComplete(desc);
    }
    
    private static void viewByPriority(Scanner scanner, ScheduleManager manager) {
        System.out.print("Priority (High/Medium/Low): ");
        String priority = scanner.nextLine();
        manager.viewTasksByPriority(priority);
    }
}

// Task.java - Task model
class Task {
    private String description;
    private String startTime;
    private String endTime;
    private String priority;
    private boolean completed;
    private List<Observer> observers;
    
    public Task(String description, String startTime, String endTime, String priority) {
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.completed = false;
        this.observers = new ArrayList<>();
    }
    
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
    
    public boolean conflictsWith(Task other) {
        int thisStart = timeToMinutes(this.startTime);
        int thisEnd = timeToMinutes(this.endTime);
        int otherStart = timeToMinutes(other.startTime);
        int otherEnd = timeToMinutes(other.endTime);
        
        return (thisStart < otherEnd && thisEnd > otherStart);
    }
    
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
    
    public String getDescription() { return description; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return completed; }
    
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void markComplete() { 
        this.completed = true;
        notifyObservers("Task completed: " + description);
    }
    
    @Override
    public String toString() {
        String status = completed ? "[COMPLETED]" : "[PENDING]";
        return String.format("%s %s - %s: %s [%s]", 
            status, startTime, endTime, description, priority);
    }
}

// TaskFactory.java - Factory Pattern
class TaskFactory {
    public static Task createTask(String desc, String start, String end, String priority) {
        Task task = new Task(desc, start, end, priority);
        task.addObserver(new ConflictObserver());
        task.addObserver(new LogObserver());
        return task;
    }
}

// Observer.java - Observer Pattern
interface Observer {
    void update(String message);
}

class ConflictObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("[CONFLICT ALERT] " + message);
    }
}

class LogObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("[LOG] " + message);
    }
}

// ScheduleManager.java - Singleton Pattern
class ScheduleManager {
    private static ScheduleManager instance;
    private List<Task> tasks;
    
    private ScheduleManager() {
        tasks = new ArrayList<>();
    }
    
    public static synchronized ScheduleManager getInstance() {
        if (instance == null) {
            instance = new ScheduleManager();
        }
        return instance;
    }
    
    public void addTask(Task task) {
        for (Task existing : tasks) {
            if (task.conflictsWith(existing)) {
                task.notifyObservers("Task conflicts with: " + existing.getDescription());
                System.out.println("Error: Task conflicts with existing task.");
                return;
            }
        }
        tasks.add(task);
        task.notifyObservers("Task added successfully: " + task.getDescription());
        System.out.println("Task added successfully.");
    }
    
    public void removeTask(String description) {
        tasks.removeIf(t -> t.getDescription().equalsIgnoreCase(description));
        System.out.println("Task removed successfully.");
    }
    
    public void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks scheduled.");
            return;
        }
        
        tasks.sort(Comparator.comparing(Task::getStartTime));
        System.out.println("\n=== All Tasks ===");
        for (Task task : tasks) {
            System.out.println(task);
        }
    }
    
    public void editTask(String description, String newStart, String newEnd) {
        for (Task task : tasks) {
            if (task.getDescription().equalsIgnoreCase(description)) {
                task.setStartTime(newStart);
                task.setEndTime(newEnd);
                System.out.println("Task updated successfully.");
                return;
            }
        }
        System.out.println("Task not found.");
    }
    
    public void markTaskComplete(String description) {
        for (Task task : tasks) {
            if (task.getDescription().equalsIgnoreCase(description)) {
                task.markComplete();
                return;
            }
        }
        System.out.println("Task not found.");
    }
    
    public void viewTasksByPriority(String priority) {
        System.out.println("\n=== Tasks with Priority: " + priority + " ===");
        tasks.stream()
            .filter(t -> t.getPriority().equalsIgnoreCase(priority))
            .forEach(System.out::println);
    }
}