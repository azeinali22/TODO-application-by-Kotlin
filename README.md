# Kotlin TODO Desktop Application using Sqlite database for storing
# Overview
This To-Do List Application is designed and Built on the Kotlin programming language using JavaFX for the graphical UI
and SQLite for storing and retrieving from Database.
# Features
• Add a todo: Users can add new tasks.\
• Render the todo items: Retrieving todo items from DB and displaying in the user interface.\
• Mark a task as completed: Checkbox functionality allows users to mark tasks as completed.\
• Delete todo items: Delete tasks with a trash icon.\
• Help Functionality: A Help button in order to provide instructions on using the application.\
# Technologies Used
• Kotlin\
• JavaFX: graphical UI\
• SQLite: Using database for retrieving and storing the TODO list
# Development Process (Class Structure)
• Task Class: Represents individual todo items, including properties for ID, description, and completion status.\
This class is responsible for the data representation of tasks.\
• Database Object: A singleton object managing the SQLite database connection. It contains methods for
initializing the database and creating the necessary tables.\
• TodoList Class: This class contains methods (addTask, markTaskAsCompleted, markTaskAsIncomplete,
deleteTask, getAllTasks) for interacting with the tasks in the database. It handles logic related to task
management.\
• MainApp Class: The main application class that initializes the UI components using JavaFX. It handles user
interactions and event handling for adding, deleting, and updating tasks. This class orchestrates the overall
functionality of the application.
