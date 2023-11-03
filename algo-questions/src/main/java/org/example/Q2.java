package org.example;

import java.util.Stack;

/*
 Given a string, that contains special character together with alphabets (‘a’ to ‘z’ and ‘A’ to ‘Z’), reverse the string ia way that special characters are not affected

 * */
public class Q2 {

    String reverse(String input) {
        Stack<Character> result = new Stack<>();
        StringBuilder result2 = new StringBuilder();
        char[] in = input.toCharArray();
        for (char c : in) {
            if (Character.isLetter(c)) {
                result.push(c);
            }
        }
        for (char c : in) {
            if (Character.isLetter(c)) {
                result2.append(result.pop());
            } else {
                result2.append(c);
            }
        }

        return result2.toString();
    }

}
