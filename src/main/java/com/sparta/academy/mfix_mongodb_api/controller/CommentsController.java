package com.sparta.academy.mfix_mongodb_api.controller;

import com.sparta.academy.mfix_mongodb_api.model.entity.Comment;
import com.sparta.academy.mfix_mongodb_api.repositories.CommentRepository;
import com.sparta.academy.mfix_mongodb_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class CommentsController {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentsController(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    //GET ---------------------------------------------------------------
    @GetMapping("/comments/all")
    public ResponseEntity<List<Comment>> getComments() {
        try{
            return new ResponseEntity<>(commentRepository.findAll(),HttpStatus.OK) ;
        }catch (Exception e){
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/comments/id/{id}")
    public ResponseEntity<Comment> getCommentsById(@PathVariable String id) {

        HttpStatus status = HttpStatus.OK;
        Comment comment = null;
        if ( commentRepository.existsById(id) ) {
            comment = commentRepository.findCommentById(id);
        } else {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(comment, status);
    }

    @GetMapping("/comments/name/{name}")
    public ResponseEntity<List<Comment>> getCommentsByName(@PathVariable String name) {
        if (commentRepository.findCommentByNameContaining(name).isEmpty()
                &&commentRepository.findCommentByNameContaining(name)==null){
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(commentRepository.findCommentByNameContaining(name),HttpStatus.OK);
    }
    @GetMapping("/comments/name/")
    public ResponseEntity<List<Comment>> getCommentsByNameBody(@RequestBody String name) {
        if (commentRepository.findCommentByNameContaining(name).isEmpty()
                &&commentRepository.findCommentByNameContaining(name)==null){
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(commentRepository.findCommentByNameContaining(name),HttpStatus.OK);
    }


    @GetMapping("/comments/date/{date}")
    public ResponseEntity<List<Comment>> getCommentsByDateYear(@PathVariable String date){
        List<Comment> listOfComments = new ArrayList<>();
        if (date==null){
            return new ResponseEntity<>(listOfComments, HttpStatus.BAD_REQUEST);
        }
        DateTimeFormatter format
                = DateTimeFormatter.ofPattern("EEE-LLL-d-HH:mm:ss-zzz-yyyy", Locale.ENGLISH);
        List<String> YMD = List.of(date.split("-"));
        List<Integer> YMDN = new ArrayList<>();

        for (String s: YMD){
            if (s.matches("\\d+(\\.\\d+)?")){
                YMDN.add(Integer.parseInt(s));
            }else {
                return new ResponseEntity<>(listOfComments, HttpStatus.BAD_REQUEST);
            }
        }
        LocalDateTime dateTime;
        if (YMDN.size()==1){
            dateTime = LocalDateTime.of(YMDN.get(0),1,1,0,0);
            return new ResponseEntity<>(commentRepository.findCommentByDateBetween(dateTime,dateTime.plusYears(1)),HttpStatus.OK);
        } else if (YMDN.size()==2) {
            dateTime = LocalDateTime.of(YMDN.get(0),YMDN.get(1),1,0,0);
            return new ResponseEntity<>(commentRepository.findCommentByDateBetween(dateTime,dateTime.plusMonths(1)),HttpStatus.OK);
        } else {
            dateTime = LocalDateTime.of(YMDN.get(0),YMDN.get(1),YMDN.get(2),0,0);
            return new ResponseEntity<>(commentRepository.findCommentByDateBetween(dateTime,dateTime.plusDays(1)),HttpStatus.OK);
        }
    }


    @GetMapping("/comments/movie/{id}")
    public ResponseEntity<List<Comment>> getCommentsByMovie(@PathVariable String id) {
        ResponseEntity<List<Comment>> response;
        try{
            response = new ResponseEntity<>(commentRepository.findCommentByMovieId(id),HttpStatus.OK);
        }catch (Exception e){
            response = new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    @GetMapping("/comments/email/{email}")
    public ResponseEntity<List<Comment>> getEmailsByEmail(@PathVariable String email) {
        if (commentRepository.findCommentByEmail(email).isEmpty()){
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(commentRepository.findCommentByEmail(email),HttpStatus.OK);
    }
    @GetMapping("/comments/email/")
    public ResponseEntity<List<Comment>> getEmailsByEmailBody(@RequestBody String email) {
        if (commentRepository.findCommentByEmail(email).isEmpty()){
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(commentRepository.findCommentByEmail(email),HttpStatus.OK);
    }



    @GetMapping("/comments/text/{text}")
    public ResponseEntity<List<Comment>> getCommentsByText(@PathVariable String text) {
        if (commentRepository.findCommentByTextContaining(text).isEmpty()){
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(commentRepository.findCommentByTextContaining(text),HttpStatus.OK);

    }

    //Request body with just the words you wanted to input e.g. Minima odit
    @GetMapping("/comments/text/")
    public ResponseEntity<List<Comment>> getCommentsByTextBody(@RequestBody String text) {
        if (commentRepository.findCommentByTextContaining(text).isEmpty()){
            return new ResponseEntity<>(commentRepository.findCommentByTextContaining(text),HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(commentRepository.findCommentByTextContaining(text),HttpStatus.OK);
    }

    //PUT
    //UPDATE COMMENT [ updates comment text ]
    @PutMapping("/comments/id/{id}")
    public ResponseEntity<Comment> updateCommentWithID(@RequestBody String text, @PathVariable String id) {

        if ( text == null || text.equals("")) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        HttpStatus status = HttpStatus.NOT_FOUND;
        Comment body = null;

        if ( commentRepository.existsById(id) ) {
            Comment comment = commentRepository.findCommentById(id);
            comment.setText(text);// Update Comment
            body = commentRepository.save(comment);
        }
        return new ResponseEntity<>(body, status);
    }

    //POST
    // INSERT COMMENT:
    @PostMapping("/comments")
    public ResponseEntity<Comment> insertComment(@RequestBody Comment comment) {

        HttpStatus status = HttpStatus.OK;
        Comment insertedComment = null;


        // Create insertion timestamp
        ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneOffset.UTC);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE LLL d HH:mm:ss zzz yyyy", Locale.ENGLISH);

        // Set timestamp
        comment.setDate(zdt.format(format));

        if (isCommentBodyValid(comment)) {
            insertedComment = commentRepository.save(comment);
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(insertedComment, status);
    }

    //DELETES ---------------------------------------------------------------

    @DeleteMapping("/comments/all/email/{email}")
    public ResponseEntity<String> deleteAllCommentsByUserEmail(@PathVariable String email) {

        HttpStatus status = HttpStatus.OK;
        String body = "ALL COMMENTS SUCCESSFULLY DELETED";

        if ( !userRepository.existsUserByEmail(email) ) {
            status = HttpStatus.NOT_FOUND;
            body = "EMAIL DOES NOT EXIST";
        } else {
            commentRepository.deleteAllByEmail(email);
        }
        return new ResponseEntity<>(body, status);
    }

    @DeleteMapping("/comments/id/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable String id) {

        HttpStatus status = HttpStatus.OK;
        String body = "COMMENT SUCCESSFULLY DELETED";

        if ( !commentRepository.existsById(id) ) {
            status = HttpStatus.NOT_FOUND;
            body = "ID DOES NOT EXIST";
        } else {
            commentRepository.deleteById(id);
        }
        return new ResponseEntity<>(body, status);
    }

    public boolean isCommentBodyValid(Comment comment) {
        return  isValueValid(comment.getEmail()) &&
                isValueValid(comment.getName()) &&
                isValueValid(comment.getText()) &&
                isValueValid(comment.getMovieId());
    }

    public boolean isValueValid(String value) {
        return value != null && value.length() > 0;
    }

}
