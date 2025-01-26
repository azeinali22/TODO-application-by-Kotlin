package org.example.amirhossein_zeinali_dehaghani_ass1

import javafx.application.Application
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.scene.layout.Region
import javafx.scene.layout.Priority
import javafx.util.Callback
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import javafx.scene.text.Text
import javafx.scene.paint.Color
import javafx.scene.Cursor
import javafx.scene.control.Tooltip

// Data class representing a Task
data class Task(val id: Int, var description: String, var isCompleted: Boolean = false)

// Database object managing database connections and table creation
object Database {
    private const val URL = "jdbc:sqlite:MyDB.db" // database filename

    init {
        try {
            DriverManager.getConnection(URL).use { connection ->
                connection.createStatement().use { statement ->
                    // Create the tasks table if it doesn't exist
                    val sql = """
                        CREATE TABLE IF NOT EXISTS tasks (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            description TEXT NOT NULL,
                            isCompleted BOOLEAN NOT NULL DEFAULT 0
                        )
                    """
                    statement.execute(sql)
                }
            }
            println("Database connected and initialized.")
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    // Provides the connection to the database
    val connection: Connection
        get() = DriverManager.getConnection(URL)
}

// TodoList class interacting with the database
class TodoList(private val connection: Connection) {
    fun addTask(description: String) {
        val sql = "INSERT INTO tasks(description) VALUES(?)"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, description)
            statement.executeUpdate()
        }
    }

    fun markTaskAsCompleted(id: Int) {
        val sql = "UPDATE tasks SET isCompleted = 1 WHERE id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    fun markTaskAsIncomplete(id: Int) {
        val sql = "UPDATE tasks SET isCompleted = 0 WHERE id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    fun deleteTask(id: Int) {
        val sql = "DELETE FROM tasks WHERE id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val sql = "SELECT * FROM tasks"
        connection.prepareStatement(sql).use { statement ->
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val task = Task(
                    id = resultSet.getInt("id"),
                    description = resultSet.getString("description"),
                    isCompleted = resultSet.getBoolean("isCompleted")
                )
                tasks.add(task)
            }
        }
        return tasks
    }
}

// Main application class
class MainApp : Application() {
    private val todoList = TodoList(Database.connection)
    private val tasksObservableList: ObservableList<Task> = FXCollections.observableArrayList()

    override fun start(primaryStage: Stage) {
        tasksObservableList.setAll(todoList.getAllTasks()) // Load tasks into the observable list

        // Create header with title and help button
        val headerLabel = Label("To-Do List with SQLite DB")
        headerLabel.style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"

        // Help button to display help dialog
        val helpButton = Button("Help")
        helpButton.cursor = Cursor.HAND
        helpButton.setOnAction { showHelpDialog() }

        // Layout for header
        val headerContainer = HBox(headerLabel, helpButton)
        headerContainer.alignment = Pos.CENTER
        headerContainer.style = "-fx-background-color: gray;"
        headerContainer.padding = Insets(10.0)
        headerContainer.spacing = 10.0

        // Input field for adding tasks
        val descriptionField = TextField()
        descriptionField.promptText = "Add a TODO"
        descriptionField.prefWidth = 300.0

        // Button for adding tasks
        val addButton = Button("+")
        addButton.cursor = Cursor.HAND
        Tooltip.install(addButton, Tooltip("Add new task"))
        addButton.setOnAction {
            val description = descriptionField.text
            if (description.isNotBlank()) {
                todoList.addTask(description)
                tasksObservableList.setAll(todoList.getAllTasks())
                descriptionField.clear()
            }
        }

        // HBox for text field and add button
        val inputContainer = HBox(descriptionField, addButton)
        inputContainer.spacing = 10.0 // Set spacing between text field and button
        inputContainer.alignment = Pos.CENTER

        // ListView to display tasks
        val tasksListView = ListView(tasksObservableList)

        // Customize the ListView to show tasks with CheckBoxes and delete button
        tasksListView.cellFactory = Callback {
            object : ListCell<Task>() {
                private val checkBox = CheckBox() // Create a checkbox
                private val deleteButton = Button("🗑") // Trash icon button
                private val descriptionText = Text() // Use Text for description

                init {
                    // Button styling
                    deleteButton.style = "-fx-background-color: transparent; -fx-padding: 0; -fx-border-color: transparent; -fx-text-fill: red;"
                    deleteButton.cursor = Cursor.HAND
                    checkBox.cursor = Cursor.HAND
                    Tooltip.install(checkBox, Tooltip("Check the task as completed"))
                    Tooltip.install(deleteButton, Tooltip("Delete the task"))

                    // Event to handle deletion
                    deleteButton.setOnAction {
                        todoList.deleteTask(item?.id ?: return@setOnAction)
                        tasksObservableList.setAll(todoList.getAllTasks())
                    }
                }

                override fun updateItem(item: Task?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if (empty || item == null) {
                        // Clear the cell if empty
                        checkBox.isVisible = false
                        deleteButton.isVisible = false
                        descriptionText.text = null
                    } else {
                        descriptionText.text = item.description // Set task description text
                        checkBox.isSelected = item.isCompleted // Set checkbox state based on completion

                        // Set text color and strikethrough based on completion status
                        descriptionText.fill = if (item.isCompleted) Color.GRAY else Color.BLACK
                        descriptionText.isStrikethrough = item.isCompleted

                        // Checkbox action
                        checkBox.setOnAction {
                            if (checkBox.isSelected) {
                                todoList.markTaskAsCompleted(item.id) // Mark as completed in the database
                            } else {
                                todoList.markTaskAsIncomplete(item.id) // Mark as incomplete in the database
                            }
                            tasksObservableList.setAll(todoList.getAllTasks()) // Refresh the ListView
                        }

                        // Create an HBox for checkbox and description
                        val mainHBox = HBox(checkBox, descriptionText)
                        mainHBox.spacing = 10.0

                        // Use Region as a spacer to push the delete button to the right
                        val spacer = Region()
                        HBox.setHgrow(spacer, Priority.ALWAYS) // Allow spacer to grow

                        // Combine components into a parent HBox
                        val parentHBox = HBox(mainHBox, spacer, deleteButton)
                        parentHBox.spacing = 10.0 // Set spacing between components
                        graphic = parentHBox // Set the HBox as the graphic for the cell

                        checkBox.isVisible = true // Show the checkbox
                        deleteButton.isVisible = true // Show the delete button
                    }
                }
            }
        }
        val nameLabel = Label("AmirHossein Zeinali Dehaghani (Student ID: 1225496)")
        nameLabel.alignment = Pos.CENTER
        nameLabel.padding = Insets(5.0)

        // Set up the main layout with the header, tasks list, input container, and name label
        val layout = VBox(10.0, headerContainer, tasksListView, inputContainer, nameLabel)
        layout.padding = Insets(10.0)
        layout.alignment = Pos.CENTER // Center all contents in the VBox

        val scene = Scene(layout, 400.0, 300.0)
        primaryStage.title = "TO-DO App"
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun showHelpDialog() {
        val helpAlert = Alert(Alert.AlertType.INFORMATION)
        helpAlert.title = "Help"
        helpAlert.headerText = null
        helpAlert.contentText = "This is a simple To-Do List application.\n\n" +
                "1. To add a task, write in Add a TODO textbox \"+\".\n" +
                "2. Use the checkboxes to mark tasks as completed.\n" +
                "3. Click the trash icon to delete a task."

        helpAlert.showAndWait() // Show the dialog and wait for user to close it
    }
}

// Main entry point for the application
fun main() {
    Application.launch(MainApp::class.java)
}