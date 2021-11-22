package com.android.core.taskflow

interface ITaskCreator {
    fun createTask(taskName: String): Task
}