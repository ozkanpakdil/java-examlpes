package org.example.findprefixes;

import java.io.*;

public class Solution {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        int t = Integer.parseInt(br.readLine());

        while (t-- > 0) {
            String s = br.readLine();
            bw.write(Long.toString(calculateSimilarity(s)));
            bw.write("\n");
        }
        bw.flush();
    }

    private static long calculateSimilarity(String s) {
        int n = s.length();
        char[] str = s.toCharArray();

        // Initialize Z array
        int[] Z = new int[n];
        int left = 0, right = 0;
        long sum = n;  // Include the full string match

        for (int i = 1; i < n; i++) {
            if (i > right) {
                // No previous match to use, calculate directly
                left = right = i;
                while (right < n && str[right - left] == str[right]) {
                    right++;
                }
                Z[i] = right - left;
                right--;
            } else {
                int k = i - left;

                // If previous Z value fits within current Z-box
                if (Z[k] < right - i + 1) {
                    Z[i] = Z[k];
                } else {
                    // Need to check beyond the Z-box
                    left = i;
                    while (right < n && str[right - left] == str[right]) {
                        right++;
                    }
                    Z[i] = right - left;
                    right--;
                }
            }
            sum += Z[i];
        }

        return sum;
    }
}