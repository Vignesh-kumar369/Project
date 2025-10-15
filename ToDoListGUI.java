import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    String description;
    boolean isComplete;

    Task(String description) {
        this.description = description;
        this.isComplete = false;
    }

    void toggleComplete() {
        isComplete = !isComplete;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return (isComplete ? "✅ " : "⬜ ") + description;
    }
}

class TaskListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Task) {
            Task task = (Task) value;
            setText(task.getDescription());

            if (task.isComplete) {
                Font font = getFont();
                setFont(new Font(font.getName(), Font.ITALIC, font.getSize()));
                setForeground(Color.GRAY);
                setText("<html><s>" + task.getDescription() + "</s></html>");
            } else {
                setFont(list.getFont());
                setForeground(list.getForeground());
            }
        }
        return this;
    }
}

public class ToDoListGUI extends JFrame {
    private ArrayList<Task> tasks;
    private DefaultListModel<Task> listModel;
    private JList<Task> taskJList;
    private JTextField taskInput;
    private JButton addButton, toggleButton, removeButton, editButton, saveButton, loadButton;

    public ToDoListGUI() {
        tasks = new ArrayList<>();
        listModel = new DefaultListModel<>();

        setTitle("To-Do List Manager");
        setSize(750, 430); // slightly wider for save/load buttons
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        taskInput = new JTextField();
        taskInput.setFont(new Font("SansSerif", Font.PLAIN, 14));
        addButton = new JButton("Add Task");
        addButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        // Task list
        taskJList = new JList<>(listModel);
        taskJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskJList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        taskJList.setCellRenderer(new TaskListCellRenderer());
        JScrollPane scrollPane = new JScrollPane(taskJList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Control panel with buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        toggleButton = new JButton("Toggle Complete");
        toggleButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        removeButton = new JButton("Remove Task");
        removeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        editButton = new JButton("Edit Task");
        editButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveButton = new JButton("Save Tasks");
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        loadButton = new JButton("Load Tasks");
        loadButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        controlPanel.add(addButton);
        controlPanel.add(toggleButton);
        controlPanel.add(editButton);
        controlPanel.add(removeButton);
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);

        // Layout
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(inputPanel, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(controlPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addTask());
        toggleButton.addActionListener(e -> toggleSelectedTaskCompletion());
        editButton.addActionListener(e -> editSelectedTask());
        removeButton.addActionListener(e -> removeSelectedTask());
        saveButton.addActionListener(e -> saveTasksToFile());
        loadButton.addActionListener(e -> loadTasksFromFile());

        taskInput.addActionListener(e -> addTask());

        taskJList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    removeSelectedTask();
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    toggleSelectedTaskCompletion();
                }
            }
        });

        updateButtonStates();
        taskJList.addListSelectionListener(e -> updateButtonStates());

        setVisible(true);
    }

    private void addTask() {
        String desc = taskInput.getText().trim();
        if (!desc.isEmpty()) {
            Task newTask = new Task(desc);
            tasks.add(newTask);
            listModel.addElement(newTask);
            taskInput.setText("");
            updateButtonStates();
        }
    }

    private void toggleSelectedTaskCompletion() {
        int index = taskJList.getSelectedIndex();
        if (index != -1) {
            Task selectedTask = listModel.get(index);
            selectedTask.toggleComplete();
            listModel.set(index, selectedTask);
        }
    }

    private void editSelectedTask() {
        int index = taskJList.getSelectedIndex();
        if (index != -1) {
            Task selectedTask = listModel.get(index);
            String currentDescription = selectedTask.getDescription();

            String newDescription = (String) JOptionPane.showInputDialog(
                    this,
                    "Edit task description:",
                    "Edit Task",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    currentDescription);

            if (newDescription != null && !newDescription.trim().isEmpty() && !newDescription.trim().equals(currentDescription)) {
                selectedTask.setDescription(newDescription.trim());
                listModel.set(index, selectedTask);
            }
        }
    }

    private void removeSelectedTask() {
        int index = taskJList.getSelectedIndex();
        if (index != -1) {
            Task taskToRemove = listModel.remove(index);
            tasks.remove(taskToRemove);
            updateButtonStates();
        }
    }

    private void saveTasksToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Tasks");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chooser.getSelectedFile()))) {
                oos.writeObject(tasks);
                JOptionPane.showMessageDialog(this, "Tasks saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving tasks:\n" + ex.getMessage());
            }
        }
    }

    private void loadTasksFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Tasks");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()))) {
                Object obj = ois.readObject();
                if (obj instanceof ArrayList) {
                    ArrayList<?> loaded = (ArrayList<?>) obj;
                    tasks.clear();
                    listModel.clear();
                    for (Object o : loaded) {
                        if (o instanceof Task) {
                            Task t = (Task) o;
                            tasks.add(t);
                            listModel.addElement(t);
                        }
                    }
                    updateButtonStates();
                    JOptionPane.showMessageDialog(this, "Tasks loaded successfully!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading tasks:\n" + ex.getMessage());
            }
        }
    }

    private void updateButtonStates() {
        boolean taskSelected = !taskJList.isSelectionEmpty();
        toggleButton.setEnabled(taskSelected);
        editButton.setEnabled(taskSelected);
        removeButton.setEnabled(taskSelected);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListGUI::new);
    }
}
