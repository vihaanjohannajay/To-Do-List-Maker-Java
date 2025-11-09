## To-Do List Maker (Java Swing Project)

This project is a desktop task manager built with **Java Swing**, offering a simple yet powerful way to organize and track daily tasks. It showcases clean GUI design, object-oriented structure, and file handling in one self-contained application.

### Overview

The program lets users create, manage, and track tasks with features like:

* Adding, editing, and deleting tasks
* Setting priority levels and due dates
* Filtering and searching tasks by status or keyword
* Saving and loading data for session persistence
* Exporting tasks to CSV for external use

It serves as a practical demonstration of **Java Swing UI design**, **serialization**, and **file I/O** while maintaining a clean and structured object-oriented approach.

### Core Components

* **`Task`** – Represents a single to-do item, containing details like title, description, priority, due date, and completion status.
* **`TaskList`** – Manages the collection of tasks and supports filtering, searching, and serialization.
* **`TodoApp`** – The main GUI class that integrates all components, handling user interactions and display logic through Swing elements like `JList`, `JButton`, and `JDialog`.

### Features

* **Interactive Task Management:** Add, edit, delete, and toggle task completion directly from the GUI.
* **Priority & Deadlines:** Assign each task a priority level (Low, Medium, High) and optional due date.
* **Filtering & Search:** Quickly filter between All, Active, and Completed tasks or search by title/description.
* **Persistent Storage:** Save tasks to `.todo` files and reload them between sessions.
* **CSV Export:** Export all tasks to a readable `.csv` file.
* **Keyboard Shortcuts:**

  * **Enter** — Toggle completion
  * **Delete** — Remove task
  * **Double-Click** — Edit task

### Tech Highlights

* Built entirely with **Java Swing** — no external libraries required.
* Demonstrates **encapsulation**, **modularity**, and **MVC-inspired design**.
* Uses **custom renderers** and **dialogs** for smooth and responsive UI behavior.
* Structured for readability, making it an ideal reference for Java GUI projects.

### How to Run

```bash
javac TodoApp.java
java TodoApp
```

### Why This Project

This application is designed to show how everyday productivity tools can be implemented using **pure Java**, emphasizing readability, modularity, and interactive UI development. It’s a strong example for students or developers learning **Swing**, **event handling**, and **state persistence** — or anyone building their first full desktop app in Java.
