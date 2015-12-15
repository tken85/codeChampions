package com.codechampions.controllers;

import com.codechampions.entities.Message;
import com.codechampions.entities.User;
import com.codechampions.services.MessageRepository;
import com.codechampions.services.UserRepository;
import com.codechampions.utils.PasswordHash;
import org.hibernate.annotations.SourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

/**
 * Created by Jack on 12/9/15.
 */

@RestController
public class CodeChampionsController {
    @Autowired
    UserRepository users;
    @Autowired
    MessageRepository messages;

    @PostConstruct
    public void init() throws InvalidKeySpecException, NoSuchAlgorithmException, FileNotFoundException {
        User admin = users.findOneByUsername("Admin");
        if (admin == null) {
            admin = new User();
            admin.username = "Admin";
            admin.password = PasswordHash.createHash("Admin");
            users.save(admin);
        }


            Message message = new Message(1, -1, "Game Message Board", admin);
            Message message1 = new Message(2, -1, "Classroom Message Board", admin);
            Message message2 = new Message(3, -1, "Lesson Message Board", admin);
            Message message3 = new Message(5, 0, "Hello Game Board!", admin);
            Message message4 = new Message(5, 1, "Hello Classroom Board!", admin);
            messages.save(message);
            messages.save(message1);
            messages.save(message2);
            messages.save(message3);
            messages.save(message4);
    }

    @RequestMapping(path = "/newUser", method = RequestMethod.POST)
    public User createUser(HttpServletResponse response, @RequestBody User tempUser) throws Exception {
        if (tempUser.username == null || tempUser.password == null) {
            response.sendError(403, "Please enter both a username and password!");
        } else if (users.findOneByUsername(tempUser.username) != null) {
            response.sendError(404, "Username already exists");
        } else {
            User user = new User();
            user.username = tempUser.username;
            user.password = PasswordHash.createHash(tempUser.password);
            user.email = tempUser.email;
            users.save(user);
            System.out.println("Success!");
            return user;
        }
        return null;
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public User login(HttpServletResponse response, HttpSession session, @RequestBody User tempUser) throws Exception {
        User user = users.findOneByUsername(tempUser.username);
        session.setAttribute("username", tempUser.username);
        session.getAttribute(tempUser.username);

        if (tempUser.username == null || tempUser.password == null) {
            response.sendError(404, "Please enter both a username and password!");
        } else if (user == null) {
            response.sendError(403, "Username does not exist!");
        } else if (!PasswordHash.validatePassword(tempUser.password, user.password)) {
            response.sendError(405, "Wrong Password!");
        } else {
            System.out.println("Success!");
            return user;
        }
        return null;
    }

    @RequestMapping("/logout")
    public void logout(HttpSession session) throws IOException {
        session.invalidate();
        System.out.println("Successfully Logged Out!");
    }

    @RequestMapping("/users")
    public List<User> users() {
        return (List<User>) users.findAll();
    }

    @RequestMapping(path = "/editUser/{id}", method = RequestMethod.PUT)
    public void editUser(HttpSession session, HttpServletResponse response, @RequestBody User tempUser) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        session.getAttribute(tempUser.username);
        User user = users.findOne(tempUser.id);

        if (tempUser.username == null) {
            response.sendError(403, "Not logged in.");
        } else if (users.findOneByUsername(tempUser.username) != null) {
            response.sendError(404, "Username already exists!");
        } else {
            user.username = tempUser.username;
            users.save(user);
        }
    }

    @RequestMapping("/messages")
    public List<Message> messages() {
        return (List<Message>) messages.findAll();
    }

    @RequestMapping("/showGameBoard")
        public Message gameMessage() {
            return messages.findOne(1);
        }
}