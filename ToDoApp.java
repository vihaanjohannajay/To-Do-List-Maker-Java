import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TodoApp.java
 * Single-file Swing To-Do List maker with save/load and CSV export.
 *
 * Compile: javac TodoApp.java
 * Run:     java TodoApp
 */
public class TodoApp
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(TodoUI::new);
    }
}

/* -------------------------
   Data model: Task
   ------------------------- */
class Task implements Serializable
{
    private static final long serialVersionUID = 1L;

    enum Priority
    {
        LOW, MEDIUM, HIGH
    }

    private final UUID id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private boolean completed;

    public Task(String title, String description, LocalDate dueDate, Priority priority)
    {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = (priority == null ? Priority.MEDIUM : priority);
        this.completed = false;
    }

    public UUID getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String t)
    {
        title = t;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String d)
    {
        description = d;
    }

    public LocalDate getDueDate()
    {
        return dueDate;
    }

    public void setDueDate(LocalDate d)
    {
        dueDate = d;
    }

    public Priority getPriority()
    {
        return priority;
    }

    public void setPriority(Priority p)
    {
        priority = p;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    public void setCompleted(boolean c)
    {
        completed = c;
    }

    @Override
    public String toString()
    {
        return title;
    }
}

/* -------------------------
   UI: TodoUI
   ------------------------- */
class TodoUI extends JFrame
{
    private final DefaultListModel<Task> model = new DefaultListModel<>();
    private final JList<Task> list = new JList<>(model);
    private final JTextField searchField = new JTextField(16);
    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{"All", "Active", "Completed"});
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;

    private List<Task> allTasks = new ArrayList<>();

    public TodoUI()
    {
        super("To-Do List Maker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 520);
        setLocationRelativeTo(null);

        configureList();
        add(createTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        setupKeyboardShortcuts();
        setVisible(true);

        addSampleTasks();
    }

    private void configureList()
    {
        list.setCellRenderer(new TaskCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    editSelectedTask();
                }
            }
        });
    }

    private JPanel createTopPanel()
    {
        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton toggleBtn = new JButton("Toggle Complete");

        addBtn.addActionListener(e -> addTaskDialog());
        editBtn.addActionListener(e -> editSelectedTask());
        delBtn.addActionListener(e -> deleteSelectedTask());
        toggleBtn.addActionListener(e -> toggleSelectedTask());

        left.add(addBtn);
        left.add(editBtn);
        left.add(delBtn);
        left.add(toggleBtn);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        searchField.setToolTipText("Search title or description");
        searchField.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent e)
            {
                applyFilterAndSearch();
            }
        });
        filterCombo.addActionListener(e -> applyFilterAndSearch());

        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        JButton csvBtn = new JButton("Export CSV");

        saveBtn.addActionListener(e -> saveTasks());
        loadBtn.addActionListener(e -> loadTasks());
        csvBtn.addActionListener(e -> exportCSV());

        right.add(new JLabel("Search:"));
        right.add(searchField);
        right.add(new JLabel("Filter:"));
        right.add(filterCombo);
        right.add(saveBtn);
        right.add(loadBtn);
        right.add(csvBtn);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel createBottomPanel()
    {
        JPanel bottom = new JPanel(new BorderLayout());
        JLabel hint = new JLabel("Double-click a task to edit. Select + Enter to toggle complete.");
        hint.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bottom.add(hint, BorderLayout.WEST);
        return bottom;
    }

    private void setupKeyboardShortcuts()
    {
        list.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "toggle");
        list.getActionMap().put("toggle", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                toggleSelectedTask();
            }
        });

        list.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DELETE"), "delete");
        list.getActionMap().put("delete", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteSelectedTask();
            }
        });
    }

    private void addTaskDialog()
    {
        TaskForm form = new TaskForm(this, "Add Task", null);
        Task created = form.showDialog();
        if (created != null)
        {
            allTasks.add(created);
            refreshModel();
            list.setSelectedValue(created, true);
        }
    }

    private void editSelectedTask()
    {
        Task sel = list.getSelectedValue();
        if (sel == null)
        {
            JOptionPane.showMessageDialog(this, "Select a task to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TaskForm form = new TaskForm(this, "Edit Task", sel);
        Task updated = form.showDialog();
        if (updated != null)
        {
            sel.setTitle(updated.getTitle());
            sel.setDescription(updated.getDescription());
            sel.setDueDate(updated.getDueDate());
            sel.setPriority(updated.getPriority());
            sel.setCompleted(updated.isCompleted());
            refreshModel();
        }
    }

    private void deleteSelectedTask()
    {
        Task sel = list.getSelectedValue();
        if (sel == null)
        {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete selected task?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION)
        {
            allTasks.removeIf(t -> t.getId().equals(sel.getId()));
            refreshModel();
        }
    }

    private void toggleSelectedTask()
    {
        Task sel = list.getSelectedValue();
        if (sel == null)
        {
            return;
        }
        sel.setCompleted(!sel.isCompleted());
        refreshModel();
    }

    private void saveTasks()
    {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("ToDo files (*.todo)", "todo"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".todo"))
        {
            f = new File(f.getAbsolutePath() + ".todo");
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f)))
        {
            oos.writeObject(allTasks);
            JOptionPane.showMessageDialog(this, "Saved to " + f.getName());
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTasks()
    {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("ToDo files (*.todo)", "todo"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        File f = fc.getSelectedFile();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f)))
        {
            Object obj = ois.readObject();
            if (obj instanceof List)
            {
                allTasks = new ArrayList<>((List<Task>) obj);
                refreshModel();
                JOptionPane.showMessageDialog(this, "Loaded " + allTasks.size() + " tasks from " + f.getName());
            }
            else
            {
                throw new IOException("Unexpected file format");
            }
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCSV()
    {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv"))
        {
            f = new File(f.getAbsolutePath() + ".csv");
        }
        try (PrintWriter pw = new PrintWriter(f))
        {
            pw.println("Title,Description,DueDate,Priority,Completed");
            for (Task t : allTasks)
            {
                String due = t.getDueDate() == null ? "" : t.getDueDate().format(dateFmt);
                pw.printf("\"%s\",\"%s\",%s,%s,%s%n",
                        escapeForCSV(t.getTitle()),
                        escapeForCSV(t.getDescription()),
                        due, t.getPriority(), t.isCompleted());
            }
            JOptionPane.showMessageDialog(this, "Exported CSV to " + f.getName());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeForCSV(String s)
    {
        if (s == null)
        {
            return "";
        }
        return s.replace("\"", "\"\"");
    }

    private void addSampleTasks()
    {
        allTasks.add(new Task("Buy groceries", "Milk, bread, eggs", null, Task.Priority.MEDIUM));
        allTasks.add(new Task("Finish project", "Push final changes to repo", LocalDate.now().plusDays(2), Task.Priority.HIGH));
        allTasks.add(new Task("Call mom", "Weekly check-in", LocalDate.now().plusDays(1), Task.Priority.LOW));
        refreshModel();
    }

    private void refreshModel()
    {
        String query = searchField.getText().trim().toLowerCase();
        String filter = (String) filterCombo.getSelectedItem();

        List<Task> view = allTasks.stream()
                .filter(t ->
                {
                    boolean matchesFilter = "All".equals(filter) ||
                            ("Active".equals(filter) && !t.isCompleted()) ||
                            ("Completed".equals(filter) && t.isCompleted());
                    boolean matchesQuery = query.isEmpty() ||
                            t.getTitle().toLowerCase().contains(query) ||
                            (t.getDescription() != null && t.getDescription().toLowerCase().contains(query));
                    return matchesFilter && matchesQuery;
                })
                .sorted(Comparator.comparing(Task::isCompleted)
                        .thenComparing((Task t) -> t.getPriority(), Comparator.reverseOrder())
                        .thenComparing(t -> Optional.ofNullable(t.getDueDate()).orElse(LocalDate.MAX)))
                .collect(Collectors.toList());

        model.clear();
        for (Task t : view)
        {
            model.addElement(t);
        }
    }

    private void applyFilterAndSearch()
    {
        refreshModel();
    }
}

/* -------------------------
   Cell renderer for tasks
   ------------------------- */
class TaskCellRenderer extends JPanel implements ListCellRenderer<Task>
{
    private final JLabel titleLbl = new JLabel();
    private final JLabel metaLbl = new JLabel();

    public TaskCellRenderer()
    {
        setLayout(new BorderLayout(6, 2));
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 13f));
        metaLbl.setFont(metaLbl.getFont().deriveFont(Font.PLAIN, 11f));
        add(titleLbl, BorderLayout.CENTER);
        add(metaLbl, BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Task> list, Task value, int index,
                                                  boolean isSelected, boolean cellHasFocus)
    {
        titleLbl.setText(value.getTitle());
        String due = value.getDueDate() == null ? "" : "Due: " + value.getDueDate().toString();
        String prio = value.getPriority().name();
        metaLbl.setText(prio + (due.isEmpty() ? "" : " • " + due));

        if (value.isCompleted())
        {
            titleLbl.setForeground(Color.GRAY);
            metaLbl.setForeground(Color.GRAY);
            titleLbl.setText("<html><strike>" + escapeHtml(value.getTitle()) + "</strike></html>");
        }
        else
        {
            titleLbl.setForeground(Color.BLACK);
            metaLbl.setForeground(new Color(60, 60, 60));
        }

        setBackground(isSelected ? new Color(220, 235, 255) : Color.WHITE);
        setOpaque(true);
        return this;
    }

    private String escapeHtml(String s)
    {
        if (s == null)
        {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

/* -------------------------
   Dialog: TaskForm (Add/Edit)
   ------------------------- */
class TaskForm extends JDialog
{
    private final JTextField titleField = new JTextField(24);
    private final JTextField dueField = new JTextField(10);
    private final JComboBox<Task.Priority> prioCombo = new JComboBox<>(Task.Priority.values());
    private final JTextArea descArea = new JTextArea(5, 24);
    private final JCheckBox completedBox = new JCheckBox("Completed");

    private Task result = null;
    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

    public TaskForm(Frame owner, String title, Task existing)
    {
        super(owner, title, true);
        setLayout(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        fields.add(new JLabel("Title:"), c);
        c.gridx = 1;
        fields.add(titleField, c);

        c.gridx = 0;
        c.gridy = 1;
        fields.add(new JLabel("Due (yyyy-MM-dd):"), c);
        c.gridx = 1;
        fields.add(dueField, c);

        c.gridx = 0;
        c.gridy = 2;
        fields.add(new JLabel("Priority:"), c);
        c.gridx = 1;
        fields.add(prioCombo, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        fields.add(new JLabel("Description:"), c);

        c.gridy = 4;
        JScrollPane sp = new JScrollPane(descArea);
        fields.add(sp, c);

        c.gridy = 5;
        fields.add(completedBox, c);

        add(fields, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        if (existing != null)
        {
            populateFrom(existing);
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel createButtonPanel()
    {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> onOk());
        cancel.addActionListener(e -> onCancel());
        p.add(cancel);
        p.add(ok);
        getRootPane().setDefaultButton(ok);
        return p;
    }

    private void populateFrom(Task t)
    {
        titleField.setText(t.getTitle());
        descArea.setText(t.getDescription());
        prioCombo.setSelectedItem(t.getPriority());
        dueField.setText(t.getDueDate() == null ? "" : t.getDueDate().toString());
        completedBox.setSelected(t.isCompleted());
    }

    private void onOk()
    {
        String title = titleField.getText().trim();
        if (title.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String desc = descArea.getText().trim();
        LocalDate due = null;
        String dueTxt = dueField.getText().trim();
        if (!dueTxt.isEmpty())
        {
            try
            {
                due = LocalDate.parse(dueTxt, fmt);
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        Task.Priority prio = (Task.Priority) prioCombo.getSelectedItem();
        result = new Task(title, desc, due, prio);
        result.setCompleted(completedBox.isSelected());
        dispose();
    }

    private void onCancel()
    {
        result = null;
        dispose();
    }

    public Task showDialog()
    {
        setVisible(true);
        return result;
    }
}
