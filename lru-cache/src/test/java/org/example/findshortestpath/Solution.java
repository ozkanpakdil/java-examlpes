package org.example.findshortestpath;

import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class Result {

    //https://www.hackerrank.com/challenges/coin-on-the-table/problem?utm_campaign=challenge-recommendation&utm_medium=email&utm_source=24-hour-campaign

    private static char[][] board;
    private static int[][] costs;

    private static void dfs(int row, int column, int cost, int time, int k) {
        if (!inBoard(row, column) || cost >= costs[row][column]) {
            return;
        }
        costs[row][column] = cost;
        if (board[row][column] == '*') {
            return;
        }
        if (time == k) {
            return;
        }
        dfs(row - 1, column, board[row][column] == 'U' ? cost : cost + 1, time + 1, k);
        dfs(row, column - 1, board[row][column] == 'L' ? cost : cost + 1, time + 1, k);
        dfs(row + 1, column, board[row][column] == 'D' ? cost : cost + 1, time + 1, k);
        dfs(row, column + 1, board[row][column] == 'R' ? cost : cost + 1, time + 1, k);
    }

    private static boolean inBoard(int row, int column) {
        return row >= 0 && row < board.length && column >= 0 && column < board[row].length;
    }


    /*
     * Complete the 'coinOnTheTable' function below.
     *
     * The function is expected to return an INTEGER.
     * The function accepts following parameters:
     *  1. INTEGER m
     *  2. INTEGER k
     *  3. STRING_ARRAY board
     */

    public static int coinOnTheTable(int m, int k, List<String> boardList) {
        board = new char[boardList.size()][m];
        costs = new int[boardList.size()][m];
        for (int[] cost : costs) {
            Arrays.fill(cost, Integer.MAX_VALUE);
        }
        for (int i = 0; i < boardList.size(); i++) {
            for (int j = 0; j < m; j++) {
                board[i][j] = boardList.get(i).charAt(j);
            }
        }
        dfs(0, 0, 0, 0, k);

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == '*') {
                    int minCost = costs[i][j];
                    return minCost == Integer.MAX_VALUE ? -1 : minCost;
                }
            }
        }

        return -1;
    }


}

public class Solution {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        String[] firstMultipleInput = bufferedReader.readLine().replaceAll("\\s+$", "").split(" ");

        int n = Integer.parseInt(firstMultipleInput[0]);

        int m = Integer.parseInt(firstMultipleInput[1]);

        int k = Integer.parseInt(firstMultipleInput[2]);

        List<String> board = IntStream.range(0, n).mapToObj(i -> {
                    try {
                        return bufferedReader.readLine();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(toList());

        int result = Result.coinOnTheTable(m, k, board);

        System.out.println(result);
        bufferedReader.close();
    }
}
