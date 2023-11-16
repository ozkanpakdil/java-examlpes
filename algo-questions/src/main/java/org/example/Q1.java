package org.example;

import java.util.Stack;

/*
* make a Queue from using stacks
* */
public class Q1 {
    private final Stack<Integer> newest =new Stack<>();
    private final Stack<Integer> oldest=new Stack<>();

    void enqueue(Integer e){
        newest.push(e);
    }

    private void shift(){
        while(!newest.empty()){
            oldest.push(newest.pop());
        }
    }

    Integer deque(){
        shift();
        return oldest.pop();
    }
}
