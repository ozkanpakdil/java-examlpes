import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FastSlowPointers {
    public int findDuplicate(int[] nums) {
        Set<Integer> s = new HashSet<>();
        for (int num : nums) {
            if (s.contains(num)) {
                return num;
            }
            s.add(num);
        }
        return -1;

    }

    public int findDuplicateFastest(int[] nums) {
        int n = nums.length;
        boolean[] a = new boolean[n];
        for (int num : nums) {
            if (a[num])
                return num;
            a[num] = true;
        }
        return -1;
    }

    public int climbStairs(int n) {
        if (n == 0 || n == 1)
            return 1;

        return climbStairs(n - 1) + climbStairs(n - 2);
    }

    public int[] findDiagonalOrder(int[][] mat) {
        int n = mat.length, mode = 0, it = 0, lower = 0;
        List<Integer> res=new LinkedList<Integer>();

        // 2n will be the number of iterations
        for (int t = 0; t < (2 * n - 1); t++) {
            int t1 = t;
            if (t1 >= n) {
                mode++;
                t1 = n - 1;
                it--;
                lower++;
            }
            else {
                lower = 0;
                it++;
            }
            for (int i = t1; i >= lower; i--) {
                if ((t1 + mode) % 2 == 0) {
                    res.add(mat[i][t1 + lower - i]);
                }
                else {
                    res.add(mat[t1 + lower - i][i]);
                }
            }
        }

        return res.stream().mapToInt(Integer::intValue).toArray();
    }
    @Test
    public void test() {
//        int[] nums = {1, 3, 4, 2, 2};
//        System.out.println(findDuplicate(nums));
        int[] diagonalOrder = findDiagonalOrder(new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } });
        System.out.println(Arrays.toString(diagonalOrder));
    }
}
