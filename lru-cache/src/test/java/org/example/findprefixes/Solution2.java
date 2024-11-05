package org.example.findprefixes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.IntStream;

class Result {

    /*
     * Complete the 'stringSimilarity' function below.
     *
     * The function is expected to return an INTEGER.
     * The function accepts STRING s as parameter.
     */

    public static int stringSimilarity(String s) {
        int r = s.length();
        for (int i = 1; i < s.length(); i++) {
            String s2 = s.substring(i);

            for (int j = 0; j < s2.length(); j++) {
                if (s.charAt(j) == s2.charAt(j)) {
                    r++;
                } else
                    break;
            }
        }
        return r;
    }

    public static long stringSimilarity2(String str) {
        if (str == null || str.isEmpty())
            return 0;
        if (str.length() == 1)
            return 1;

        // Convert to char array for faster access
        char[] s = str.toCharArray();
        int n = s.length;
        long result = n;  // Count the full string itself

        // Use array instead of String.substring() to avoid creating new strings
        for (int start = 1; start < n; start++) {
            int matchLen = 0;

            // Compare characters directly using the array
            // Break early if remaining length can't beat current best
            int maxPossible = n - start;

            while (matchLen < maxPossible && s[matchLen] == s[start + matchLen]) {
                matchLen++;
            }

            result += matchLen;
        }

        return result;
    }

}

public class Solution2 {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int t = Integer.parseInt(bufferedReader.readLine().trim());

        IntStream.range(0, t).forEach(tItr -> {
            try {
                String s = bufferedReader.readLine();

                int result = Result.stringSimilarity(s);

                bufferedWriter.write(String.valueOf(result));
                bufferedWriter.newLine();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        bufferedReader.close();
        bufferedWriter.close();
    }
}
