package com.ribeirowski.todolist.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ribeirowski.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("")
    public ResponseEntity create(@RequestBody TaskModel task, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        task.setIdUser((UUID) idUser);

        var curDate = LocalDateTime.now();
        if (curDate.isAfter(task.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR! Start date is before current date!");
        }

        if (curDate.isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR! End date is before current date!");
        }

        if (task.getStartAt().isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR! End date is before start date!");
        }

        var taskCreated = this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(taskCreated);
    }

    @GetMapping("")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var taskList = this.taskRepository.findByIdUser((UUID) idUser);
        return taskList;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel task, HttpServletRequest request, @PathVariable UUID id) {
        var idUser = request.getAttribute("idUser");

        var taskid = this.taskRepository.findById(id).orElse(null);

        if (taskid == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR! Task not found!");
        }

        if (!taskid.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR! You can't update this task!");
        }

        Utils.copyNonNullProperties(task, taskid);

        var taskUpdated = this.taskRepository.save(taskid);

        return ResponseEntity.ok().body(taskUpdated);
    }
}