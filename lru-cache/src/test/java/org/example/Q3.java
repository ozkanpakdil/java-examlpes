package org.example;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class Q3 {
    //1930. Unique Length-3 Palindromic Subsequences
    // https://leetcode.com/problems/unique-length-3-palindromic-subsequences/?envType=daily-question&envId=2023-11-14
    /*
    Given a string s, return the number of unique palindromes of length three that are a subsequence of s.

Note that even if there are multiple ways to obtain the same subsequence, it is still only counted once.

A palindrome is a string that reads the same forwards and backwards.

A subsequence of a string is a new string generated from the original string with some characters (can be none) deleted without changing the relative order of the remaining characters.

    For example, "ace" is a subsequence of "abcde".



Example 1:

Input: s = "aabca"
Output: 3
Explanation: The 3 palindromic subsequences of length 3 are:
- "aba" (subsequence of "aabca")
- "aaa" (subsequence of "aabca")
- "aca" (subsequence of "aabca")

Example 2:

Input: s = "adc"
Output: 0
Explanation: There are no palindromic subsequences of length 3 in "adc".

Example 3:

Input: s = "bbcbaba"
Output: 4
Explanation: The 4 palindromic subsequences of length 3 are:
- "bbb" (subsequence of "bbcbaba")
- "bcb" (subsequence of "bbcbaba")
- "bab" (subsequence of "bbcbaba")
- "aba" (subsequence of "bbcbaba")

    * */

    public static Set<String> generatePalindromes() {
        Set<String> result = new HashSet<>();
        for (char c1 = 'a'; c1 <= 'z'; c1++) {
            for (char c2 = 'a'; c2 <= 'z'; c2++) {
                for (char c3 = 'a'; c3 <= 'z'; c3++) {
                    if (c1 == c3) {
                        result.add("" + c1 + c2 + c3);
                    }
                }
            }
        }
        return result;
    }

    public int countPalindromicSubsequence(String s) {
        char[] arr = s.toCharArray();
        if (arr.length == 3) return 0;
        int result = 0;
        for (String p1 : generatePalindromes()) {
            String p2=p1;
            for (char c : arr) {
                if (p1.contains(c + "")) {
                    p1 = p1.replace(c, ' ');
                }
                if (p1.trim().isEmpty()) {
                    result++;
                    System.out.println(p2);
                    break;
                }
            }
        }
        return result;
    }

    @Test
    void test() {
        int i = countPalindromicSubsequence("aabca");
        System.out.println(i);
    }

}
