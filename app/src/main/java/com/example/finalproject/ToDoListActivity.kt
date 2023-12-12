package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ToDoListActivity : AppCompatActivity() {

    private lateinit var recyclerViewTodoList: RecyclerView
    private lateinit var buttonAddTodo: ImageButton
    private lateinit var todoListAdapter: ToDoAdapter
    private lateinit var signOut: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_list)

        recyclerViewTodoList = findViewById(R.id.recyclerViewTodoList)
        buttonAddTodo = findViewById(R.id.buttonAddTodo)

        signOut = findViewById<TextView>(R.id.signOutButtonTD)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val menu = bottomNavigation.menu
        val secondMenuItem = menu.getItem(1)
        secondMenuItem.isChecked = true

        buttonAddTodo.setOnClickListener{
            onAddTodoClick(buttonAddTodo)
        }

        signOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_ToDo -> true
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        todoListAdapter = ToDoAdapter()
        recyclerViewTodoList.adapter = todoListAdapter
        recyclerViewTodoList.layoutManager = LinearLayoutManager(this)
        populateTodoList()
    }

    fun onAddTodoClick(anchorView: View) {
        Toast.makeText(this, "Add Todo clicked", Toast.LENGTH_SHORT).show()
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.your_menu_resource, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_gallery -> {
//                    choosePhotoFromGallery()
                    true
                }

                R.id.menu_take_picture -> {
//                    takePhotoFromCamera()
                    true
                }

                else -> false
            }
        }
    }

    private fun populateTodoList() {
        val todoItems = mutableListOf<ToDoItem>()
        todoItems.add(ToDoItem("Task 1", false))
        todoItems.add(ToDoItem("Task 2", true))
        todoItems.add(ToDoItem("Task 3", false))

        todoListAdapter.updateData(todoItems)
    }
}